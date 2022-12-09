package group.liquido.databuffer.autoconfigure.annotation;

import group.liquido.databuffer.autoconfigure.DataBufferAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * enable auto configuration for data-buffer service
 * @author vinfer
 * @date 2022-12-07 17:22
 * @see DataBufferAutoConfiguration
 */
@Import(DataBufferAutoConfiguration.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableDataBufferAutoConfiguration {
}
