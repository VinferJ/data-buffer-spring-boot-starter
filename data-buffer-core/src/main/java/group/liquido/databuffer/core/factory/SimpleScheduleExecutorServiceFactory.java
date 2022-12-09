package group.liquido.databuffer.core.factory;

import org.springframework.util.Assert;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author vinfer
 * @date 2022-12-07 19:02
 */
public class SimpleScheduleExecutorServiceFactory implements ScheduleExecutorServiceFactory{

    @Override
    public ScheduledExecutorService createScheduleExecutorService(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        Assert.isTrue(corePoolSize >= 0, "SimpleScheduleExecutorServiceFactory createScheduleExecutorService corePoolSize must greater than or equals to 0");
        ThreadFactory tf = decideThreadFactory(threadFactory);
        RejectedExecutionHandler reh = decideRejectedExecutionHandler(rejectedExecutionHandler);
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory, reh);
    }

    protected ThreadFactory decideThreadFactory(ThreadFactory threadFactory) {
        return Objects.requireNonNullElse(threadFactory, Thread::new);
    }

    protected RejectedExecutionHandler decideRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        return Objects.requireNonNullElseGet(rejectedExecutionHandler, ThreadPoolExecutor.AbortPolicy::new);

    }

}
