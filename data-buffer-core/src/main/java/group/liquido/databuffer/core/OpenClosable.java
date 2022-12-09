package group.liquido.databuffer.core;

/**
 * @author vinfer
 * @date 2022-12-07 16:28
 */
public interface OpenClosable {

    /**
     * means to open some operations or services, resources.
     */
    void open();

    /**
     * means to close some operations or services, resources.
     */
    void close();

}
