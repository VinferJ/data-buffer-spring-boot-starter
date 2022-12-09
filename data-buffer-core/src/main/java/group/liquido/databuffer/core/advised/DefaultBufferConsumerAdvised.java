package group.liquido.databuffer.core.advised;

import group.liquido.databuffer.core.DataBufferLayer;
import group.liquido.databuffer.core.event.DataBufferLayerOpenEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author vinfer
 * @date 2022-12-08 16:35
 */
public class DefaultBufferConsumerAdvised extends AbstractBufferConsumerAdvised implements ApplicationListener<DataBufferLayerOpenEvent> {

    private DataBufferLayer dataBufferLayer;

    @Override
    protected DataBufferLayer getDataBufferLayer() {
        return dataBufferLayer;
    }

    @Override
    public void onApplicationEvent(DataBufferLayerOpenEvent event) {
        this.dataBufferLayer = event.getDataBufferLayer();
    }

}
