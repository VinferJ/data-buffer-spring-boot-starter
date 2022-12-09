package group.liquido.databuffer.core.epoll;

import group.liquido.databuffer.core.common.ServiceThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * abstract of a runnable stateful service.
 * @author vinfer
 * @date 2022-12-06 14:51
 */
public abstract class AbstractStatefulService extends ServiceThread implements StatefulService, Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStatefulService.class);

    private static final int STATE_NEW = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_TERMINATED = 2;
    private static final int STATE_EXITED = -1;

    private final AtomicInteger state = new AtomicInteger(STATE_NEW);

    /**
     * start in internal by subclass.
     * @throws Throwable    if any error occurs when start internal
     */
    protected abstract void startInternal() throws Throwable;

    /**
     * shutdown this service in internal by subclass.
     */
    protected abstract void shutdownInternal();

    /**
     * listening on internal start error.
     * @param t     error details
     */
    protected void onStartInternalError(Throwable t) {
        state.set(STATE_EXITED);
    }

    @Override
    public boolean isRunning() {
        return state.get() == STATE_RUNNING;
    }

    @Override
    public boolean isTerminated() {
        return state.get() == STATE_TERMINATED;
    }

    @Override
    public boolean isExited() {
        return state.get() == STATE_EXITED;
    }

    @Override
    public int getState() {
        return state.get();
    }

    public void startService() {
        if (isRunning()) {
            return;
        }

        if (state.compareAndSet(STATE_NEW, STATE_RUNNING)) {
            try {
                startInternal();
            }catch (Throwable t) {
                LOGGER.error("AbstractStatefulService startService error occurs when do internal start, this service is about to exit now", t);
                onStartInternalError(t);
            }
        }else {
            LOGGER.warn("AbstractStatefulService startService found illegal state {} when try to start, the correct state should be {}", getState(), STATE_NEW);
        }
    }

    public void shutdownService() {
        if (isTerminated() || isExited()) {
            LOGGER.warn("AbstractStatefulService shutdownService service has been shutdown or exited already");
            return;
        }

        if (state.compareAndSet(STATE_RUNNING, STATE_TERMINATED)) {
            shutdownInternal();
        }else {
            LOGGER.warn("AbstractStatefulService shutdownService found illegal state {} when try to shutdown, the correct state should be {}", getState(), STATE_RUNNING);
        }
    }

    @Override
    public void run() {
        startService();
    }

    @Override
    protected Runnable getDaemonService() {
        return null;
    }
}
