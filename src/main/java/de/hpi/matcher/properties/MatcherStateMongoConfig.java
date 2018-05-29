package de.hpi.matcher.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConfigurationProperties(prefix = "state.mongodb")
public class MatcherStateMongoConfig extends AbstractMongoConfig {

    public @Bean(name = "stateTemplate")
    MongoTemplate getMongoTemplate() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }

}
