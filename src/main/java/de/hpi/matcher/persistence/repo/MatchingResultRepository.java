package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.MatchingResult;
import lombok.AccessLevel;
import lombok.Getter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Getter(AccessLevel.PRIVATE)
@Repository
public class MatchingResultRepository {

    @Autowired
    @Qualifier("matchingResultTemplate")
    private MongoTemplate mongoTemplate;


    public void save(long shopId, MatchingResult matchingResult) {
        getMongoTemplate().getCollection(Long.toString(shopId)).deleteOne(new Document("_id", matchingResult.getUrl()));
        getMongoTemplate().insert(matchingResult, Long.toString(shopId));
    }
}