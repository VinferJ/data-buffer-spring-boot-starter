package group.liquido.databuffer.core.common;

/**
 * @author vinfer
 * @date 2022-12-12 10:44
 */
public class SimpleSequenceBufferRow<T> implements SequenceBufferRow<T>{

    private String seqNo;
    private T row;

    public SimpleSequenceBufferRow(String seqNo, T row) {
        this.seqNo = seqNo;
        this.row = row;
    }

    public SimpleSequenceBufferRow() {

    }

    public static <T> SimpleSequenceBufferRow<T> create(T row) {
        return new SimpleSequenceBufferRow<>(null, row);
    }


    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public String getSeqNo() {
        return seqNo;
    }

    public void setRow(T row) {
        this.row = row;
    }

    @Override
    public T getRow() {
        return row;
    }
}
