package group.liquido.databuffer.core.common;

import org.springframework.util.Assert;

import java.util.concurrent.*;

/**
 * @author vinfer
 * @date 2022-12-07 18:38
 */
public class ExecutorServiceConf {

    public static final ExecutorServiceConf DEFAULT = ExecutorServiceConf.builder().build();

    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;
    private TimeUnit timeUnit;
    private ThreadFactory threadFactory;
    private BlockingQueue<Runnable> taskQueue;
    private RejectedExecutionHandler rejectedExecutionHandler;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ExecutorServiceConf conf;

        public Builder() {
            conf = new ExecutorServiceConf();
            conf.setCorePoolSize(2);
            conf.setMaxPoolSize(4);
            conf.setTaskQueue(new LinkedBlockingQueue<>());
            conf.setKeepAliveTime(0L);
            conf.setTimeUnit(TimeUnit.MILLISECONDS);
            conf.setThreadFactory(Thread::new);
            conf.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        }

        public Builder corePoolSize(int corePoolSize) {
            Assert.isTrue(corePoolSize >= 0, "ExecutorServiceConf$Builder corePoolSize must be greater than or equals to 0");
            conf.setCorePoolSize(corePoolSize);
            return this;
        }

        public Builder maxPoolSize(int maxPoolSize) {
            Assert.isTrue(maxPoolSize >= 0, "ExecutorServiceConf$Builder maxPoolSize must be greater than or equals to 0");
            conf.setMaxPoolSize(maxPoolSize);
            return this;
        }

        public Builder keepAliveTime(long keepAliveTime) {
            Assert.isTrue(keepAliveTime >= 0, "ExecutorServiceConf$Builder keepAliveTime must be greater than or equals to 0");
            conf.setKeepAliveTime(keepAliveTime);
            return this;
        }

        public Builder timeUnit(TimeUnit timeUnit) {
            Assert.notNull(timeUnit, "ExecutorServiceConf$Builder timeUnit must not null");
            conf.setTimeUnit(timeUnit);
            return this;
        }

        public Builder threadFactory(ThreadFactory threadFactory) {
            Assert.notNull(threadFactory, "ExecutorServiceConf$Builder threadFactory must not null");
            conf.setThreadFactory(threadFactory);
            return this;
        }

        public Builder taskQueue(BlockingQueue<Runnable> taskQueue) {
            Assert.notNull(taskQueue, "ExecutorServiceConf$Builder taskQueue must not null");
            conf.setTaskQueue(taskQueue);
            return this;
        }

        public Builder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
            Assert.notNull(rejectedExecutionHandler, "ExecutorServiceConf$Builder rejectedExecutionHandler must not null");
            conf.setRejectedExecutionHandler(rejectedExecutionHandler);
            return this;
        }

        public ExecutorServiceConf build() {
            return conf;
        }
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public BlockingQueue<Runnable> getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    @Override
    public String toString() {
        return "ExecutorServiceConf{" +
                "corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", timeUnit=" + timeUnit +
                ", threadFactory=" + threadFactory +
                ", taskQueue=" + taskQueue +
                ", rejectedExecutionHandler=" + rejectedExecutionHandler +
                '}';
    }
}
