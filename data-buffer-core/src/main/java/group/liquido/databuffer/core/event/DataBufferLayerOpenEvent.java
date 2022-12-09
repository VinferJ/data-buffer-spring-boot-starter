package group.liquido.databuffer.core.event;

import group.liquido.databuffer.core.DataBufferLayer;

/**
 * @author vinfer
 * @date 2022-12-07 16:34
 */
public class DataBufferLayerOpenEvent extends DataBufferLayerEvent {

    public DataBufferLayerOpenEvent(DataBufferLayer dataBufferLayer) {
        super(dataBufferLayer);
    }

    public DataBufferLayer getDataBufferLayer() {
        return (DataBufferLayer) getSource();
    }

}
