package group.liquido.databuffer.core;

import group.liquido.databuffer.core.common.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.LifecycleProcessor;

import java.util.concurrent.ExecutorService;

/**
 * @author vinfer
 * @date 2022-12-06 18:12
 */
public class LifecycleBufferEventPoller extends BufferEventPoller implements LifecycleProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleBufferEventPoller.class);

    private final TaskRunner taskRunner;

    private final Long delayMill;

    private final Long periodMill;

    public LifecycleBufferEventPoller(TaskRunner taskRunner, BufferStore bufferStore, int bufferSize, BufferFlushEventFactory bufferFlushEventFactory) {
        this(taskRunner, bufferStore, null);
    }

    public LifecycleBufferEventPoller(TaskRunner taskRunner, BufferStore bufferStore, ExecutorService es) {
        this(taskRunner, null, null, bufferStore, es);
    }

    public LifecycleBufferEventPoller(TaskRunner taskRunner,
                                      Long delayMill,
                                      Long periodMill,
                                      BufferStore bufferStore,
                                      ExecutorService es) {
        super(bufferStore, es);
        this.taskRunner = taskRunner;
        this.delayMill = delayMill;
        this.periodMill = periodMill;
    }

    @Override
    public void onRefresh() {
        start();
    }

    @Override
    public void onClose() {
        stop();
    }

    @Override
    public void start() {
        LOGGER.info("LifecycleBufferEventPoller start with lifecycle...");
        if (tryStartWithSchedule()) {
            return;
        }
        taskRunner.run(this);
    }

    private boolean tryStartWithSchedule() {
        if (null != delayMill && null != periodMill) {
            taskRunner.runScheduleAtFixedRate(this::startInScheduleMode, delayMill, periodMill);
            LOGGER.info("LifecycleBufferEventPoller startInScheduleMode delay {} period {}", delayMill, periodMill);
            return true;
        }
        return false;
    }

    @Override
    public void stop() {
        LOGGER.info("LifecycleBufferEventPoller stop with lifecycle, poller service will be shutdown later...");
        shutdownInternal();
        // shutdown task runner
        taskRunner.shutdown();
    }
}
