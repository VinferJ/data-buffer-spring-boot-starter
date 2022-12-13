package group.liquido.databuffer.core.provider;

import java.util.function.Supplier;

/**
 * @author vinfer
 * @date 2022-12-13 11:23
 */
public interface SafeReadWrite {

    /**
     * execute a read operation safely
     * @param readOperation     read operation
     * @return                  result of read operation
     * @param <T>               result type
     */
    <T> T safeRead(Supplier<T> readOperation);

    /**
     * execute a write operation safely
     * @param writeOperation    write operation
     */
    void safeWrite(Runnable writeOperation);

}
