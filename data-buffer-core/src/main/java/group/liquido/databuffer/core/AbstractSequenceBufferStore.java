package group.liquido.databuffer.core;

import cn.hutool.core.collection.CollectionUtil;
import group.liquido.databuffer.core.common.SequenceBufferRow;
import group.liquido.databuffer.core.common.SequenceCursor;
import group.liquido.databuffer.core.provider.KeyMonitorSafeOperationProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * abstract of {@link SequenceBufferStore}, maintains the sequence fetch cursor and providing the thread-safe fetch.
 * @author vinfer
 * @date 2022-12-12 15:03
 */
public abstract class AbstractSequenceBufferStore implements SequenceBufferStore, InitializingBean {

    private static final String KEY_FETCH_ALL_CURSORS = "FETCH_ALL_CURSORS";
    protected static final String SEQ_CURSOR_FIELD_KEY = "key";
    protected static final String SEQ_CURSOR_FIELD_SEQ_NO = "seqNo";

    private final Set<String> storedBufferKeySet = new HashSet<>();

    private final Map<String, String> sequenceCursorMap = new HashMap<>();

    // TODO: 2022/12/13 configurable
    private final KeyMonitorSafeOperationProvider safeOperationProvider = new KeyMonitorSafeOperationProvider();

    @Override
    public final <T> Collection<T> fetchBuffers(String bufferKey, Class<T> bufferType) {
        if (bufferKeyNotFound(bufferKey)) {
            return Collections.emptyList();
        }
        return SequenceBufferStore.super.fetchBuffers(bufferKey, bufferType);
    }

    @Override
    public final  <T> List<Collection<T>> fetchAll(String bufferKey, Class<T> bufferType) {
        if (bufferKeyNotFound(bufferKey)) {
            return Collections.emptyList();
        }
        return SequenceBufferStore.super.fetchAll(bufferKey, bufferType);
    }

    @Override
    public <T> List<? extends SequenceBufferRow<T>> fetchSeqBuffersWithCursor(String bufferKey, SequenceCursor seqCursor, Class<T> bufferType) {
        return safeOperationProvider.safeRead(bufferKey, () -> {
            if (bufferKeyNotFound(bufferKey)) {
                return Collections.emptyList();
            }

            SequenceCursor queryCursor = Objects.requireNonNullElse(seqCursor, ZERO_POS_CURSOR);
            List<? extends SequenceBufferRow<T>> sequenceBufferRows = fetchSeqBuffersWithSeqNo(bufferKey, queryCursor.getSeqNo(), bufferType);
            moveSeqCursorForward(bufferKey, sequenceBufferRows);

            return sequenceBufferRows;
        });
    }

    @Override
    public <T> void storeSequenceBuffers(String bufferKey, Collection<SequenceBufferRow<T>> bufferRows) {
        doStoreSequenceBuffers(bufferKey, bufferRows);
        storedBufferKeySet.add(bufferKey);
    }

    @Override
    public int countBufferItem(String bufferKey) {
        if (bufferKeyNotFound(bufferKey)) {
            return 0;
        }

        String cursorSeqNo = getCursorSeqNo(bufferKey);
        long bufferRowCount = countWithSeqNo(bufferKey, cursorSeqNo);

        return Math.toIntExact(bufferRowCount);
    }

    @Override
    public final SequenceCursor fetchCurrentCursor(String key) {
        return safeOperationProvider.safeRead(key, () -> doFetchCurrentCursor(key));
    }

    @Override
    public final SequenceCursor fetchPositionCursor(String key, int position) {
        return safeOperationProvider.safeRead(key, () -> doFetchPositionCursor(key, position));
    }

    @Override
    public final List<? extends SequenceCursor> fetchAllSeqCursors() {
        return safeOperationProvider.safeRead(KEY_FETCH_ALL_CURSORS, this::doFetchAllCursors);
    }

    @Override
    public final void updateCursor(String key, String seqNo) {
        safeOperationProvider.safeWrite(key, () -> {
            sequenceCursorMap.put(key, seqNo);
            upsertCursor(key, seqNo);
        });
    }

