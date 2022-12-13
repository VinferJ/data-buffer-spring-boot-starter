package group.liquido.databuffer.core.provider.mongo;

import group.liquido.databuffer.core.common.SequenceCursor;

/**
 * @author vinfer
 * @date 2022-12-07 16:19
 */
public class SequenceCursorDocument extends BaseMongoDocument implements SequenceCursor {

    private String key;
    private String seqNo;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public String toString() {
        return "SequenceCursorDocument{" +
                "key='" + key + '\'' +
                ", seqNo='" + seqNo + '\'' +
                "} " + super.toString();
    }
}
