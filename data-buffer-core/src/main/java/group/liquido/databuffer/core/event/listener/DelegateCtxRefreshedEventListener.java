package group.liquido.databuffer.core.event.listener;

import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author vinfer
 * @date 2022-12-08 10:53
 */
public interface DelegateCtxRefreshedEventListener {

    void onContextRefresh(ContextRefreshedEvent event);

}
