package group.liquido.databuffer.core.provider;

import group.liquido.databuffer.core.AbstractDataBufferLayer;
import group.liquido.databuffer.core.BufferFlushEventFactory;
import group.liquido.databuffer.core.BufferFlushListenerRegistry;
import group.liquido.databuffer.core.BufferStore;
import group.liquido.databuffer.core.factory.ApplicationBufferFlushEventFactory;

/**
 * @author vinfer
 * @date 2022-12-07 15:12
 */
public class DataBufferLayerProvider extends AbstractDataBufferLayer {

    public DataBufferLayerProvider(BufferStore bufferStore, BufferFlushListenerRegistry listenerRegistry, BufferFlushEventFactory eventFactory) {
        super(bufferStore, listenerRegistry, eventFactory);
    }

    public DataBufferLayerProvider(BufferStore bufferStore, BufferFlushListenerRegistry listenerRegistry) {
        this(bufferStore, listenerRegistry, new ApplicationBufferFlushEventFactory());
    }

}
