package group.liquido.databuffer.core.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author vinfer
 * @date 2022-12-07 16:41
 */
public abstract class DataBufferLayerEvent extends ApplicationEvent {

    public DataBufferLayerEvent(Object source) {
        super(source);
    }

}
