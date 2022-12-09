package group.liquido.databuffer.core;

import group.liquido.databuffer.core.event.BufferFlushEvent;

/**
 * @author vinfer
 * @date 2022-12-06 11:13
 */
public interface BufferFlushEventFactory {

    /**
     * create a buffer flush event with {@link DataBuffer}
     * @param dataBuffer        {@link DataBuffer}, must not null
     * @return                  {@link BufferFlushEvent}
     */
    BufferFlushEvent createBufferFlushEvent(DataBuffer dataBuffer);

}
