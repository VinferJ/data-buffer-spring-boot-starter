package group.liquido.databuffer.core.common;

import java.util.Arrays;

/**
 * @author vinfer
 * @date 2022-12-08 11:30
 */
public enum InnerSupportStoreType {

    /**
     * use a mongo data source to create {@code BufferStore}
     */
    MONGO,

    /**
     * use file access service to create {@code BufferStore}
     * <p> TODO: 2022/12/7  not implements yet
     */
    FILE,

    ;

    /**
     * convert a store type string to store type enum.
     * <p> if no matched in all store type enums, return {@link InnerSupportStoreType#MONGO} as default
     * @param storeType     store type string
     * @return              {@link InnerSupportStoreType}
     */
    public static InnerSupportStoreType convert(String storeType) {
        return Arrays.stream(InnerSupportStoreType.values())
                .filter(st -> st.name().equalsIgnoreCase(storeType))
                .findFirst()
                .orElse(InnerSupportStoreType.MONGO);
    }

}
