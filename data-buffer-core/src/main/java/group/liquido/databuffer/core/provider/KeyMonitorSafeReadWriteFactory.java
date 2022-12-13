package group.liquido.databuffer.core.provider;

/**
 * @author vinfer
 * @date 2022-12-13 11:40
 */
public interface KeyMonitorSafeReadWriteFactory {

    /**
     * create an instance of {@link SafeReadWrite} with a monitor key
     * @param monitorKey        monitor key
     * @return                  {@link SafeReadWrite}
     */
    SafeReadWrite createSafeReadWrite(String monitorKey);

}
