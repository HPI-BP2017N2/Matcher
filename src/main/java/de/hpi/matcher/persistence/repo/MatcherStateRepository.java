package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.State;
import lombok.AccessLevel;
import lombok.Getter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Getter(AccessLevel.PRIVATE)
@Repository
public class MatcherStateRepository {

    @Autowired
    @Qualifier(value = "stateTemplate")
    private MongoTemplate mongoTemplate;

    public List<State> popAllStates() {
        return getMongoTemplate().findAllAndRemove(query(where("_id").exists(true)), State.class);
    }

    public void saveState(long shopId, byte phase, List<Integer> imageIds) {
        getMongoTemplate().getCollection("state").deleteOne(new Document("_id", shopId));
        getMongoTemplate().insert(new State(shopId, phase, imageIds));
    }

    public void saveAllStates(List<State> states) {
        for (State state : states) {
            getMongoTemplate().getCollection("state").deleteOne(new Document("_id", state.getShopId()));
        }
        getMongoTemplate().insertAll(states);
    }

}
