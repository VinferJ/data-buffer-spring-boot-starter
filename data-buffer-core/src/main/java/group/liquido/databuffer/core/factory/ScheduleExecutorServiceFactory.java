package group.liquido.databuffer.core.factory;

import org.springframework.lang.Nullable;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @author vinfer
 * @date 2022-12-07 18:43
 */
public interface ScheduleExecutorServiceFactory {

    default ScheduledExecutorService getDefaultScheduleExecutorService() {
        return createScheduleExecutorService(1, null, null);
    }

    ScheduledExecutorService createScheduleExecutorService(int corePoolSize,
                                                           @Nullable ThreadFactory threadFactory,
                                                           @Nullable RejectedExecutionHandler rejectedExecutionHandler);
}
