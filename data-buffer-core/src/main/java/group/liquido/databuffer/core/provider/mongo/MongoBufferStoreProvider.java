package group.liquido.databuffer.core.provider.mongo;

import cn.hutool.core.collection.CollectionUtil;
import group.liquido.databuffer.core.PersistableBufferStore;
import group.liquido.databuffer.core.common.SkipCursor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vinfer
 * @date 2022-12-07 11:44
 */
public class MongoBufferStoreProvider extends PersistableBufferStore {

    private final MongoOperations mongoOperations;

    public <T> MongoBufferStoreProvider(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    protected void upsertSkipCursor(String bufferKey, int skip) {
        String tableSkipCursor = getTableSkipCursor();
        Query query = new Query();
        Update update = Update.update("cursor", skip);
        mongoOperations.upsert(query, update, SkipCursorDocument.class, tableSkipCursor);
    }

    @Override
    protected List<SkipCursorDocument> getAllSkipCursors() {
        return mongoOperations.find(new Query(), SkipCursorDocument.class, getTableSkipCursor());
    }

    @Override
    protected <T> void save(String tableName, Collection<T> collection) {
        if (CollectionUtil.isEmpty(collection)) {
            return;
        }

        // convert to mongoBuffers
        List<RowMongoBufferDocument<T>> mongoBuffers = collection.stream()
                .map(this::toRowMongoBuffer)
                .collect(Collectors.toList());

        mongoOperations.insert(mongoBuffers, tableName);
    }

    @Override
    protected void createTableIfNotExists(String tableName) {
        // mongo collection will be auto created, do nothing here.
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected <T> List<T> find(String tableName, int skip, int limit, Class<T> rowType) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.skip(skip);
        query.limit(limit);

        List<RowMongoBufferDocument> list = mongoOperations.find(query, RowMongoBufferDocument.class, tableName);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream()
                .map(row -> (T)row.getRow())
                .collect(Collectors.toList());
    }

    @Override
    protected long count(String tableName, int skip) {
        Query query = new Query();
        query.skip(skip);
        return mongoOperations.count(query, tableName);
    }

    @Override
    protected void remove(String tableName, int removeCount) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.limit(removeCount);
        mongoOperations.remove(query, tableName);
    }

    @Override
    protected void dropTable(String tableName) {
        mongoOperations.dropCollection(tableName);
    }

    private <T> RowMongoBufferDocument<T> toRowMongoBuffer(T item) {
        RowMongoBufferDocument<T> rowMongoBuffer = new RowMongoBufferDocument<>();
        rowMongoBuffer.setRow(item);
        rowMongoBuffer.setCreateTime(new Date());
        return rowMongoBuffer;
    }

}
