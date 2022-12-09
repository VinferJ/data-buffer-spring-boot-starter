package group.liquido.databuffer.core.factory;

import group.liquido.databuffer.core.common.ExecutorServiceConf;

import java.util.concurrent.ExecutorService;

/**
 * @author vinfer
 * @date 2022-12-07 18:36
 */
public interface ExecutorServiceFactory {

    default ExecutorService getDefaultExecutorService() {
        return createExecutorService(ExecutorServiceConf.DEFAULT);
    }

    ExecutorService createExecutorService(ExecutorServiceConf conf);

}
