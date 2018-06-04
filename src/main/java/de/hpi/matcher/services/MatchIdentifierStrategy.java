package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.ParsedOffer;

public interface MatchIdentifierStrategy {

    /**
     * This method finds a "safe" match for a given offer if it exists.
     * @param shopId The ID of the shop
     * @param offer The offer to find a match for
     * @return The corresponding parsed offer (null if no match found)
     */
    ParsedOffer match(long shopId, ShopOffer offer);

    String getMatchingReason();
}
