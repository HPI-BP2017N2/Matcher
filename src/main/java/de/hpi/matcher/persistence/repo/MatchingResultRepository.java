package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.MatchingResult;

public interface MatchingResultRepository {

    void save(long shopId, MatchingResult matchingResult);
    void createCollection(long shopId);
}
