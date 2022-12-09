package group.liquido.databuffer.core.common;

/**
 * @author vinfer
 * @date 2022-12-06 18:13
 */
public interface TaskRunner {

    /**
     * run this task
     * @param task  {@link Runnable}
     */
    void run(Runnable task);

    /**
     * run a task with schedule at fixed rate.
     * @param task          task
     * @param delayMill     delay start after this milliseconds
     * @param periodMill    running period milliseconds
     */
    void runScheduleAtFixedRate(Runnable task, long delayMill, long periodMill);

    /**
     * shutdown this task runner
     */
    void shutdown();

}
