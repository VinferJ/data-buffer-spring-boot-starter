package group.liquido.databuffer.core.common;

/**
 * @author vinfer
 * @date 2022-12-09 16:24
 */
public interface SkipCursor {

    /**
     * this cursor's unique key
     * @return      cursor key
     */
    String getKey();

    /**
     * cursor value
     * @return      cursor value
     */
    int getCursor();
}
