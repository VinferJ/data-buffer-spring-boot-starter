package group.liquido.databuffer.core.provider.mongo;

import java.util.Date;

/**
 * @author vinfer
 * @date 2022-12-07 14:15
 */
public abstract class BaseMongoDocument {

    private String _id;
    private Date createTime;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "BaseMongoDocument{" +
                "_id='" + _id + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
