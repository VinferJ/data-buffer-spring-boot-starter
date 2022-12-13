package group.liquido.databuffer.core.common;

/**
 * the fetch sequence cursor of data buffers, to mark the position of the buffer consumption.
 * @author vinfer
 * @date 2022-12-09 16:24
 */
public interface SequenceCursor {

    /**
     * this cursor's unique key, generally buffer key.
     * @return      cursor key
     */
    String getKey();

    /**
     * cursor's sequence no value
     * @return      sequence no value
     */
    String getSeqNo();
}
