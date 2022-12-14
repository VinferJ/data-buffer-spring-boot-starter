package group.liquido.databuffer.core.epoll;

import org.springframework.lang.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vinfer
 * @date 2022-12-06 15:45
 */
public abstract class PollableEvent {

    private static final int STATE_WAIT_READY = 0;
    private static final int STATE_FINISHED = 1;

    private final AtomicInteger state = new AtomicInteger(STATE_WAIT_READY);

    /**
     * check is this event is ready, this event's listeners will be invoked when event is ready.
     * @return      true if is ready, or else false
     */
    public abstract boolean isReady();

    public boolean isFinished() {
        return state.get() == STATE_FINISHED;
    }

    /**
     * finishes this event, in case reconsume in next round event polling before this event is kicked out from event list.
     */
    public void commit() {
        state.set(STATE_FINISHED);
    }

    /**
     * get event source object of this event.
     * @return      event source object, nullable
     */
    @Nullable
    public abstract Object getEventSource();

}
