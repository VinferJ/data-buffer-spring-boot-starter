package group.liquido.databuffer.core.factory;

import group.liquido.databuffer.core.common.InnerSupportStoreType;
import com.mongodb.ConnectionString;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author vinfer
 * @date 2022-12-08 11:53
 */
public class UriMongoBufferStoreFactory extends AbstractMongoBufferStoreFactory{

    public static final String CONF_KEY_URI = "uri";

    @Override
    protected void validCreation(InnerSupportStoreType storeType, Map<String, Object> confMeta) {
        Assert.isTrue(confMeta.containsKey(CONF_KEY_URI), "UriMongoBufferStoreFactory createBufferStore confMeta must contains key ["+CONF_KEY_URI+"]");
        Assert.hasText((String) confMeta.get(CONF_KEY_URI), "UriMongoBufferStoreFactory createBufferStore confMeta's key ["+CONF_KEY_URI+"], its value must has text");
    }

    @Override
    protected ConnectionString resolveConnectionString(Map<String, Object> confMeta) {
        String uri = (String) confMeta.get(CONF_KEY_URI);
        return new ConnectionString(uri);
    }

}
