package group.liquido.databuffer.autoconfigure;

import group.liquido.databuffer.autoconfigure.prop.DataBufferProperties;
import group.liquido.databuffer.core.BufferFlushEventFactory;
import group.liquido.databuffer.core.BufferFlushListenerRegistry;
import group.liquido.databuffer.core.BufferStore;
import group.liquido.databuffer.core.DataBufferLayer;
import group.liquido.databuffer.core.advised.AbstractBufferListenerAdvised;
import group.liquido.databuffer.core.advised.DefaultBufferListenerAdvised;
import group.liquido.databuffer.core.factory.ApplicationBufferFlushEventFactory;
import group.liquido.databuffer.core.provider.DataBufferLayerProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author vinfer
 * @date 2022-12-06 10:57
 */
@EnableConfigurationProperties(DataBufferProperties.class)
@Import({BufferStoreAutoConfiguration.class, BufferEventPollerAutoConfiguration.class})
@Configuration
public class DataBufferAutoConfiguration {

    private final DataBufferProperties dataBufferProperties;

    public DataBufferAutoConfiguration(DataBufferProperties dataBufferProperties) {
        this.dataBufferProperties = dataBufferProperties;
    }

    @ConditionalOnMissingBean(AbstractBufferListenerAdvised.class)
    @Bean
    AbstractBufferListenerAdvised bufferListenerAdvised() {
        return new DefaultBufferListenerAdvised();
    }

    @ConditionalOnMissingBean(BufferFlushEventFactory.class)
    @Bean
    BufferFlushEventFactory bufferFlushEventFactory() {
        return new ApplicationBufferFlushEventFactory();
    }

    @ConditionalOnMissingBean(DataBufferLayer.class)
    @Bean
    DataBufferLayer dataBufferLayer(BufferStore bufferStore, BufferFlushListenerRegistry bufferFlushListenerRegistry, BufferFlushEventFactory bufferFlushEventFactory) {
        DataBufferLayerProvider bufferLayerProvider = new DataBufferLayerProvider(bufferStore, bufferFlushListenerRegistry, bufferFlushEventFactory);
        bufferLayerProvider.setConsumeBufferSize(dataBufferProperties.getConsumeBufferSize());
        bufferLayerProvider.setMaxWaitForFlushing(dataBufferProperties.getMaxWaitForFlushing());
        return bufferLayerProvider;
    }

}
