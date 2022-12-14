package group.liquido.databuffer.core;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Pair;
import group.liquido.databuffer.core.common.BufferFlushListenerWrapper;
import group.liquido.databuffer.core.common.DelegateDataBuffer;
import group.liquido.databuffer.core.epoll.AbstractEventPoller;
import group.liquido.databuffer.core.epoll.PollableEvent;
import group.liquido.databuffer.core.epoll.PollableEventListener;
import group.liquido.databuffer.core.event.BufferFlushEvent;
import group.liquido.databuffer.core.event.DataBufferLayerCloseEvent;
import group.liquido.databuffer.core.event.DataBufferLayerEvent;
import group.liquido.databuffer.core.event.DataBufferLayerOpenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vinfer
 * @date 2022-12-06 17:02
 */
public class BufferEventPoller extends AbstractEventPoller implements BufferFlushListenerRegistry,
        ApplicationEventPublisherAware, ApplicationListener<DataBufferLayerEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferEventPoller.class);

    private final BufferStore bufferStore;

    private final Map<String, BufferFlushListenerWrapper> registeredListenerMap = new ConcurrentHashMap<>();

    private final Map<String, PollableEvent> pollableEventMap = new ConcurrentHashMap<>();

    private final ExecutorService executorService;

    private BufferFlushEventFactory bufferFlushEventFactory;

    private ApplicationEventPublisher eventPublisher;

    private long maxWaitForFlushing;

    static class BufferPollingEvent extends PollableEvent {

        private final BufferStore bufferStore;
        private final int bufferSize;
        private final String bufferKey;
        private final Class<?> fetchType;
        private final long maxWaitForFlushing;
        private final AtomicInteger bufferCounter;
        private StopWatch stopWatch;

        BufferPollingEvent(BufferStore bufferStore,
                           long maxWaitForFlushing,
                           String bufferKey,
                           Class<?> fetchType) {
            this.bufferStore = bufferStore;
            this.bufferSize = bufferStore.getBufferSize();
            this.bufferKey = bufferKey;
            this.fetchType = fetchType;
            this.maxWaitForFlushing = maxWaitForFlushing;
            this.bufferCounter = new AtomicInteger(0);
            resumeStopWatch();
        }

        @Override
        public boolean isReady() {
            int bufferItemCount = bufferStore.countBufferItem(bufferKey);

            if (bufferItemCount > 0 && bufferItemCount < bufferSize) {
                stopWatch.stop();
                // check max wait (cumulative waiting time)
                long totalTimeMillis = stopWatch.getTotalTimeMillis();
                // restart stopWatch to accumulate the wait time
                stopWatch.start(bufferKey);

                if (totalTimeMillis >= maxWaitForFlushing) {
                    LOGGER.info("BufferPollingEvent isReady the waiting time {} has exceeded the maximum waiting time {}, current count is {} event will be ready now",
                            totalTimeMillis, maxWaitForFlushing, bufferItemCount);
                    // resume the stopWatch for recording next accumulating wait time
                    resumeStopWatch();
                    bufferCounter.addAndGet(bufferItemCount);
                    return true;
                }
            }

            bufferCounter.addAndGet(bufferItemCount);

            return bufferItemCount >= bufferSize;
        }

        private void resumeStopWatch() {
            stopWatch = StopWatch.create(bufferKey);
            stopWatch.start(bufferKey);
        }

        @Override
        public void commit() {
            // do not mark state as committed, make this event reusable
            // 假装事件提交
            LOGGER.info("BufferPollingEvent commit fake event submission, event will be reconsume in next ready state, already consumed buffer count {}, ", bufferCounter.get());
        }

        public void destroy() {
            super.commit();
            LOGGER.info("BufferPollingEvent destroy event has been committed, already consumed buffer count {}", bufferCounter.get());
        }

        @Override
        public Object getEventSource() {
            return bufferStore.fetchBuffers(bufferKey, fetchType);
        }

    }

    static class BufferEventPublisher<T> implements PollableEventListener {

        private final String bufferKey;
        private final BufferFlushEventFactory bufferFlushEventFactory;
        private final ApplicationEventPublisher eventPublisher;
        private DataBuffer dataBuffer;

        BufferEventPublisher(String bufferKey,
                             BufferFlushEventFactory bufferFlushEventFactory,
                             ApplicationEventPublisher eventPublisher) {
            this.bufferKey = bufferKey;
            this.bufferFlushEventFactory = bufferFlushEventFactory;
            this.eventPublisher = eventPublisher;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onEventReady(PollableEvent event) {
            Collection<T> eventSource = (Collection<T>) event.getEventSource();
            DataBuffer dataBuffer = DelegateDataBuffer.ofKeyCollection(bufferKey, eventSource);
            this.dataBuffer = dataBuffer;
            BufferFlushEvent bufferFlushEvent = bufferFlushEventFactory.createBufferFlushEvent(dataBuffer);
            eventPublisher.publishEvent(bufferFlushEvent);
        }

        protected DataBuffer getDataBuffer() {
            return dataBuffer;
        }

    }

    static class DelegateBufferEventListener<T> extends BufferEventPublisher<T> {

        private final BufferFlushListener bufferFlushListener;

        DelegateBufferEventListener(String bufferKey,
                                    BufferFlushEventFactory bufferFlushEventFactory,
                                    ApplicationEventPublisher eventPublisher,
                                    BufferFlushListener bufferFlushListener) {
            super(bufferKey, bufferFlushEventFactory, eventPublisher);
            this.bufferFlushListener = bufferFlushListener;
        }

        @Override
        public void onEventReady(PollableEvent event) {
            super.onEventReady(event);
            bufferFlushListener.onBufferFlush(getDataBuffer());
            event.commit();
        }
    }

    public BufferEventPoller(BufferStore bufferStore) {
        this(bufferStore, null);
    }

    public BufferEventPoller(BufferStore bufferStore, ExecutorService es) {
        Assert.notNull(bufferStore, "BufferEventPoller bufferStore must not null");

        this.bufferStore = bufferStore;
        this.executorService = es;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(DataBufferLayerEvent event) {
        if (event instanceof DataBufferLayerOpenEvent) {
            LOGGER.info("BufferEventPoller on data buffer layer open event...");
            DataBufferLayer dataBufferLayer = ((DataBufferLayerOpenEvent) event).getDataBufferLayer();
            this.bufferFlushEventFactory = dataBufferLayer.getBufferFlushEventFactory();
            this.maxWaitForFlushing = dataBufferLayer.getMaxWaitForFlushing();
        }else if (event instanceof DataBufferLayerCloseEvent) {
            LOGGER.info("BufferEventPoller on data buffer layer close event...");
            flushRemainsBuffers();
        }
    }

    private <T> PollableEvent createPollableEvent(String bufferKey, Class<T> bufferType) {
        return new BufferPollingEvent(
                bufferStore,
                maxWaitForFlushing,
                bufferKey,
                bufferType
        );
    }

    private <T> PollableEventListener createPollableEventListener(String bufferKey, Class<T> bufferType, BufferFlushListener bufferFlushListener) {
        return new DelegateBufferEventListener<T>(
                bufferKey,
                bufferFlushEventFactory,
                eventPublisher,
                bufferFlushListener
        );
    }

    public void flushRemainsBuffers() {
        if (CollectionUtil.isEmpty(registeredListenerMap)) {
            return;
        }

        // commit all pollable events
        for (PollableEvent event : pollableEventMap.values()) {
            ((BufferPollingEvent)event).destroy();
        }

        for (String bufferKey : registeredListenerMap.keySet()) {
            doFlushRemainBuffers(bufferKey);
        }

        bufferStore.clearBufferBuckets();
    }

    private void doFlushRemainBuffers(String bufferKey) {
        BufferFlushListenerWrapper listenerWrapper = registeredListenerMap.get(bufferKey);
        Class<?> bufferType = listenerWrapper.getBufferType();
        List<? extends Collection<?>> remainDataBuffers = bufferStore.fetchAll(bufferKey, bufferType);
        if (CollectionUtil.isNotEmpty(remainDataBuffers)) {
            for (Collection<?> remainDataBuffer : remainDataBuffers) {
                publishAndFlush(bufferKey, remainDataBuffer, listenerWrapper);
            }
        }
    }

    private <T> void publishAndFlush(String bufferKey, Collection<T> dataBuffers, BufferFlushListener listener) {
        DataBuffer dataBuffer = DelegateDataBuffer.ofKeyCollection(bufferKey, dataBuffers);
        BufferFlushEvent bufferFlushEvent = bufferFlushEventFactory.createBufferFlushEvent(dataBuffer);
        eventPublisher.publishEvent(bufferFlushEvent);
        listener.onBufferFlush(dataBuffer);
    }

    @Override
    public <T> void registerListener(String bufferKey, BufferFlushListener listener, Class<T> bufferType) {
        if (null == bufferFlushEventFactory) {
            throw new IllegalStateException("BufferEventPoller registerListener bufferFlushEventFactory is not ready yet, make sure you have already opened a DataBufferLayer");
        }

        if (containsListener(bufferKey)) {
            LOGGER.warn("BufferEventPoller registerListener listener with buffer key {} is already registered", bufferKey);
            return;
        }

        // register event
        PollableEvent pollableEvent = createPollableEvent(bufferKey, bufferType);
        PollableEventListener pollableEventListener = createPollableEventListener(bufferKey, bufferType, listener);
        registerEvent(pollableEvent, pollableEventListener);

        // put map
        registeredListenerMap.put(bufferKey, new BufferFlushListenerWrapper(bufferKey, bufferType, listener));
        pollableEventMap.put(bufferKey, pollableEvent);
    }

    @Override
    public void unregisterListener(String bufferKey) {
        registeredListenerMap.remove(bufferKey);
        PollableEvent removed = pollableEventMap.remove(bufferKey);
        if (removed != null) {
            if (removed.isReady()) {
                // try consuming the remains buffer before unregistering
                doFlushRemainBuffers(bufferKey);
            }
            ((BufferPollingEvent)removed).destroy();
        }
    }

    @Override
    public boolean containsListener(String bufferKey) {
        return registeredListenerMap.containsKey(bufferKey);
    }

    @Override
    public Set<String> getRegisteredBufferKeys() {
        return registeredListenerMap.keySet();
    }

    @Override
    public BufferFlushListener getRegisteredListener(String bufferKey) {
        return registeredListenerMap.get(bufferKey);
    }

    @Override
    protected ExecutorService getExecutorService() {
        return Objects.requireNonNullElse(executorService, super.getExecutorService());
    }

    @Override
    protected void beforeShutdown(List<Pair<PollableEvent, PollableEventListener>> eventList) {
        // clear all remain buffers if any
        flushRemainsBuffers();
    }
}
