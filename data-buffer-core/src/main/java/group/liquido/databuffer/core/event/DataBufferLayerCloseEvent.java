package group.liquido.databuffer.core.event;

import group.liquido.databuffer.core.common.BufferConsumeStat;

/**
 * @author vinfer
 * @date 2022-12-07 16:36
 */
public class DataBufferLayerCloseEvent extends DataBufferLayerEvent {

    public DataBufferLayerCloseEvent(BufferConsumeStat source) {
        super(source);
    }

    public BufferConsumeStat getBufferConsumeStat() {
        return (BufferConsumeStat) getSource();
    }

}
