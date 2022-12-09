package group.liquido.databuffer.core.factory;

import group.liquido.databuffer.core.BufferFlushEventFactory;
import group.liquido.databuffer.core.DataBuffer;
import group.liquido.databuffer.core.event.ApplicationBufferFlushEvent;
import group.liquido.databuffer.core.event.BufferFlushEvent;
import org.springframework.util.Assert;

/**
 * @author vinfer
 * @date 2022-12-06 18:52
 */
public class ApplicationBufferFlushEventFactory implements BufferFlushEventFactory {
    @Override
    public BufferFlushEvent createBufferFlushEvent(DataBuffer dataBuffer) {
        Assert.notNull(dataBuffer, "ApplicationBufferFlushEventFactory createBufferFlushEvent dataBuffer must not null");
        return new ApplicationBufferFlushEvent(dataBuffer);
    }
}
