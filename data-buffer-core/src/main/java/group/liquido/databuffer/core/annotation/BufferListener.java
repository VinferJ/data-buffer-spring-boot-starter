package group.liquido.databuffer.core.annotation;

import group.liquido.databuffer.core.BufferFlushListener;
import group.liquido.databuffer.core.BufferFlushListenerRegistry;
import group.liquido.databuffer.core.DataBufferLayer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotation for data buffer oriented consuming handle, providing a low business invasion way to register a method as a {@link BufferFlushListener}.
 *
 * <p> only effective on those methods with void return type and which declared two parameters which are one with [String] type and another with [Collection] or its subtype.
 * <p>
 *     eg: there are some methods as follow :
 *      <li> void method1(String, {@code List<?>}); </li>
 *      <li> void method2({@code Collection<?>}, String);</li>
 *      <li> int method3(String, {@code Collection<?>}); (return type is non-void) </li>
 *      <li> void method4(String, {@code Map<?, ?>}); (parameter's count is 2, but the type of one of them is not a [Collection])</li>
 *      <li> void method5(String, {@code Collection<?>}, String); (parameter's count is over 2) </li>
 *      <li> void method6(String); (parameter's count is less than 2) </li>
 *      <br>
 *     only effective on method1 and method2
 * </p>
 *
 * @author vinfer
 * @date 2022-12-06 11:02
 * @see DataBufferLayer
 * @see BufferFlushListenerRegistry
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BufferListener {

    /**
     * what {@code bufferKey} parameter's name is.
     * <p> or you can mark that parameter with {@link BufferKey}, advisement will resolve it
     * @return  bufferKey parameter's name
     */
    String bufferKey() default "";

    /**
     * what {@code dataBuffers} parameter's name is.
     * <p> or you can mark that parameter with {@link Buffers}, advisement will resolve it
     * @return  dataBuffers parameter's name
     */
    String buffers() default "";

}
