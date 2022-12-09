package group.liquido.databuffer.core;

import java.util.Collection;

/**
 * data buffer layer, a facade interface of {@link BufferStore} and {@link BufferFlushListenerRegistry}, providing common data buffer required function,
 * accepting buffers data from any buffer producer, and also maintains {@link BufferStore}, {@link BufferFlushListenerRegistry}
 * and manages buffer flush event's creation.
 *
 * <p> those data buffers producing business service, should only communicate with this layer interface
 *
 * @author vinfer
 * @date 2022-12-06 11:12
 */
public interface DataBufferLayer extends OpenClosable {

    /**
     * put a bufferKey buffer collection to this layer.
     * @param bufferKey             buffer's business key, must be unique and not empty
     * @param bufferCollection      buffer collection, must not empty
     * @param <T>                   collection's element's type
     */
    <T> void putKeyBuffers(String bufferKey, Collection<T> bufferCollection);

    /**
     * register a listener with buffer key, do nothing if listener already exists.
     * @param bufferKey     buffer key, ref: {@link DataBuffer#key()}, identify this listener and which key type's data buffer this listener should listen on
     * @param bufferType    buffer type listening on
     * @param listener      {@link BufferFlushListener}
     */
    <T> void registerListener(String bufferKey, BufferFlushListener listener, Class<T> bufferType);

    /**
     * set consume buffer size, when accumulated buffers size reached this {@code consumeBufferSize}, there will be another component
     * to publish a flush event to notify all listeners waiting to consume these buffers.
     * @param consumeBufferSize     buffer size, must be greater than 0
     */
    void setConsumeBufferSize(int consumeBufferSize);

    /**
     * get consumer buffer size
     * @return      consumer buffer size
     */
    int getConsumeBufferSize();

    /**
     * set maximum waiting time for data buffers flushing.
     * @param maxWaitForFlushing    maximum waiting time milliseconds
     */
    void setMaxWaitForFlushing(long maxWaitForFlushing);

    /**
     * get data buffers flushing maximum waiting time.
     * <p> after waiting for this time, the buffer items will be consumed no matter they are accumulated to the amount of {@code consumeBufferSize} or not.
     * @return      maximum waiting time milliseconds
     */
    long getMaxWaitForFlushing();

    /**
     * get this layer's buffer store
     * @return      {@link BufferStore}
     */
    BufferStore getBufferStore();

    /**
     * get this layer's listener registry
     * @return      {@link BufferFlushListenerRegistry}
     */
    BufferFlushListenerRegistry getBufferFlushListenerRegistry();

    /**
     * get this layer's {@link BufferFlushEventFactory}
     * @return      {@link BufferFlushEventFactory}
     */
    BufferFlushEventFactory getBufferFlushEventFactory();

}
