package group.liquido.databuffer.core.provider.mongo;

/**
 * @author vinfer
 * @date 2022-12-07 14:15
 */
public class RowMongoBufferDocument<T> extends BaseMongoDocument{

    private T row;

    public void setRow(T row) {
        this.row = row;
    }

    public T getRow() {
        return row;
    }
}
