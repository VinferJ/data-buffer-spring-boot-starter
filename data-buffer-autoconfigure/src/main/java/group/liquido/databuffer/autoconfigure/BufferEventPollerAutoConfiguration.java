package group.liquido.databuffer.autoconfigure;

import group.liquido.databuffer.autoconfigure.prop.BufferEventPollerProperties;
import group.liquido.databuffer.core.BufferEventPoller;
import group.liquido.databuffer.core.BufferStore;
import group.liquido.databuffer.core.LifecycleBufferEventPoller;
import group.liquido.databuffer.core.common.ExecutorServiceConf;
import group.liquido.databuffer.core.common.TaskRunner;
import group.liquido.databuffer.core.event.listener.DelegateCtxClosedEventListener;
import group.liquido.databuffer.core.event.listener.DelegateCtxRefreshedEventListener;
import group.liquido.databuffer.core.factory.*;
import group.liquido.databuffer.core.provider.DelegateTaskRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author vinfer
 * @date 2022-12-06 10:56
 */
@Import(BufferStoreAutoConfiguration.class)
@Configuration
@EnableConfigurationProperties(BufferEventPollerProperties.class)
public class BufferEventPollerAutoConfiguration {

    private final BufferEventPollerProperties bufferEventPollerProperties;

    public BufferEventPollerAutoConfiguration(BufferEventPollerProperties bufferEventPollerProperties) {
        this.bufferEventPollerProperties = bufferEventPollerProperties;
    }

    @ConditionalOnMissingBean(ExecutorServiceFactory.class)
    @Bean
    ExecutorServiceFactory executorServiceFactory() {
        return new SimpleExecutorServiceFactory();
    }

    @ConditionalOnMissingBean(ScheduleExecutorServiceFactory.class)
    @Bean
    ScheduleExecutorServiceFactory scheduleExecutorServiceFactory() {
        return new SimpleScheduleExecutorServiceFactory();
    }

    @ConditionalOnMissingBean(TaskRunner.class)
    @Bean
    TaskRunner taskRunner(ExecutorServiceFactory executorServiceFactory, ScheduleExecutorServiceFactory scheduleExecutorServiceFactory) {
        return new DelegateTaskRunner(executorServiceFactory::getDefaultExecutorService, scheduleExecutorServiceFactory::getDefaultScheduleExecutorService);
    }

    @ConditionalOnProperty(name = "liquido.data-buffer.buffer-event-poller.enable-lifecycle-poller", havingValue = "false", matchIfMissing = true)
    @ConditionalOnMissingBean({BufferEventPoller.class})
    @Bean
    BufferEventPoller bufferEventPoller(BufferStore bufferStore, ExecutorServiceFactory executorServiceFactory) {
        ExecutorService listenerExecutorService = createListenerExecutorService(executorServiceFactory);
        BufferEventPoller bufferEventPoller = new BufferEventPoller(bufferStore, listenerExecutorService);
        setProp4Poller(bufferEventPoller);
        return bufferEventPoller;
    }

    @ConditionalOnProperty(name = "liquido.data-buffer.buffer-event-poller.enable-lifecycle-poller", havingValue = "true")
    @ConditionalOnMissingBean(LifecycleBufferEventPoller.class)
    @Bean
    LifecycleBufferEventPoller lifecycleBufferEventPoller(BufferStore bufferStore, TaskRunner taskRunner, ExecutorServiceFactory executorServiceFactory) {
        ExecutorService listenerExecutorService = createListenerExecutorService(executorServiceFactory);
        LifecycleBufferEventPoller lifecycleBufferEventPoller = new LifecycleBufferEventPoller(taskRunner, bufferStore, listenerExecutorService);
        setProp4Poller(lifecycleBufferEventPoller);
        return lifecycleBufferEventPoller;
    }

    private void setProp4Poller(BufferEventPoller poller) {
        poller.setCleanerScanningInterval(bufferEventPollerProperties.getCleanerScanningInterval());
        poller.setPollingInterval(bufferEventPollerProperties.getPollInterval());
    }

    @ConditionalOnProperty(name = "liquido.data-buffer.buffer-event-poller.auto-startup", havingValue = "true")
    @Bean
    DelegateCtxRefreshedEventListener bufferEventPollerAutoStartup(BufferEventPoller bufferEventPoller, TaskRunner taskRunner) {
        return event -> {
            if (bufferEventPoller instanceof LifecycleBufferEventPoller) {
                return;
            }

            if (bufferEventPollerProperties.isSchedule()) {
                Long scheduleDelay = bufferEventPollerProperties.getScheduleDelay();
                Long schedulePeriod = bufferEventPollerProperties.getSchedulePeriod();
                taskRunner.runScheduleAtFixedRate(bufferEventPoller::startInScheduleMode, scheduleDelay, schedulePeriod);
            }else {
                DelegateThreadFactory threadFactory = new DelegateThreadFactory();
                threadFactory.createThread("BufferEventPoller", bufferEventPoller::startService).start();
            }
        };
    }

    @Bean
    DelegateCtxClosedEventListener bufferEventPollerAutoClosed(BufferEventPoller bufferEventPoller) {
        return event -> {
            if (bufferEventPoller instanceof LifecycleBufferEventPoller) {
                return;
            }
            bufferEventPoller.shutdownService();
        };
    }

    ExecutorService createListenerExecutorService(ExecutorServiceFactory executorServiceFactory) {
        DelegateThreadFactory threadFactory = new DelegateThreadFactory();
        threadFactory.setThreadName("BufferEventListener");
        ExecutorServiceConf.Builder confBuilder = ExecutorServiceConf.builder()
                .taskQueue(new LinkedBlockingQueue<>())
                .threadFactory(threadFactory);

        String listenerWorkMode = bufferEventPollerProperties.getListenerWorkMode();
        if (listenerWorkMode.equals(BufferEventPollerProperties.LISTENER_WORK_MODE_THREAD)) {
            confBuilder
                    .corePoolSize(0)
                    .maxPoolSize(Integer.MAX_VALUE)
                    .keepAliveTime(0L)
                    .timeUnit(TimeUnit.MILLISECONDS);

        }else {
            BufferEventPollerProperties.ListenerWorkerPoolConf workerPool = bufferEventPollerProperties.getListenerWorkerPool();
            int core = workerPool.getCore();
            int max = workerPool.getMax();
            long keepAlive = workerPool.getKeepAlive();
            TimeUnit timeUnit = workerPool.getTimeUnit();
            confBuilder
                    .corePoolSize(core)
                    .maxPoolSize(max)
                    .keepAliveTime(keepAlive)
                    .timeUnit(timeUnit);
        }

        return executorServiceFactory.createExecutorService(confBuilder.build());
    }

}
