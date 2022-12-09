package group.liquido.databuffer.core.event.listener;

import org.springframework.context.event.ContextClosedEvent;

/**
 * @author vinfer
 * @date 2022-12-08 10:55
 */
public interface DelegateCtxClosedEventListener {

    void onContextClosed(ContextClosedEvent event);

}
