package group.liquido.databuffer.core.provider.mongo;

import group.liquido.databuffer.core.common.SkipCursor;

/**
 * @author vinfer
 * @date 2022-12-07 16:19
 */
public class SkipCursorDocument extends BaseMongoDocument implements SkipCursor {

    private String key;
    private Integer cursor;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "SkipCursorDocument{" +
                "key='" + key + '\'' +
                ", cursor=" + cursor +
                "} " + super.toString();
    }
}
