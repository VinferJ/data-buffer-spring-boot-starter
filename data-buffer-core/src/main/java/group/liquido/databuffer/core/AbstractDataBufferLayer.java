package group.liquido.databuffer.core;

import group.liquido.databuffer.core.common.BufferConsumeStat;
import group.liquido.databuffer.core.event.DataBufferLayerCloseEvent;
import group.liquido.databuffer.core.event.DataBufferLayerOpenEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author vinfer
 * @date 2022-12-07 16:32
 */
public abstract class AbstractDataBufferLayer implements DataBufferLayer, ApplicationEventPublisherAware, InitializingBean {

    private final BufferStore bufferStore;

    private final BufferFlushListenerRegistry listenerRegistry;

    private final BufferFlushEventFactory eventFactory;

    private final AtomicBoolean openState = new AtomicBoolean(false);

    private final AtomicBoolean closeState = new AtomicBoolean(false);

    private ApplicationEventPublisher eventPublisher;

    private long maxWaitForFlushing;

    protected AbstractDataBufferLayer(BufferStore bufferStore, BufferFlushListenerRegistry listenerRegistry, BufferFlushEventFactory eventFactory) {
        Assert.notNull(bufferStore, "AbstractDataBufferLayer bufferStore must not null");
        Assert.notNull(listenerRegistry, "AbstractDataBufferLayer listenerRegistry must not null");
        Assert.notNull(eventFactory, "AbstractDataBufferLayer eventFactory must not null");

        this.bufferStore = bufferStore;
        this.listenerRegistry = listenerRegistry;
        this.eventFactory = eventFactory;
    }

    @Override
    public <T> void putKeyBuffers(String bufferKey, Collection<T> bufferCollection) {
        bufferStore.storeBuffers(bufferKey, bufferCollection);
    }

    @Override
    public <T> void registerListener(String bufferKey, BufferFlushListener listener, Class<T> bufferType) {
        listenerRegistry.registerListener(bufferKey, listener, bufferType);
    }

    @Override
    public void setConsumeBufferSize(int consumeBufferSize) {
        bufferStore.setBufferSize(consumeBufferSize);
    }

    @Override
    public int getConsumeBufferSize() {
        return bufferStore.getBufferSize();
    }

    @Override
    public void setMaxWaitForFlushing(long maxWaitForFlushing) {
        this.maxWaitForFlushing = maxWaitForFlushing;
    }

    @Override
    public long getMaxWaitForFlushing() {
        return maxWaitForFlushing;
    }

    @Override
    public BufferStore getBufferStore() {
        return bufferStore;
    }

    @Override
    public BufferFlushListenerRegistry getBufferFlushListenerRegistry() {
        return listenerRegistry;
    }

    @Override
    public BufferFlushEventFactory getBufferFlushEventFactory() {
        return eventFactory;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void open() {
        if (isOpened()) {
            return;
        }
        openState.set(true);
        eventPublisher.publishEvent(new DataBufferLayerOpenEvent(this));
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        // TODO: 2022/12/7 buffer consume info stat
        closeState.set(false);
        eventPublisher.publishEvent(new DataBufferLayerCloseEvent(new BufferConsumeStat()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        open();
    }

    protected boolean isOpened() {
        return openState.get();
    }

    protected boolean isClosed() {
        return closeState.get();
    }

}
