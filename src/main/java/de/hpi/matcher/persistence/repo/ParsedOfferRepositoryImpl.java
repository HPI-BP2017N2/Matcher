package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Getter(AccessLevel.PRIVATE)
@Repository
public class ParsedOfferRepositoryImpl implements ParsedOfferRepository {

    @Autowired
    @Qualifier("parsedOfferTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public ParsedOffer getParsedOffer(long shopId) {
        return getMongoTemplate().findOne(query(where("url").exists(true)), ParsedOffer.class);
    }
}
