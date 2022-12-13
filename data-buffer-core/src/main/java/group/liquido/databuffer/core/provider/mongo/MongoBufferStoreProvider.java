package group.liquido.databuffer.core.provider.mongo;

import group.liquido.databuffer.core.PersistableBufferStore;
import group.liquido.databuffer.core.common.SequenceBufferRow;
import group.liquido.databuffer.core.common.SequenceCursor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vinfer
 * @date 2022-12-07 11:44
 */
public class MongoBufferStoreProvider extends PersistableBufferStore {

    private final MongoOperations mongoOperations;

    public MongoBufferStoreProvider(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void upsertCursor(String bufferKey, String seqCursor) {
        String seqCursorTableName = getSeqCursorTableName();
        Criteria criteria = Criteria.where(SEQ_CURSOR_FIELD_KEY).is(bufferKey);
        Query query = new Query(criteria);
        Update update = Update.update(SEQ_CURSOR_FIELD_SEQ_NO, seqCursor);
        mongoOperations.upsert(query, update, SequenceCursorDocument.class, seqCursorTableName);
    }

    @Override
    protected SequenceCursor doFetchCurrentCursor(String key) {
        String seqCursorTableName = getSeqCursorTableName();
        Criteria criteria = Criteria.where(SEQ_CURSOR_FIELD_KEY).is(key);
        Query query = new Query(criteria);
        return mongoOperations.findOne(query, SequenceCursorDocument.class, seqCursorTableName);
    }

    @Override
    protected List<? extends SequenceCursor> doFetchAllCursors() {
        String seqCursorTableName = getSeqCursorTableName();
        Query query = new Query();
        return mongoOperations.find(query, SequenceCursorDocument.class, seqCursorTableName);
    }

    @Override
    protected <T> void save(String tableName, Collection<SequenceBufferRow<T>> collection) {
        mongoOperations.insert(collection, tableName);
    }

    @Override
    public <T> SequenceBufferRow<T> createSequenceBufferRow(T bufferItem) {
        return toRowMongoBuffer(bufferItem);
    }

    @Override
    protected void createTableIfNotExists(String tableName) {
        // mongo collection will be auto created, do nothing here.
    }

    @Override
    protected SequenceCursor createCursorInstance(String key, String seqNo) {
        SequenceCursorDocument sequenceCursorDocument = new SequenceCursorDocument();
        sequenceCursorDocument.setKey(key);
        sequenceCursorDocument.setSeqNo(seqNo);
        return sequenceCursorDocument;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> List<? extends SequenceBufferRow<T>> find(String tableName, String seqCursor, int limit, Class<T> rowType) {
        Query query = genSeqCursorQuery(seqCursor);
        query.limit(limit);
        return mongoOperations.find(query, RowMongoBufferDocument.class, tableName)
                .stream()
                .map(row -> (SequenceBufferRow<T>) row)
                .collect(Collectors.toList());
    }

    @Override
    protected SequenceBufferRow<?> findOne(String tableName, int skip) {
        Query query = genSeqCursorQuery(null);
        query.skip(skip);
        query.limit(1);
        return mongoOperations.findOne(query, RowMongoBufferDocument.class, tableName);
    }

    @Override
    protected long count(String tableName, String seqCursor) {
        Query query = genSeqCursorQuery(seqCursor);
        return mongoOperations.count(query, tableName);
    }

    @Override
    protected void dropTable(String tableName) {
        mongoOperations.dropCollection(tableName);
    }

    private Query genSeqCursorQuery(String seqCursor) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.ASC, "_id"));


        if (StringUtils.hasText(seqCursor)) {
            Criteria criteria = Criteria.where("_id").gt(seqCursor);
            query.addCriteria(criteria);
        }

        return query;
    }

    private <T> RowMongoBufferDocument<T> toRowMongoBuffer(T item) {
        RowMongoBufferDocument<T> rowMongoBuffer = new RowMongoBufferDocument<>();
        rowMongoBuffer.setRow(item);
        rowMongoBuffer.setCreateTime(new Date());
        return rowMongoBuffer;
    }

}
