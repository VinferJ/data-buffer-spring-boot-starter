package group.liquido.databuffer.autoconfigure.prop;

import group.liquido.databuffer.core.common.InnerSupportStoreType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author vinfer
 * @date 2022-12-07 17:23
 */
@ConfigurationProperties(prefix = "liquido.data-buffer.buffer-store")
public class BufferStoreProperties {

    /**
     * inner support store type, details to see: {@link InnerSupportStoreType}
     */
    private String type;

    private Map<String, Object> confMeta;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getConfMeta() {
        return confMeta;
    }

    public void setConfMeta(Map<String, Object> confMeta) {
        this.confMeta = confMeta;
    }

    @Override
    public String toString() {
        return "BufferStoreProperties{" +
                "type='" + type + '\'' +
                ", confMeta=" + confMeta +
                '}';
    }
}
