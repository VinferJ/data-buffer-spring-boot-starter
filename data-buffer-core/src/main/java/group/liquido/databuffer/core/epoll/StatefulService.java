package group.liquido.databuffer.core.epoll;

/**
 * @author vinfer
 * @date 2022-12-06 14:55
 */
public interface StatefulService {

    /**
     * is this stateful service in running.
     * @return      true if is in running, or else false
     */
    boolean isRunning();

    /**
     * is this stateful service shutdown normally and terminated.
     * @return      true if is terminated, or else false
     */
    boolean isTerminated();

    /**
     * is this stateful service exited by occurring error.
     * @return      true if is exited, or else false
     */
    boolean isExited();

    /**
     * get state as integer.
     * @return      state code
     */
    int getState();

}
