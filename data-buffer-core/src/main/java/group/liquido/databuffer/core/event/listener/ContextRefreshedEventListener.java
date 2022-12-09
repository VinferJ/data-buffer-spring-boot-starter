package group.liquido.databuffer.core.event.listener;

import cn.hutool.core.collection.CollectionUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * @author vinfer
 * @date 2022-12-08 10:50
 */
public class ContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    private final List<DelegateCtxRefreshedEventListener> delegateCtxRefreshedEventListeners;

    public ContextRefreshedEventListener(List<DelegateCtxRefreshedEventListener> delegateCtxRefreshedEventListeners) {
        this.delegateCtxRefreshedEventListeners = delegateCtxRefreshedEventListeners;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (CollectionUtil.isNotEmpty(delegateCtxRefreshedEventListeners)) {
            for (DelegateCtxRefreshedEventListener eventListener : delegateCtxRefreshedEventListeners) {
                eventListener.onContextRefresh(event);
            }
        }
    }


}
