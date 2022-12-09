package group.liquido.databuffer.core;

/**
 * @author vinfer
 * @date 2022-12-06 14:13
 */
public interface BufferFlushListener {

    /**
     * listening on data buffer flushes.
     * @param dataBuffer        {@link DataBuffer}
     */
    void onBufferFlush(DataBuffer dataBuffer);

}
