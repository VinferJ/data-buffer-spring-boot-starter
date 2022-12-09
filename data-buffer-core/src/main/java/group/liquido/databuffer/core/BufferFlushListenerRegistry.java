package group.liquido.databuffer.core;

import org.springframework.lang.Nullable;

import java.util.Set;

/**
 * {@link BufferFlushListener}'s registry, to register and maintains these buffer listeners.
 * @author vinfer
 * @date 2022-12-06 11:12
 */
public interface BufferFlushListenerRegistry {

    /**
     * register a listener with buffer key, do nothing if listener already exists.
     * @param bufferKey     buffer key, ref: {@link DataBuffer#key()}, identify this listener and which key type's data buffer this listener should listen on
     * @param bufferType    buffer type listening on
     * @param listener      {@link BufferFlushListener}
     */
    <T> void registerListener(String bufferKey, BufferFlushListener listener, Class<T> bufferType);

    /**
     * unregister a listener with buffer key.
     * @param bufferKey     buffer key, ref: {@link DataBuffer#key()}
     */
    void unregisterListener(String bufferKey);

    /**
     * unregister all listeners from registry
     */
    default void unregisterAll() {
        for (String bufferKey : getRegisteredBufferKeys()) {
            unregisterListener(bufferKey);
        }
    }

    /**
     * check is this buffer key has a listener registered with.
     * @param bufferKey     buffer key, ref: {@link DataBuffer#key()}
     * @return              true if any, or else false
     */
    boolean containsListener(String bufferKey);

    /**
     * get all registered buffer keys
     * @return      buffer key set
     */
    Set<String> getRegisteredBufferKeys();

    /**
     * get one registered listener.
     * @param bufferKey     buffer key, ref: {@link DataBuffer#key()}
     * @return              {@link BufferFlushListener}, return null if this key has no listener registered with.
     */
    @Nullable
    BufferFlushListener getRegisteredListener(String bufferKey);

}
