package group.liquido.databuffer.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author vinfer
 * @date 2022-12-06 11:18
 */
public class GenericTypeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericTypeUtils.class);

    public static Class<?> getSuperClassGenericType(Class<?> subClassType) {
        return getSuperClassGenericType(subClassType, 0);
    }

    /**
     * get a subclass's supper class's generic type's actual type
     * @param subClassType      subclass type
     * @param index             this generic type's index, eg: {@code SomeClass<T,R>}, if index is 0, than return T's actual type
     * @return                  super class generic type's actual type
     */
    public static Class<?> getSuperClassGenericType(Class<?> subClassType, int index) {
        Type genType = subClassType.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            LOGGER.warn("{}'s superclass has not ParameterizedType", subClassType.getSimpleName());
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            LOGGER.warn("Index {},  Size of {}'s Parameterized Type {}", index, subClassType.getSimpleName(), params.length);
            return Object.class;
        }

        if (!(params[index] instanceof Class)) {
            LOGGER.warn("{} not set the actual class on superclass generic parameter", subClassType.getSimpleName());
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    public static Class<?> getFieldGenericType(Field field) {
        return getFieldGenericType(field, 0);
    }

    /**
     * get the actual type of generic type filed
     * @param field     generic type field
     * @param index     generic type index, eg: {@code Map<K,V>}, if index is 0, than return K's actual type
     * @return          generic type's actual type
     */
    public static Class<?> getFieldGenericType(Field field, int index) {
        field.setAccessible(true);
        Type genericType = field.getGenericType();
        if (!(genericType instanceof  ParameterizedType)) {
            return field.getType();
        }
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0 || index > actualTypeArguments.length - 1) {
            return field.getType();
        }
        return (Class<?>) actualTypeArguments[index];
    }

}
