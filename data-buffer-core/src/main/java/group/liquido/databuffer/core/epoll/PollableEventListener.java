package group.liquido.databuffer.core.epoll;

/**
 * @author vinfer
 * @date 2022-12-06 16:01
 */
public interface PollableEventListener {

    void onEventReady(PollableEvent event);

}
