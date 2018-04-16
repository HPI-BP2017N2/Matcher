package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;

public interface ParsedOfferRepository {

    ParsedOffer getParsedOffer(long shopId);

}
