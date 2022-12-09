package group.liquido.databuffer.core.event;

import group.liquido.databuffer.core.DataBuffer;

/**
 * @author vinfer
 * @date 2022-12-06 14:15
 */
public interface BufferFlushEvent {

    /**
     * get {@link DataBuffer} carried by this event.
     * @return      {@link DataBuffer}
     */
    DataBuffer getDataBuffer();

}
