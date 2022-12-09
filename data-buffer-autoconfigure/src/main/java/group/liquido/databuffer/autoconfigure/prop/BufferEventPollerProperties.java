package group.liquido.databuffer.autoconfigure.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author vinfer
 * @date 2022-12-07 17:36
 */
@ConfigurationProperties(prefix = "liquido.data-buffer.buffer-event-poller")
public class BufferEventPollerProperties {

    /**
     * the poller will create a non-core thread for every listener.
     */
    public static final String LISTENER_WORK_MODE_THREAD = "thread";

    /**
     * the poller will use a separate ExecutorService to run all listeners.
     */
    public static final String LISTENER_WORK_MODE_POOL = "pool";

    public static class ListenerWorkerPoolConf {
        private int core = 2;
        private int max = 4;
        private long keepAlive = 1000L;
        private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public int getCore() {
            return core;
        }

        public void setCore(int core) {
            this.core = core;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public long getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(long keepAlive) {
            this.keepAlive = keepAlive;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        @Override
        public String toString() {
            return "ListenerWorkerPoolConf{" +
                    "core=" + core +
                    ", max=" + max +
                    ", keepAlive=" + keepAlive +
                    ", timeUnit=" + timeUnit +
                    '}';
        }
    }

    private String listenerWorkMode = LISTENER_WORK_MODE_POOL;

    private ListenerWorkerPoolConf listenerWorkerPool = new ListenerWorkerPoolConf();

    /**
     * register BufferEventPoller as a lifecycle bean, to make poller auto start and close with spring context's lifecycle.
     */
    private boolean enableLifecyclePoller = true;

    /**
     * make BufferEventPoller auto startup after spring context refreshed; only effective when {@code enableLifecyclePoller} is false.
     */
    private boolean autoStartup = true;

    /**
     * make BufferEventPoller run in schedule; if it's enable, you have to at least configure {@code schedulePeriod} to make it work.
     */
    private boolean schedule = false;

    /**
     * the interval between tow polling operation in BufferEventPoller.
     * <p> only effective when {@code schedule} is false
     */
    private long pollInterval = 300L;

    /**
     * interval between twice scanning operations of the cleaner which is responsible for removing the invalid event of BufferEventPoller; the timeunit is millisecond.
     */
    private long cleanerScanningInterval = 30 * 1000L;

    /**
     * how long the poller delay start, time unit is millisecond.
     */
    private long scheduleDelay = 0L;

    /**
     * scheduling period, time unit is millisecond.
     */
    private Long schedulePeriod;

    public String getListenerWorkMode() {
        return listenerWorkMode;
    }

    public void setListenerWorkMode(String listenerWorkMode) {
        this.listenerWorkMode = listenerWorkMode;
    }

    public ListenerWorkerPoolConf getListenerWorkerPool() {
        return listenerWorkerPool;
    }

    public void setListenerWorkerPool(ListenerWorkerPoolConf listenerWorkerPool) {
        this.listenerWorkerPool = listenerWorkerPool;
    }

    public boolean isEnableLifecyclePoller() {
        return enableLifecyclePoller;
    }

    public void setEnableLifecyclePoller(boolean enableLifecyclePoller) {
        this.enableLifecyclePoller = enableLifecyclePoller;
    }

    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    public boolean isSchedule() {
        return schedule;
    }

    public void setSchedule(boolean schedule) {
        this.schedule = schedule;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public long getCleanerScanningInterval() {
        return cleanerScanningInterval;
    }

    public void setCleanerScanningInterval(long cleanerScanningInterval) {
        this.cleanerScanningInterval = cleanerScanningInterval;
    }

    public long getScheduleDelay() {
        return scheduleDelay;
    }

    public void setScheduleDelay(long scheduleDelay) {
        this.scheduleDelay = scheduleDelay;
    }

    public Long getSchedulePeriod() {
        return schedulePeriod;
    }

    public void setSchedulePeriod(Long schedulePeriod) {
        this.schedulePeriod = schedulePeriod;
    }

    @Override
    public String toString() {
        return "BufferEventPollerProperties{" +
                "listenerWorkMode='" + listenerWorkMode + '\'' +
                ", workerPool=" + listenerWorkerPool +
                ", enableLifecyclePoller=" + enableLifecyclePoller +
                ", autoStartup=" + autoStartup +
                ", schedule=" + schedule +
                ", pollInterval=" + pollInterval +
                ", cleanerScanningInterval=" + cleanerScanningInterval +
                ", scheduleDelay=" + scheduleDelay +
                ", schedulePeriod=" + schedulePeriod +
                '}';
    }
}
