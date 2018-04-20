package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.ParsedOffer;

public interface MatchIdentifierStrategy {

    ParsedOffer match(long shopId, ShopOffer offer);
    String getMatchingReason();
}
