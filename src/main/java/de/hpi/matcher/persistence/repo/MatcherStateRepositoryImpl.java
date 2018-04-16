package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.State;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Repository
public class MatcherStateRepositoryImpl  implements MatcherStateRepository{

    @Autowired
    @Qualifier(value = "stateTemplate")
    private MongoTemplate mongoTemplate;


    @Override
    public void saveState(long shopId, byte phase) {
        getMongoTemplate().insert(new State(shopId, phase));

    }

    @Override
    public State popState() {
        State state = getMongoTemplate().findOne(query(where("shopId").exists(true)), State.class);
        getMongoTemplate().remove(state);
        return state;
    }

}
