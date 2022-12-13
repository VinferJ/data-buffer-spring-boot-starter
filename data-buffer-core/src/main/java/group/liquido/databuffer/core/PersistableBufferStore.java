package group.liquido.databuffer.core;

import cn.hutool.core.collection.CollectionUtil;
import group.liquido.databuffer.core.common.SequenceBufferRow;
import group.liquido.databuffer.core.common.SequenceCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author vinfer
 * @date 2022-12-07 11:45
 */
public abstract class PersistableBufferStore extends AbstractSequenceBufferStore implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistableBufferStore.class);

    private static final String TABLE_SEQ_CURSOR = "buffer_consume_seq";

    private static final int DEFAULT_BUFFER_SIZE = 400;

    private int bufferSize;

    protected PersistableBufferStore(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    protected PersistableBufferStore() {
        this(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    protected <T> List<? extends SequenceBufferRow<T>> fetchSeqBuffersWithSeqNo(String bufferKey, String seqNo, Class<T> bufferType) {
        return find(bufferKey, seqNo, getBufferSize(), bufferType);
    }

    @Override
    protected  <T> void doStoreSequenceBuffers(String bufferKey, Collection<SequenceBufferRow<T>> bufferRows) {
        save(bufferKey, bufferRows);
    }

    @Override
    protected long countWithSeqNo(String bufferKey, String seqNo) {
        return count(bufferKey, seqNo);
    }

    @Override
    public void clearBufferBuckets() {
        dropTable(getSeqCursorTableName());
        Set<String> storedBufferKeySet = getStoredBufferKeySet();
        if (CollectionUtil.isNotEmpty(storedBufferKeySet)) {
            for (String bufferKey : storedBufferKeySet) {
                dropTable(bufferKey);
            }
        }
    }

    @Override
    protected SequenceCursor doFetchPositionCursor(String key, int position) {
        SequenceBufferRow<?> bufferRow = findOne(key, position - 1);
        if (null != bufferRow) {
            return createCursorInstance(key, bufferRow.getSeqNo());
        }
        return null;
    }

    /**
     * common table for save buffer sequence cursor's data.
     * @return      sequence cursor's table name
     */
    protected String getSeqCursorTableName() {
        return TABLE_SEQ_CURSOR;
    }

    protected SequenceCursor createCursorInstance(String key, String seqNo) {
        return new SequenceCursor() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getSeqNo() {
                return seqNo;
            }
        };
    }

    /**
     * batch save buffer items.
     * @param tableName     temp table to save these buffer items
     * @param collection    buffer items
     * @param <T>           buffer items type
     */
    protected abstract <T> void save(String tableName, Collection<SequenceBufferRow<T>> collection);

    /**
     * create a temporary table for saving data buffers.
     * @param tableName     table name
     */
    protected abstract void createTableIfNotExists(String tableName);

    /**
     * find data buffers from table, which buffer item's sequence no are greater than {@code seqCursor}.
     * @param tableName     table's name
     * @param seqCursor     sequence cursor of last consume
     * @param limit         limit of result list
     * @param rowType       result's row type
     * @return              buffer item list
     * @param <T>           buffer element's type
     */
    protected abstract <T> List<? extends SequenceBufferRow<T>> find(String tableName, String seqCursor, int limit, Class<T> rowType);

    /**
     * find one {@link SequenceBufferRow} with a skip operation.
     * @param tableName     table's name
     * @param skip          skip count
     * @return              {@link SequenceBufferRow}
     */
    protected abstract SequenceBufferRow<?> findOne(String tableName, int skip);

    /**
     * count how many buffer items which sequence no are greater than {@code seqCursor} already store in this table.
     * @param tableName     table's name
     * @param seqCursor     sequence cursor of last consume
     * @return              buffer item's stored count
     */
    protected abstract long count(String tableName, String seqCursor);

    /**
     * drop a temporary buffer storing table.
     * @param tableName     table's name
     */
    protected abstract void dropTable(String tableName);

}
