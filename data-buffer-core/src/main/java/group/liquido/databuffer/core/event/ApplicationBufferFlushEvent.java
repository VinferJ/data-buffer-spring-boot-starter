package group.liquido.databuffer.core.event;

import group.liquido.databuffer.core.DataBuffer;
import org.springframework.context.ApplicationEvent;

/**
 * @author vinfer
 * @date 2022-12-06 14:19
 */
public class ApplicationBufferFlushEvent extends ApplicationEvent implements BufferFlushEvent{

    public ApplicationBufferFlushEvent(DataBuffer source) {
        super(source);
    }

    @Override
    public DataBuffer getDataBuffer() {
        return (DataBuffer) getSource();
    }

}
