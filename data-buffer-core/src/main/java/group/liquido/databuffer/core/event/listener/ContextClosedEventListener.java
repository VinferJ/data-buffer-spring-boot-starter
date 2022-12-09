package group.liquido.databuffer.core.event.listener;

import cn.hutool.core.collection.CollectionUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.List;

/**
 * @author vinfer
 * @date 2022-12-08 10:54
 */
public class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent> {

    private final List<DelegateCtxClosedEventListener> delegateCtxClosedEventListeners;

    public ContextClosedEventListener(List<DelegateCtxClosedEventListener> delegateCtxClosedEventListeners) {
        this.delegateCtxClosedEventListeners = delegateCtxClosedEventListeners;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (CollectionUtil.isNotEmpty(delegateCtxClosedEventListeners)) {
            for (DelegateCtxClosedEventListener eventListener : delegateCtxClosedEventListeners) {
                eventListener.onContextClosed(event);
            }
        }
    }

}
