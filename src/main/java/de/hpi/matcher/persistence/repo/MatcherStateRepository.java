package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.State;
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
public class MatcherStateRepository {

    @Autowired
    @Qualifier(value = "stateTemplate")
    private MongoTemplate mongoTemplate;

    public void saveState(long shopId, byte phase) {
        getMongoTemplate().insert(new State(shopId, phase));

    }

    public State popState() {
        State state = getMongoTemplate().findOne(query(where("shopId").exists(true)), State.class);
        if(state != null) {
            getMongoTemplate().remove(state);
        }
        return state;
    }

}
