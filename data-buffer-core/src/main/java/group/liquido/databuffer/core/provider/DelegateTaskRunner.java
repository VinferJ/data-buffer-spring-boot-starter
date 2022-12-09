package group.liquido.databuffer.core.provider;

import group.liquido.databuffer.core.common.TaskRunner;
import org.springframework.util.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author vinfer
 * @date 2022-12-07 18:34
 */
public class DelegateTaskRunner implements TaskRunner {

    private final Supplier<ExecutorService> esSupplier;

    private final Supplier<ScheduledExecutorService> sesSupplier;

    private ExecutorService es;

    private ScheduledExecutorService ses;

    public DelegateTaskRunner(Supplier<ExecutorService> esSupplier, Supplier<ScheduledExecutorService> sesSupplier) {
        this.esSupplier = esSupplier;
        this.sesSupplier = sesSupplier;
    }

    @Override
    public void run(Runnable task) {
        if (es == null) {
            es = esSupplier.get();
        }
        Assert.notNull(es, "DelegateTaskRunner the executorService get from Supplier must not null");
        es.submit(task);
    }

    @Override
    public void runScheduleAtFixedRate(Runnable task, long delayMill, long periodMill) {
        if (null == ses) {
            ses = sesSupplier.get();
        }
        Assert.notNull(es, "DelegateTaskRunner the scheduledExecutorService get from Supplier must not null");
        ses.scheduleAtFixedRate(task, delayMill, periodMill, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        if (null != es) {
            es.shutdown();
        }
        if (null != ses) {
            ses.shutdown();
        }
    }

}
