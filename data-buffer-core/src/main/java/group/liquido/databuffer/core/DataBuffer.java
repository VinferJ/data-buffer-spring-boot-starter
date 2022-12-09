package group.liquido.databuffer.core;

import java.util.Collection;

/**
 * a common interface for all data buffer object, to describe the {@link BufferFlushListener} acceptable data.
 * @author vinfer
 * @date 2022-12-06 11:14
 */
public interface DataBuffer {

    /**
     * get data buffer's collection.
     * @return      data collection, with size less than or equals to {@link #size()}
     * @param <T>   data buffer's concrete type
     */
    <T> Collection<T> get();

    /**
     * get this data buffer's unique key
     * @return      unique key string, must not empty
     */
    String key();

    /**
     * get this data buffer's size.
     * @return      buffer size, must grater than 0
     */
    int size();

    /**
     * get data buffer's concrete type.
     * @return      type class
     * @param <T>   buffer type
     */
    <T> Class<T> type();

}
