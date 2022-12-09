package group.liquido.databuffer.core.factory;

import group.liquido.databuffer.core.common.ExecutorServiceConf;

import java.util.concurrent.ExecutorService;

/**
 * @author vinfer
 * @date 2022-12-07 18:50
 */
public class SimpleExecutorServiceFactory implements ExecutorServiceFactory {

    @Override
    public ExecutorService createExecutorService(ExecutorServiceConf conf) {
        return null;
    }

}
