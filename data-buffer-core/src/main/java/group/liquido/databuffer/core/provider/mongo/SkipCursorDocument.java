package group.liquido.databuffer.core.provider.mongo;

/**
 * @author vinfer
 * @date 2022-12-07 16:19
 */
public class SkipCursorDocument extends BaseMongoDocument{

    private Integer cursor;

    public Integer getCursor() {
        return cursor;
    }

    public void setCursor(Integer cursor) {
        this.cursor = cursor;
    }

    @Override
    public String toString() {
        return "SkipCursorDocument{" +
                "cursor=" + cursor +
                "} " + super.toString();
    }
}