    private String getCursorSeqNo(String bufferKey) {
        return safeOperationProvider.safeRead(bufferKey, () -> doGetCursorSeqNo(bufferKey));
    }

    private String doGetCursorSeqNo(String bufferKey) {
        String seqNo = sequenceCursorMap.get(bufferKey);
        if (StringUtils.hasText(seqNo)) {
            return seqNo;
        }

        // fetch seqNo and update
        SequenceCursor sequenceCursor = doFetchCurrentCursor(bufferKey);
        if (null != sequenceCursor) {
            seqNo = sequenceCursor.getSeqNo();
            sequenceCursorMap.put(bufferKey, seqNo);
        }

        return seqNo;
    }

    private <T> void moveSeqCursorForward(String bufferKey, List<? extends SequenceBufferRow<T>> sequenceBufferRows) {
        String nextCursorSeqNo = genNextCursorSeqNo(sequenceBufferRows);
        sequenceCursorMap.put(bufferKey, nextCursorSeqNo);
        upsertCursor(bufferKey, nextCursorSeqNo);
    }

    @Override
    public void afterPropertiesSet() {
        loadSeqCursor();
    }

    private void loadSeqCursor() {
        List<? extends SequenceCursor> sequenceCursors = fetchAllSeqCursors();
        if (null != sequenceCursors && CollectionUtil.isNotEmpty(sequenceCursors)) {
            for (SequenceCursor sequenceCursor : sequenceCursors) {
                sequenceCursorMap.put(sequenceCursor.getKey(), sequenceCursor.getSeqNo());
            }
        }
    }

    protected final boolean bufferKeyNotFound(String bufferKey) {
        return !storedBufferKeySet.contains(bufferKey);
    }

    protected Set<String> getStoredBufferKeySet() {
        return storedBufferKeySet;
    }

    protected <T> String genNextCursorSeqNo(List<? extends SequenceBufferRow<T>> sequenceBufferRows) {
        return CollectionUtil.getLast(sequenceBufferRows).getSeqNo();
    }

    /**
     * fetch buffers with a sequence no, all {@link SequenceBufferRow}'s seqNo should be greater than this {@code seqNo}
     * @param bufferKey     buffer key
     * @param seqNo         {@link SequenceBufferRow#getSeqNo()}, a fetch cursor either, should fetch the data behind this seqNo
     * @param bufferType    buffer's type
     * @return              {@link SequenceBufferRow}
     * @param <T>           buffer's type
     */
    protected abstract <T> List<? extends SequenceBufferRow<T>> fetchSeqBuffersWithSeqNo(String bufferKey, String seqNo, Class<T> bufferType);

    /**
     * delegate implements of {@link #storeSequenceBuffers(String, Collection)}
     * @param bufferKey             buffer key
     * @param sequenceBufferRows    {@link SequenceBufferRow}
     * @param <T>                   buffer's type
     */
    protected abstract <T> void doStoreSequenceBuffers(String bufferKey, Collection<SequenceBufferRow<T>> sequenceBufferRows);

    /**
     * delegate implements of {@link #fetchCurrentCursor(String)}
     * @param bufferKey     buffer key
     * @return              {@link SequenceCursor}
     */
    protected abstract SequenceCursor doFetchCurrentCursor(String bufferKey);

    /**
     * delegate implements of {@link #fetchPositionCursor(String, int)}
     * @param bufferKey     buffer key
     * @param position      cursor position
     * @return              {@link SequenceCursor}
     */
    protected abstract SequenceCursor doFetchPositionCursor(String bufferKey, int position);

    /**
     * delegate implements of {@link #fetchAllSeqCursors()}
     * @return              {@link SequenceCursor}
     */
    protected abstract List<? extends SequenceCursor> doFetchAllCursors();

    /**
     * count buffer items with a start sequence no
     * @param bufferKey     buffer key
     * @param seqNo         start sequence no
     * @return              buffer item's count
     */
    protected abstract long countWithSeqNo(String bufferKey, String seqNo);

    /**
     * update a cursor's seqNo, insert it if not exists.
     * @param bufferKey     buffer key
     * @param cursorSeqNo   cursor sequence no
     */
    protected abstract void upsertCursor(String bufferKey, String cursorSeqNo);

}
