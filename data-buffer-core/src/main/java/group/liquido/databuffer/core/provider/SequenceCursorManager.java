package group.liquido.databuffer.core.provider;

import group.liquido.databuffer.core.common.SequenceBufferRow;
import group.liquido.databuffer.core.common.SequenceCursor;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author vinfer
 * @date 2022-12-12 16:57
 */
public interface SequenceCursorManager {

    /**
     * fetch a {@link SequenceCursor} of current position(sequence cursor of last consumption).
     * @param key   cursor's key
     * @return      {@link SequenceCursor}
     */
    SequenceCursor fetchCurrentCursor(String key);

    /**
     * fetch a {@link SequenceCursor} of specified position.
     * <p> eg: position is 400, will return the cursor with sequence no of 400th {@link SequenceBufferRow}
     * @param key       cursor's key
     * @param position  cursor's position
     * @return          {@link SequenceCursor}
     */
    SequenceCursor fetchPositionCursor(String key, int position);

    /**
     * get {@link SequenceCursor} of all keys.
     * @return      {@link SequenceCursor}, nullable
     */
    @Nullable
    List<? extends SequenceCursor> fetchAllSeqCursors();

    /**
     * update a {@link SequenceCursor}
     * @param key       cursor's key
     * @param seqNo     cursor's sequence no
     */
    void updateCursor(String key, String seqNo);

}
