package de.hpi.matcher.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@ConfigurationProperties(prefix = "parsed-offers.mongodb")
public class ParsedOfferMongoConfig extends AbstractMongoConfig {

    @Primary
    @Override
    public @Bean(name = "parsedOfferTemplate")
    MongoTemplate getMongoTemplate() throws Exception {
        return new MongoTemplate(mongoDbFactory());
    }

}