package group.liquido.databuffer.core.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author vinfer
 * @date 2022-12-13 11:21
 */
public class KeyMonitorSafeOperationProvider {

    private final Map<String, SafeReadWrite> operatorMap = new ConcurrentHashMap<>();

    private final KeyMonitorSafeReadWriteFactory safeReadWriteFactory;

    public KeyMonitorSafeOperationProvider(KeyMonitorSafeReadWriteFactory safeReadWriteFactory) {
        this.safeReadWriteFactory = safeReadWriteFactory;
    }

    public KeyMonitorSafeOperationProvider() {
        this(new SynchronizedSafeReadWriteFactory());
    }

    public  <T> T safeRead(String monitorKey, Supplier<T> readOperation) {
        SafeReadWrite safeReadWrite = getOperationDelegator(monitorKey);
        return safeReadWrite.safeRead(readOperation);
    }

    public void safeWrite(String monitorKey, Runnable writeOperation) {
        SafeReadWrite safeReadWrite = getOperationDelegator(monitorKey);
        safeReadWrite.safeWrite(writeOperation);
    }

    private SafeReadWrite getOperationDelegator(String key) {
        SafeReadWrite safeReadWrite = operatorMap.get(key);
        if (null == safeReadWrite) {
            safeReadWrite = safeReadWriteFactory.createSafeReadWrite(key);
            operatorMap.put(key, safeReadWrite);
        }
        return safeReadWrite;
    }

}
