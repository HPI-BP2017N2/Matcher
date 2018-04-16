package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.State;

public interface MatcherStateRepository {

    void saveState(long shopId, byte phase);
    State popState();

}
