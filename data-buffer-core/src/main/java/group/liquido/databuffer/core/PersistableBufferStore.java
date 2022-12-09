package group.liquido.databuffer.core;

import cn.hutool.core.collection.CollectionUtil;
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

    private final AtomicInteger skipCursorHolder = new AtomicInteger(0);

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
        int currentSkip = skipCursorHolder.get();
        List<T> list = find(bufferKey, currentSkip, getBufferSize(), bufferType);
        skipForward(getBufferSize());
        return list;
    }

    private void skipForward(int forward) {
        int forwardSkip = skipCursorHolder.addAndGet(forward);
        upsertSkipCursor(forwardSkip);
    }

    private void skipBackward(int backward) {
        int backwardSkip = Math.max(0, skipCursorHolder.addAndGet(-backward));
        upsertSkipCursor(backwardSkip);
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
            skipForward(limit);
        }

        return remainBuffers;
    }

    @Override
    public int countBufferItem(String bufferKey) {
        if (bufferKeyNotFound(bufferKey)) {
            return 0;
        }
        return Math.toIntExact(count(bufferKey, skipCursorHolder.get()));
    }

    @Override
    public void clearBuffers(String bufferKey, int size) {
        if (bufferKeyNotFound(bufferKey)) {
            return;
        }
        remove(bufferKey, size);
        skipBackward(size);
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
        skipCursorHolder.set(getSkipCursor());
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
     * @param skip  skip cursor to update or save
     */
    protected abstract void upsertSkipCursor(int skip);

    /**
     * get skip cursor from some table.
     * <p> if there is no skip cursor exists, here should return 0
     * @return      skip cursor
     */
    protected abstract int getSkipCursor();

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
