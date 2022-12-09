package group.liquido.databuffer.core;

import java.util.Collection;
import java.util.List;

/**
 * @author vinfer
 * @date 2022-12-06 12:00
 */
public interface BufferStore {

    /**
     * set buffer size of this buffer store, how many buffer items should be token when {@link #fetchBuffers(String, Class)}
     * @param bufferSize    buffer size, must be greater than 0
     */
    void setBufferSize(int bufferSize);

    /**
     * get buffer size of this buffer store.
     * @return      buffer size, must be greater than 0
     */
    int getBufferSize();

    /**
     * save buffer items and make them accumulate in this component.
     * <p> {@code bufferItems} can contain any size of element
     * @param bufferKey     buffer's unique key
     * @param bufferItems   buffer items, {@link DataBuffer#get()}, should be not empty
     * @param <T>           buffer items' type
     */
    <T> void storeBuffers(String bufferKey, Collection<T> bufferItems);

    /**
     * count how many {@link DataBuffer#get()} elements have already accumulated.
     * <p> one {@link DataBuffer} contains many buffer item, what this function counting is this buffer item but not {@link DataBuffer} itself
     * <p> eg: if {@link DataBuffer#size()} is 400, and you have already accumulated 2 {@link DataBuffer},
     * so this {@code countBufferItem()} maybe return 400 * 2 = 800, or the number over than 800(because of keeping accumulating dataBuffer)
     * @param bufferKey     buffer's unique key
     * @return              the data buffer's item count, greater than or equals to 0
     */
    int countBufferItem(String bufferKey);

    /**
     * fetch a batch size of buffers.
     * @param bufferKey     which buffer to fetch
     * @param bufferType    buffer's concrete type
     * @param <T>           buffer type
     * @return              buffer items
     */
    <T> Collection<T> fetchBuffers(String bufferKey, Class<T> bufferType);

    /**
     * fetch the remains buffers of this buffer key
     * @param bufferKey     buffer key
     * @param bufferType    buffer's concrete type
     * @return              list of buffer items, list's element is a buffer item collection, and its size is equals to or less than {@link #getBufferSize()}
     * @param <T>           buffer item's element type
     */
    <T> List<Collection<T>> fetchAll(String bufferKey, Class<T> bufferType);

    /**
     * clean some buffers for this buffer key.
     * @param bufferKey     buffer key
     * @param size          clean size
     */
    void clearBuffers(String bufferKey, int size);

    /**
     * buffer data maybe store in a logic bucket, and maybe buffer store will create bucket for each buffer key,
     * so here providing a way to clear all those buffer buckets when the buffers are invalid.
     */
    default void clearBufferBuckets() {
        // subclass can implement this method if necessary.
    }

}
