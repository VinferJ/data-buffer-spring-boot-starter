package group.liquido.databuffer.core;

import cn.hutool.core.collection.CollectionUtil;
import group.liquido.databuffer.core.common.SequenceBufferRow;
import group.liquido.databuffer.core.common.SequenceCursor;
import group.liquido.databuffer.core.common.SimpleSequenceBufferRow;
import group.liquido.databuffer.core.provider.SequenceCursorManager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * a buffer store providing sequence buffer store and maintains its consume cursor after buffer was fetched from store.
 * <p> when some buffers are fetched by upstream, this {@code SequenceBufferStore} treats them as data for one-time consumption,
 * there the store will maintains a cursor to record the consumption position.
 *
 * <p> so when you fetch buffers by calling {@link #fetchBuffers(String, Class)}, this method will be forced to fetch buffers by a {@link SequenceCursor},
 * you can't fetch the same buffers in twice method call because the cursor will auto move forward after the buffers was fetched.
 *
 * <p> so we provide a way {@link #fetchSeqBuffersWithCursor(String, SequenceCursor, Class)} to fetch buffers with a passable {@link SequenceCursor} to fetch any positions buffers.
 *
 * @author vinfer
 * @date 2022-12-12 14:11
 */
public interface SequenceBufferStore extends BufferStore, SequenceCursorManager {

    SequenceCursor ZERO_POS_CURSOR = new SequenceCursor() {
        @Override
        public String getKey() {
            throw new UnsupportedOperationException("only use as a flag cursor object, no key to obtain");
        }

        @Override
        public String getSeqNo() {
            return null;
        }
    };

    /**
     * delegate to {@link #storeSequenceBuffers(String, Collection)}
     * @param bufferKey     buffer's unique key
     * @param bufferItems   buffer items, {@link DataBuffer#get()}, should be not empty
     * @param <T>           buffer item's type
     */
    @Override
    default <T> void storeBuffers(String bufferKey, Collection<T> bufferItems) {
        if (CollectionUtil.isEmpty(bufferItems)) {
            return;
        }

        List<SequenceBufferRow<T>> sequenceBufferRows = bufferItems.stream()
                .map(this::createSequenceBufferRow)
                .collect(Collectors.toList());
        storeSequenceBuffers(bufferKey, sequenceBufferRows);
    }

    /**
     * delegate to {@link #fetchSeqBuffersWithCursor(String, SequenceCursor, Class)}
     * @param bufferKey     which buffer to fetch
     * @param bufferType    buffer's concrete type
     * @return              collection of buffer items
     * @param <T>           buffer's type
     */
    @Override
    default <T> Collection<T> fetchBuffers(String bufferKey, Class<T> bufferType) {
        SequenceCursor sequenceCursor = fetchCurrentCursor(bufferKey);
        List<? extends SequenceBufferRow<T>> sequenceBufferRows = fetchSeqBuffersWithCursor(bufferKey, sequenceCursor, bufferType);
        if (CollectionUtil.isEmpty(sequenceBufferRows)) {
            return Collections.emptyList();
        }
        return sequenceBufferRows.stream()
                .map(SequenceBufferRow::getRow)
                .collect(Collectors.toList());
    }

    /**
     * create an instance of {@link SequenceBufferRow} with a buffer item.
     * @param bufferItem        buffer item object, must not null
     * @return                  instance of {@link SequenceBufferRow}
     * @param <T>               buffer item's type
     */
    default <T> SequenceBufferRow<T> createSequenceBufferRow(T bufferItem) {
        return SimpleSequenceBufferRow.create(bufferItem);
    }

    /**
     * store buffer items which are wrapped as {@link SequenceBufferRow}
     * @param bufferKey             buffer's unique key
     * @param sequenceBufferRows    {@link SequenceBufferRow}
     * @param <T>                   buffer's type
     */
    <T> void storeSequenceBuffers(String bufferKey, Collection<SequenceBufferRow<T>> sequenceBufferRows);

    /**
     * fetch buffers with a specified {@link SequenceCursor}.
     * <p> after fetching buffers, this buffer key's {@link SequenceCursor} should auto move forward to the position of last consumption
     * @param bufferKey         buffer's unique key
     * @param seqCursor         {@link SequenceCursor}, should not be empty
     * @param bufferType        buffer's type
     * @param <T>               buffer's type
     * @return                  {@link SequenceBufferRow}
     */
    <T> List<? extends SequenceBufferRow<T>> fetchSeqBuffersWithCursor(String bufferKey, SequenceCursor seqCursor, Class<T> bufferType);

}
