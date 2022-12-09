package group.liquido.databuffer.autoconfigure.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * data-buffer service configuration properties.
 * @author vinfer
 * @date 2022-12-06 10:57
 */
@ConfigurationProperties(prefix = "liquido.data-buffer")
public class DataBufferProperties {

    /**
     * how many data items in one {@code DataBuffer} item.
     */
    private int consumeBufferSize = 400;

    /**
     * maximum waiting time for data buffers flushing.
     * <p> after waiting for this time, the buffer items will be consumed no matter they are accumulated to the amount of {@code consumeBufferSize} or not.
     * <p> timeunit is millisecond, default is 5min
     */
    private long maxWaitForFlushing = 5 * 60 * 1000L;

    private BufferStoreProperties bufferStore;

    private BufferEventPollerProperties bufferEventPoller;

    public BufferStoreProperties getBufferStore() {
        return bufferStore;
    }

    public void setBufferStore(BufferStoreProperties bufferStore) {
        this.bufferStore = bufferStore;
    }

    public BufferEventPollerProperties getBufferEventPoller() {
        return bufferEventPoller;
    }

    public void setBufferEventPoller(BufferEventPollerProperties bufferEventPoller) {
        this.bufferEventPoller = bufferEventPoller;
    }

    public int getConsumeBufferSize() {
        return consumeBufferSize;
    }

    public void setConsumeBufferSize(int consumeBufferSize) {
        this.consumeBufferSize = consumeBufferSize;
    }

    public long getMaxWaitForFlushing() {
        return maxWaitForFlushing;
    }

    public void setMaxWaitForFlushing(long maxWaitForFlushing) {
        this.maxWaitForFlushing = maxWaitForFlushing;
    }

    @Override
    public String toString() {
        return "DataBufferProperties{" +
                "bufferSize=" + consumeBufferSize +
                ", maxWaitForFlushing=" + maxWaitForFlushing +
                ", bufferStore=" + bufferStore +
                ", bufferEventPoller=" + bufferEventPoller +
                '}';
    }
}
