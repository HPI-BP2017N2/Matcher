package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;

public interface ParsedOfferRepository {

    ParsedOffer getByEan(long shopId, String ean);
    ParsedOffer getByEanWithVariation(long shopId, String ean);
    ParsedOffer getByHan(long shopId, String han);
    void deleteParsedOffer(long shopId, String url);

}
