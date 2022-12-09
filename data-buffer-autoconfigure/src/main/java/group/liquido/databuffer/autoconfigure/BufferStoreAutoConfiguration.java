package group.liquido.databuffer.autoconfigure;

import group.liquido.databuffer.autoconfigure.prop.BufferStoreProperties;
import group.liquido.databuffer.core.BufferStore;
import group.liquido.databuffer.core.common.InnerSupportStoreType;
import group.liquido.databuffer.core.factory.BufferStoreFactory;
import group.liquido.databuffer.core.factory.UriMongoBufferStoreFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author vinfer
 * @date 2022-12-07 17:58
 */
@Configuration
@EnableConfigurationProperties(BufferStoreProperties.class)
public class BufferStoreAutoConfiguration {

    private final BufferStoreProperties bufferStoreProperties;

    public BufferStoreAutoConfiguration(BufferStoreProperties bufferStoreProperties) {
        this.bufferStoreProperties = bufferStoreProperties;
    }

    @ConditionalOnProperty(name = "liquido.data-buffer.buffer-store.type", havingValue = "mongo")
    @ConditionalOnMissingBean(BufferStoreFactory.class)
    @Bean
    BufferStoreFactory mongoBufferStoreFactory() {
        String storeType = bufferStoreProperties.getType();
        Map<String, Object> confMeta = bufferStoreProperties.getConfMeta();
        InnerSupportStoreType innerSupportStoreType = InnerSupportStoreType.convert(storeType);
        if (confMeta.containsKey(UriMongoBufferStoreFactory.CONF_KEY_URI)) {
            return new UriMongoBufferStoreFactory();
        }
        throw new BeanCreationException("store type only support ["+InnerSupportStoreType.MONGO.name()+"] now, and you can only use a uri value as confMeta");
    }

    @ConditionalOnMissingBean(BufferStore.class)
    @Bean
    BufferStore bufferStore(BufferStoreFactory bufferStoreFactory) {
        return bufferStoreFactory.createBufferStore(bufferStoreProperties.getType(), bufferStoreProperties.getConfMeta());
    }

}
