package group.liquido.databuffer.core.common;

import group.liquido.databuffer.core.DataBuffer;
import group.liquido.databuffer.core.util.GenericTypeUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author vinfer
 * @date 2022-12-06 11:18
 */
public class DelegateDataBuffer implements DataBuffer {

    private Collection<?> buffers;

    private String key;

    private int size;

    private static Field buffersField;

    public static <T> DataBuffer ofKeyCollection(String key, Collection<T> buffers) {
        Assert.hasText(key, "buffer's key must has text");
        Assert.notEmpty(buffers, "buffer data must not empty");

        DelegateDataBuffer dataBuffer = new DelegateDataBuffer();
        dataBuffer.buffers = buffers;
        dataBuffer.key = key;
        dataBuffer.size = buffers.size();
        return dataBuffer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Collection<T> get() {
        return (Collection<T>) buffers;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> type() {
        if (null == buffersField) {
            try {
                buffersField = DelegateDataBuffer.class.getDeclaredField("buffers");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return (Class<T>) GenericTypeUtils.getFieldGenericType(buffersField);
    }
}
