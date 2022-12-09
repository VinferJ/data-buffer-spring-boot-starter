package group.liquido.databuffer.core.common;

import group.liquido.databuffer.core.BufferFlushListener;
import group.liquido.databuffer.core.DataBuffer;

/**
 * @author vinfer
 * @date 2022-12-06 18:43
 */
public class BufferFlushListenerWrapper implements BufferFlushListener {

    private final String bufferKey;
    private final Class<?> bufferType;
    private final BufferFlushListener listener;

    public <T> BufferFlushListenerWrapper(String bufferKey, Class<T> bufferType, BufferFlushListener listener) {
        this.bufferKey = bufferKey;
        this.bufferType = bufferType;
        this.listener = listener;
    }


    @Override
    public void onBufferFlush(DataBuffer dataBuffer) {
        listener.onBufferFlush(dataBuffer);
    }

    public Class<?> getBufferType() {
        return bufferType;
    }

    public String getBufferKey() {
        return bufferKey;
    }
}
