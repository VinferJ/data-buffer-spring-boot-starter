package group.liquido.databuffer.core.provider;

import java.util.function.Supplier;

/**
 * @author vinfer
 * @date 2022-12-13 11:43
 */
public class SynchronizedSafeReadWriteFactory implements KeyMonitorSafeReadWriteFactory{

    static class SynchronizedSafeReadWrite implements SafeReadWrite {
        private final String monitor;

        public SynchronizedSafeReadWrite(String monitor) {
            this.monitor = monitor;
        }

        public void safeWrite(Runnable operation) {
            synchronized (monitor) {
                operation.run();
            }
        }

        public <T> T safeRead(Supplier<T> operation) {
            synchronized (monitor) {
                return operation.get();
            }
        }
    }

    @Override
    public SafeReadWrite createSafeReadWrite(String monitorKey) {
        return new SynchronizedSafeReadWrite(monitorKey);
    }

}
