package group.liquido.databuffer.core.advised;

import cn.hutool.core.lang.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * @author vinfer
 * @date 2022-12-08 14:32
 */
public abstract class AbstractMethodAdvised {

    static class MethodAdvisedContext {

        private final ProceedingJoinPoint pjp;
        private final Method advisedMethod;
        private final Object[] args;
        private final Parameter[] parameters;
        private final Class<?> proxyClass;
        private final Object proxyTarget;

        public MethodAdvisedContext(ProceedingJoinPoint pjp) {
            this.pjp = pjp;
            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            this.advisedMethod = methodSignature.getMethod();
            this.args = pjp.getArgs();
            this.parameters = advisedMethod.getParameters();
            this.proxyClass = advisedMethod.getDeclaringClass();
            this.proxyTarget = pjp.getTarget();
        }

        public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationType) {
            return advisedMethod.getDeclaredAnnotation(annotationType);
        }

        public <T> T searchArgByParamName(String paramName, Class<T> requiredType) {
            Pair<Integer, Parameter> parameterPair = searchParameterByName(paramName);
            return getArgByParamPair(requiredType, parameterPair);
        }

        public Pair<Integer, Parameter> searchParameterByName(String paramName) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.getName().equals(paramName)) {
                    return Pair.of(i, parameter);
                }
            }
            return null;
        }

        public <T> T searchArgByAnnotationMark(Class<? extends Annotation> markedAnnotationType, Class<T> requiredType) {
            Pair<Integer, Parameter> parameterPair = searchParameterByAnnotationMark(markedAnnotationType);
            return getArgByParamPair(requiredType, parameterPair);
        }

        public Pair<Integer, Parameter> searchParameterByAnnotationMark(Class<? extends Annotation> markedAnnotationType) {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.isAnnotationPresent(markedAnnotationType)) {
                    return Pair.of(i, parameter);
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public <T> T getArgByParamPair(Class<T> requiredType, Pair<Integer, Parameter> parameterPair) {
            if (null != parameterPair) {
                Integer parameterIndex = parameterPair.getKey();
                Parameter parameter = parameterPair.getValue();
                if (requiredType.isAssignableFrom(parameter.getType())) {
                    return (T) args[parameterIndex];
                }
            }
            return null;
        }

        public String getMethodName() {
            return advisedMethod.getName();
        }

        public String getFullClassifyMethodName() {
            return proxyClass.getName() + "#" + getMethodName();
        }

        public Class<?> getMethodReturnType() {
            return advisedMethod.getReturnType();
        }

        public boolean isMethodReturnVoid() {
            Class<?> methodReturnType = getMethodReturnType();
            return methodReturnType.isAssignableFrom(void.class) || methodReturnType.isAssignableFrom(Void.class);
        }

        public ProceedingJoinPoint getPjp() {
            return pjp;
        }

        public Method getAdvisedMethod() {
            return advisedMethod;
        }

        public Object[] getArgs() {
            return args;
        }

        public Parameter[] getParameters() {
            return parameters;
        }

        public Class<?> getProxyClass() {
            return proxyClass;
        }

        public Object getProxyTarget() {
            return proxyTarget;
        }

        @Override
        public String toString() {
            return "MethodAdvisedContext{" +
                    "advisedMethod=" + advisedMethod +
                    ", args=" + Arrays.toString(args) +
                    ", parameters=" + Arrays.toString(parameters) +
                    ", proxyClass=" + proxyClass +
                    ", proxyTarget=" + proxyTarget +
                    '}';
        }
    }

    protected MethodAdvisedContext getAdvisedContext(ProceedingJoinPoint pjp) {
        return new MethodAdvisedContext(pjp);
    }

}
