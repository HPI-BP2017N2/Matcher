package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;

public interface ParsedOfferRepository {

    ParsedOffer getParsedOffer(long shopId);
    ParsedOffer getByEan(long shopId, String ean);
    ParsedOffer getByHan(long shopId, String han);
    ParsedOffer getBySku(long shopId, String sku);
    void deleteParsedOffer(long shopId, String url);

}
