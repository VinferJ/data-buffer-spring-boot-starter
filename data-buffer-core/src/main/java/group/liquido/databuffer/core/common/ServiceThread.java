package group.liquido.databuffer.core.common;

import group.liquido.databuffer.core.factory.DelegateThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * @author vinfer
 * @date 2022-12-06 16:19
 */
public abstract class ServiceThread {

    protected ServiceThread() {
        Runnable daemonService = getDaemonService();
        if (null == daemonService) {
            return;
        }

        // create daemon and start
        Thread thread = getThreadFactory().newThread(daemonService);
        thread.setDaemon(true);
        thread.start();
    }

    protected ThreadFactory getThreadFactory() {
        DelegateThreadFactory delegateThreadFactory = new DelegateThreadFactory();
        delegateThreadFactory.setThreadName("ServiceWorker");
        return delegateThreadFactory;
    }

    /**
     * get daemon service
     * @return      daemon service
     */
    protected abstract Runnable getDaemonService();

}
