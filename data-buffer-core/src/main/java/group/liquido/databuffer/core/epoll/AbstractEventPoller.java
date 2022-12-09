package group.liquido.databuffer.core.epoll;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author vinfer
 * @date 2022-12-06 14:50
 */
public abstract class AbstractEventPoller extends AbstractStatefulService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventPoller.class);

    private long pollingInterval = 0L;

    private long cleanerScanningInterval = 30 * 1000L;

    private final AtomicBoolean startInScheduleMode = new AtomicBoolean(false);

    private final List<Pair<PollableEvent, PollableEventListener>> eventList = new CopyOnWriteArrayList<>();

    public void setPollingInterval(long pollingIntervalMill) {
        this.pollingInterval = pollingIntervalMill;
    }

    public void setCleanerScanningInterval(long cleanerScanningInterval) {
        this.cleanerScanningInterval = cleanerScanningInterval;
    }

    public long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * append a pair of {@link PollableEvent} and its {@link PollableEventListener} into this poller.
     * @param event             {@link PollableEvent}, must not null
     * @param eventListener     {@link PollableEventListener}, must not null
     */
    public void registerEvent(PollableEvent event, PollableEventListener eventListener) {
        Assert.notNull(event, "AbstractEventPoller appendEvent event must not null");
        Assert.notNull(eventListener, "AbstractEventPoller appendEvent eventListener must not null");
        eventList.add(Pair.of(event, eventListener));
    }

    public boolean isStartInScheduleMode() {
        return startInScheduleMode.get();
    }

    public void startInScheduleMode() {
        if (startInScheduleMode.compareAndSet(false, true)) {
            doPolling();
        }
    }

    @Override
    protected void startInternal() throws Throwable {
        // start polling
        LOGGER.info("AbstractEventPoller startInternal poller service starting...");
        while (isRunning()) {
            doPolling();

            if (!isStartInScheduleMode() && pollingInterval > 0) {
                Thread.sleep(pollingInterval);
            }
        }
        LOGGER.info("AbstractEventPoller startInternal poller looping is over...");
    }

    private void doPolling() {
        for (Pair<PollableEvent, PollableEventListener> pair : eventList) {
            PollableEvent event = pair.getKey();
            PollableEventListener listener = pair.getValue();
            try {
                if (event.isReady()) {
                    ExecutorService executorService = getExecutorService();
                    if (null != executorService) {
                        executorService.submit(() -> listener.onEventReady(event));
                    }else {
                        listener.onEventReady(event);
                    }
                }
            }catch (Throwable t) {
                LOGGER.error("AbstractEventPoller doPolling error occurs while polling, current checking event is ["+event+"]", t);
                // alarm monitor
            }
        }

        if (CollectionUtil.isNotEmpty(eventList)) {
            // check the first event is ready or not,
            // keep polling if it's ready.
            Pair<PollableEvent, PollableEventListener> first = CollectionUtil.getFirst(eventList);
            if (null != first && first.getKey().isReady()) {
                doPolling();
            }
        }
    }

    private void doScanAndCleaning() {
        while (isRunning()) {
            eventList.removeIf(eventPair -> {
                PollableEvent pollableEvent = eventPair.getKey();
                return pollableEvent.isFinished();
            });

            if (cleanerScanningInterval > 0) {
                try {
                    Thread.sleep(cleanerScanningInterval);
                } catch (InterruptedException e) {
                    LOGGER.error("AbstractEventPoller interrupted when scanning cleanable event from eventList", e);
                }
            }
        }
    }

    protected void beforeShutdown(List<Pair<PollableEvent, PollableEventListener>> eventList) {
        // do something here if subclass need
    }

    @Override
    protected void shutdownInternal() {
        beforeShutdown(eventList);
        // help GC
        eventList.clear();
    }

    /**
     * async invoke event listener when event is ready if this {@link ExecutorService} is not null.
     * @return      {@link ExecutorService}
     */
    protected ExecutorService getExecutorService() {
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                getThreadFactory());
    }

    @Override
    protected Runnable getDaemonService() {
        return this::doScanAndCleaning;
    }
}
