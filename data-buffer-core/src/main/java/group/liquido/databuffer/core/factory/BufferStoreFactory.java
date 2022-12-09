package group.liquido.databuffer.core.factory;

import group.liquido.databuffer.core.BufferStore;
import group.liquido.databuffer.core.common.InnerSupportStoreType;

import java.util.Map;

/**
 * @author vinfer
 * @date 2022-12-08 11:29
 */
public interface BufferStoreFactory {

    /**
     * create a {@code BufferStore} with specified store type and its conf metadata.
     * @param storeType     {@link InnerSupportStoreType}, must not null
     * @param confMeta      configuration metadata, requires by {@code BufferStore}'s creation
     * @return              {@link BufferStore}
     */
    BufferStore createBufferStore(InnerSupportStoreType storeType, Map<String, Object> confMeta);

    /**
     * create a {@code BufferStore} with string store type and its conf metadata.
     * @param storeType     string store type, if not match any from {@link InnerSupportStoreType}, use {@link InnerSupportStoreType#MONGO} as default store type.
     * @param confMeta      configuration metadata, requires by {@code BufferStore}'s creation
     * @return              {@link BufferStore}
     */
    default BufferStore createBufferStore(String storeType, Map<String, Object> confMeta) {
        return createBufferStore(InnerSupportStoreType.convert(storeType), confMeta);
    }

}
