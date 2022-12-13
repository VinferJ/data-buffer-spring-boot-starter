package group.liquido.databuffer.core.common;

/**
 * @author vinfer
 * @date 2022-12-12 10:11
 */
public interface SequenceBufferRow<T> {

    /**
     * get this buffer item's sequence no.
     * @return  sequence no
     */
    String getSeqNo();

    /**
     * get buffer row data.
     * @return  buffer row
     */
    T getRow();

}
