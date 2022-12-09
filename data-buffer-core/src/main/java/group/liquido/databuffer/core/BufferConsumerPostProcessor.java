package group.liquido.databuffer.core;

import group.liquido.databuffer.core.annotation.BufferListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vinfer
 * @date 2022-12-08 14:11
 */
public class BufferConsumerPostProcessor implements BeanPostProcessor {

    private final BufferFlushListenerRegistry listenerRegistry;

    public BufferConsumerPostProcessor(BufferFlushListenerRegistry listenerRegistry) {
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (isBufferConsumerBean(bean)) {
            List<Method> bufferConsumerMethods = Arrays.stream(bean.getClass().getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(BufferListener.class))
                    .collect(Collectors.toList());
            registerBufferConsumers(bufferConsumerMethods, bean);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    private boolean isBufferConsumerBean(Object bean) {
        return Arrays.stream(bean.getClass().getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(BufferListener.class));
    }

    private void registerBufferConsumers(List<Method> bufferConsumerMethods, Object bean) {
        for (Method bufferConsumerMethod : bufferConsumerMethods) {
            BufferListener bufferListener = bufferConsumerMethod.getDeclaredAnnotation(BufferListener.class);

        }
    }

}
