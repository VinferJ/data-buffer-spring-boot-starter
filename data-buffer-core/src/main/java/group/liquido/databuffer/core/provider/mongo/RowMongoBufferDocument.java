package group.liquido.databuffer.core.provider.mongo;

import group.liquido.databuffer.core.common.SequenceBufferRow;

/**
 * @author vinfer
 * @date 2022-12-07 14:15
 */
public class RowMongoBufferDocument<T> extends BaseMongoDocument implements SequenceBufferRow<T> {

    private T row;

    @Override
    public String getSeqNo() {
        return get_id();
    }
    public void setRow(T row) {
        this.row = row;
    }

    public T getRow() {
        return row;
    }
}
