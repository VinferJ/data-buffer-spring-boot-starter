package group.liquido.databuffer.core.advised;

import cn.hutool.core.lang.Pair;
import group.liquido.databuffer.core.BufferFlushListener;
import group.liquido.databuffer.core.DataBufferLayer;
import group.liquido.databuffer.core.annotation.BufferListener;
import group.liquido.databuffer.core.annotation.BufferKey;
import group.liquido.databuffer.core.annotation.Buffers;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author vinfer
 * @date 2022-12-06 11:02
 */
@Aspect
public abstract class AbstractBufferListenerAdvised extends AbstractMethodAdvised {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBufferListenerAdvised.class);

    static class BufferConsumeAdvisedContext extends MethodAdvisedContext{

        private int bufferKeyArgIndex;
        private int buffersArgIndex;

        public BufferConsumeAdvisedContext(ProceedingJoinPoint pjp) {
            super(pjp);
        }

        public int getBufferKeyArgIndex() {
            return bufferKeyArgIndex;
        }

        public void setBufferKeyArgIndex(int bufferKeyArgIndex) {
            this.bufferKeyArgIndex = bufferKeyArgIndex;
        }

        public int getBuffersArgIndex() {
            return buffersArgIndex;
        }

        public void setBuffersArgIndex(int buffersArgIndex) {
            this.buffersArgIndex = buffersArgIndex;
        }

        @Override
        public String toString() {
            return "BufferConsumeAdvisedContext{" +
                    "bufferKeyArgIndex=" + bufferKeyArgIndex +
                    ", buffersArgIndex=" + buffersArgIndex +
                    "} " + super.toString();
        }
    }

    @Pointcut("@annotation(group.liquido.databuffer.core.annotation.BufferListener)")
    public void pointcut() {}

    /**
     * get a dataBufferLayer, all service depend on this layer
     * @return      {@link DataBufferLayer}
     */
    protected abstract DataBufferLayer getDataBufferLayer();

    @Around("pointcut()")
    protected Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        BufferConsumeAdvisedContext advisedContext = createBufferConsumeContext(pjp);

        if (conditionMetInterception(advisedContext)) {
            doAdvised(advisedContext);
        }else {
            LOGGER.warn("AbstractBufferConsumerAdvised doAround current condition is not met to do buffer-consume proxy for this method, joint point will be proceed now" +
                    "current method name is: {} , you can adjust it if necessary", advisedContext.getFullClassifyMethodName());
            return pjp.proceed();
        }

        // must proxy on a void return type method
        return null;
    }

    protected BufferConsumeAdvisedContext createBufferConsumeContext(ProceedingJoinPoint pjp) {
        return new BufferConsumeAdvisedContext(pjp);
    }

    protected boolean conditionMetInterception(MethodAdvisedContext advisedContext) {
        // check return type
        if (!advisedContext.isMethodReturnVoid()) {
            LOGGER.info("AbstractBufferConsumerAdvised conditionMetInterception only do buffer-consumer proxy for void return type method, current method return type is [{}]", advisedContext.getMethodReturnType());
            return false;
        }

        // check parameter, only support the method declaring two parameters now,
        // one is [bufferKey] with String type, one is [buffers] with Collection type
        // TODO: 2022/12/8 support any length parameter declaration
        int supportLen = 2;
        Parameter[] parameters = advisedContext.getParameters();
        if (parameters.length != supportLen) {
            LOGGER.info("AbstractBufferConsumerAdvised conditionMetInterception only support two parameters method now, current method parameter's count is {}", parameters.length);
            return false;
        }

        boolean withStringType = false;
        boolean withCollectionType = false;
        for (Parameter parameter : parameters) {
            Class<?> parameterType = parameter.getType();
            if (String.class.isAssignableFrom(parameterType)) {
                withStringType = true;
            }
            if (Collection.class.isAssignableFrom(parameterType)) {
                withCollectionType = true;
            }
        }

        if (!(withStringType && withCollectionType)) {
            LOGGER.info("AbstractBufferConsumerAdvised conditionMetInterception can not found a parameter with string type and another with collection type, parameters details: {}", Arrays.toString(parameters));
            return false;
        }

        // check dataBufferLayer
        if (getDataBufferLayer() == null) {
            LOGGER.info("AbstractBufferConsumerAdvised conditionMetInterception get dataBufferLayer but return null, make sure DataBufferLayer was opened");
            return false;
        }

        return true;
    }

    protected void doAdvised(BufferConsumeAdvisedContext advisedContext) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("AbstractBufferConsumerAdvised doAround method call will be cancel by advised and it will be processed by DataBufferLayer, current method name {}", advisedContext.getFullClassifyMethodName());
        }

        // get buffer key and buffers data from args
        BufferListener bufferListener = advisedContext.getDeclaredAnnotation(BufferListener.class);
        Pair<Class<?>, Collection<?>> buffersPair = resolveBuffers(advisedContext, bufferListener.buffers());
        String bufferKey = resolveBufferKey(advisedContext, bufferListener.bufferKey());
        Collection<?> buffers = buffersPair.getValue();
        Class<?> bufferType = buffersPair.getKey();

        // put buffers and register listener for proxy method
        putBuffersAndRegisterListener(bufferKey, buffers, bufferType, advisedContext);
    }

    protected String resolveBufferKey(BufferConsumeAdvisedContext advisedContext, String bufferKey) {
        Pair<Integer, Parameter> parameterPair = null;

        if (StringUtils.hasText(bufferKey)) {
            parameterPair = advisedContext.searchParameterByName(bufferKey);
        }else {
            parameterPair = advisedContext.searchParameterByAnnotationMark(BufferKey.class);
        }

        String bufferKeyVal = advisedContext.getArgByParamPair(String.class, parameterPair);
        advisedContext.setBufferKeyArgIndex(parameterPair.getKey());
        return bufferKeyVal;
    }

    protected Pair<Class<?>, Collection<?>> resolveBuffers(BufferConsumeAdvisedContext advisedContext, String buffers) {
        Pair<Integer, Parameter> parameterPair = null;

        if (StringUtils.hasText(buffers)) {
            parameterPair = advisedContext.searchParameterByName(buffers);
        }else {
            parameterPair = advisedContext.searchParameterByAnnotationMark(Buffers.class);
        }

        Class<?> bufferType = parameterPair.getValue().getType();
        Collection<?> buffersCollection = advisedContext.getArgByParamPair(Collection.class, parameterPair);
        advisedContext.setBuffersArgIndex(parameterPair.getKey());
        return Pair.of(bufferType, buffersCollection);
    }

    private void putBuffersAndRegisterListener(String bufferKey,
                                               Collection<?> buffers,
                                               Class<?> bufferType,
                                               BufferConsumeAdvisedContext advisedContext) {
        DataBufferLayer dataBufferLayer = getDataBufferLayer();
        // put buffers to bufferLayer
        dataBufferLayer.putKeyBuffers(bufferKey, buffers);

        // register listener if not registered
        if (!dataBufferLayer.getBufferFlushListenerRegistry().containsListener(bufferKey)) {
            BufferFlushListener listener = dataBuffer -> {
                String key = dataBuffer.key();
                Collection<?> collection = dataBuffer.get();
                Object[] finalInvokeArgs = new Object[advisedContext.getArgs().length];
                int bufferKeyArgIndex = advisedContext.getBufferKeyArgIndex();
                int buffersArgIndex = advisedContext.getBuffersArgIndex();
                finalInvokeArgs[bufferKeyArgIndex] = key;
                finalInvokeArgs[buffersArgIndex] = resolveBuffersArg(collection, advisedContext.getParameters()[buffersArgIndex].getType());

                // proceed joint point
                try {
                    advisedContext.getPjp().proceed(finalInvokeArgs);
                }catch (Throwable t) {
                    // TODO: 2022/12/8 transfer a errorHandler by BufferConsumer
                    LOGGER.error("BufferFlushListener onBufferFlush error occurs when proceeding a BufferConsumer with proxy method name ["+advisedContext.getFullClassifyMethodName()+"]", t);
                }
            };
            dataBufferLayer.registerListener(bufferKey, listener, bufferType);
        }
    }

    protected Object resolveBuffersArg(Collection<?> buffers, Class<?> paramType) {
        if (paramType.isAssignableFrom(Collection.class)) {
            return buffers;
        }else if (paramType.isAssignableFrom(List.class)) {
            if (buffers instanceof List) {
                return (List<?>)buffers;
            }
            return new ArrayList<>(buffers);
        }else if (paramType.isAssignableFrom(Set.class)) {
            if (buffers instanceof Set) {
                return (Set<?>)buffers;
            }
            return new LinkedHashSet<>(buffers);
        }

        return null;
    }

}
