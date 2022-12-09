package group.liquido.databuffer.core.factory;

import group.liquido.databuffer.core.BufferStore;
import group.liquido.databuffer.core.common.InnerSupportStoreType;
import group.liquido.databuffer.core.provider.mongo.MongoBufferStoreProvider;
import com.mongodb.ConnectionString;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author vinfer
 * @date 2022-12-08 11:41
 */
public abstract class AbstractMongoBufferStoreFactory implements BufferStoreFactory{

    @Override
    public BufferStore createBufferStore(InnerSupportStoreType storeType, Map<String, Object> confMeta) {
        validCreation(storeType, confMeta);
        ConnectionString connectionString = resolveConnectionString(confMeta);
        MongoOperations mongoOperations = createMongoOperations(connectionString);
        return new MongoBufferStoreProvider(mongoOperations);
    }

    protected void validCreation(InnerSupportStoreType storeType, Map<String, Object> confMeta) {
        Assert.isTrue(InnerSupportStoreType.MONGO.equals(storeType), "AbstractMongoBufferStoreFactory createBufferStore storeType ["+storeType+"] not supported");
    }

    protected MongoOperations createMongoOperations(ConnectionString connectionString) {
        SimpleMongoClientDatabaseFactory databaseFactory = new SimpleMongoClientDatabaseFactory(connectionString);
        return new MongoTemplate(databaseFactory);
    }

    /**
     * resolve {@code ConnectionString} by configuration metadata
     * @param confMeta      configuration metadata
     * @return              {@link ConnectionString}
     */
    protected abstract ConnectionString resolveConnectionString(Map<String, Object> confMeta);

}
