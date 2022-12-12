/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config.mongo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;

/**
 *	Configuration for MongoDB.
 */
@Configuration
public class MongoDatabaseCFG {

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(final MongoPropertiesCFG mongoPropertiesCFG){
        return new SimpleMongoClientDatabaseFactory(mongoPropertiesCFG.getUri());
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(final ApplicationContext appContext,final MongoDatabaseFactory factory) {
        final MongoMappingContext mongoMappingContext = new MongoMappingContext();
        mongoMappingContext.setApplicationContext(appContext);
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(factory), mongoMappingContext);

        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(factory, converter);
    }

    @Bean
    public LockProvider lockProvider(MongoTemplate template) {
        return new MongoLockProvider(template.getDb());
    }

}