package group.liquido.databuffer.core;

import cn.hutool.core.collection.CollectionUtil;
import group.liquido.databuffer.core.common.SkipCursor;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vinfer
 * @date 2022-12-07 11:45
 */
public abstract class PersistableBufferStore implements BufferStore, InitializingBean {

    private static final String TABLE_SKIP_CURSOR = "buffer_skip_cursor";

    private static final int DEFAULT_BUFFER_SIZE = 400;

    private final Set<String> storeBufferKeySet = new HashSet<>();

    private final Map<String, AtomicInteger> skipCursorMap = new HashMap<>();

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
    public <T> void storeBuffers(String bufferKey, Collection<T> bufferItems) {
        if (!storeBufferKeySet.contains(bufferKey)) {
            // create a temporary table for each buffer key
            createTableIfNotExists(bufferKey);
        }

        storeBufferKeySet.add(bufferKey);
        save(bufferKey, bufferItems);
    }

    @Override
    public <T> Collection<T> fetchBuffers(String bufferKey, Class<T> bufferType) {
        if (bufferKeyNotFound(bufferKey)) {
            return Collections.emptyList();
        }
        AtomicInteger bufferSkipCursor = getBufferSkipCursor(bufferKey);
        List<T> list = find(bufferKey, bufferSkipCursor.get(), getBufferSize(), bufferType);
        skipForward(bufferKey, getBufferSize());
        return list;
    }

    private void skipForward(String bufferKey, int forward) {
        int forwardSkip = getBufferSkipCursor(bufferKey).addAndGet(forward);
        upsertSkipCursor(bufferKey, forwardSkip);
    }

    private void skipBackward(String bufferKey, int backward) {
        int backwardSkip = Math.max(0, getBufferSkipCursor(bufferKey).addAndGet(-backward));
        upsertSkipCursor(bufferKey, backwardSkip);
    }

    private AtomicInteger getBufferSkipCursor(String bufferKey) {
        return skipCursorMap.getOrDefault(bufferKey, new AtomicInteger(0));
    }

    @Override
    public <T> List<Collection<T>> fetchAll(String bufferKey, Class<T> bufferType) {
        if (bufferKeyNotFound(bufferKey)) {
            return Collections.emptyList();
        }

        List<Collection<T>> remainBuffers = new ArrayList<>();
        Collection<T> buffers = null;
        int page = 1;
        int limit = getBufferSize();
        while (CollectionUtil.isNotEmpty((buffers = find(bufferKey, (page - 1) * limit, limit, bufferType)))) {
            page+=1;
            remainBuffers.add(buffers);
            skipForward(bufferKey, limit);
        }

        return remainBuffers;
    }

    @Override
    public int countBufferItem(String bufferKey) {
        if (bufferKeyNotFound(bufferKey)) {
            return 0;
        }
        AtomicInteger bufferSkipCursor = getBufferSkipCursor(bufferKey);
        return Math.toIntExact(count(bufferKey, bufferSkipCursor.get()));
    }

    @Override
    public void clearBuffers(String bufferKey, int size) {
        if (bufferKeyNotFound(bufferKey)) {
            return;
        }
        remove(bufferKey, size);
        skipBackward(bufferKey, size);
    }

    @Override
    public void clearBufferBuckets() {
        if (CollectionUtil.isNotEmpty(storeBufferKeySet)) {
            for (String bufferKey : storeBufferKeySet) {
                dropTable(bufferKey);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkSkipCursor();
    }

    private void checkSkipCursor() {
        List<? extends SkipCursor> allSkipCursors = getAllSkipCursors();
        if (CollectionUtil.isNotEmpty(allSkipCursors)) {
            for (SkipCursor skipCursor : allSkipCursors) {
                int cursor = skipCursor.getCursor();
                String bufferKey = skipCursor.getKey();
                skipCursorMap.put(bufferKey, new AtomicInteger(0));
                // make sure the cursor is 0
                // if cursor is greater than 0, and this buffer key's data's amount is greater than or equals to cursor,
                // that means there are dirty data in range of [0, cursor] which should have been removed before service shutdown,
                // we need remove them here.
                if (cursor > 0 && count(bufferKey, 0) >= cursor) {
                    // delete the dirty data
                    remove(bufferKey, cursor);
                }
            }
        }
    }

    protected boolean bufferKeyNotFound(String bufferKey) {
        return !storeBufferKeySet.contains(bufferKey);
    }

    /**
     * common table for save skip cursor
     * @return      skip cursor's table name
     */
    protected String getTableSkipCursor() {
        return TABLE_SKIP_CURSOR;
    }

    /**
     * update skip cursor or insert it when it's not exists, this an extra ability for current service to manage other buffer's meta info.
     * <p> to maintains a skip cursor is preventing data buffers concurrency consuming error.
     * @param bufferKey     which buffer key's cursor
     * @param skip          skip cursor to update or save
     */
    protected abstract void upsertSkipCursor(String bufferKey, int skip);

    /**
     * get all skip cursors from some table.
     * <p> if there is no skip cursor exists, current skipCursor should init as 0
     * @return              skip cursor
     */
    protected abstract List<? extends SkipCursor> getAllSkipCursors();

    /**
     * batch save buffer items.
     * @param tableName     temp table to save these buffer items
     * @param collection    buffer items
     * @param <T>           buffer items type
     */
    protected abstract <T> void save(String tableName, Collection<T> collection);

    /**
     * create a temporary table for saving data buffers.
     * @param tableName     table name
     */
    protected abstract void createTableIfNotExists(String tableName);

    /**
     * find data buffers from table.
     * @param tableName     table's name
     * @param skip          skip count
     * @param limit         limit of result list
     * @param rowType       result's row type
     * @return              buffer item list
     * @param <T>           buffer element's type
     */
    protected abstract <T> List<T> find(String tableName, int skip, int limit, Class<T> rowType);

    /**
     * count how many buffer items already store in this table.
     * @param tableName     table's name
     * @param skip          skip data's count
     * @return              buffer item's stored count
     */
    protected abstract long count(String tableName, int skip);

    /**
     * remove buffer items from table
     * @param tableName     table's name
     * @param removeCount   how many items to remove
     */
    protected abstract void remove(String tableName, int removeCount);

    /**
     * drop a temporary buffer storing table.
     * @param tableName     table's name
     */
    protected abstract void dropTable(String tableName);

}
