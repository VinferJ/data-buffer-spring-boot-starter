package group.liquido.databuffer.core.factory;

import java.util.concurrent.ThreadFactory;

/**
 * @author vinfer
 * @date 2022-12-06 16:22
 */
public class DelegateThreadFactory implements ThreadFactory {

    private String threadName = "DelegateWorker";

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public Thread createThread(String threadName, Runnable r) {
        Thread thread = newThread(r);
        thread.setName(threadName);
        if (null != uncaughtExceptionHandler) {
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
        return thread;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, threadName);
    }

}
