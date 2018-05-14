package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;

import java.util.List;

public interface ParsedOfferRepository {

    boolean eanFound(long shopId);
    boolean hanFound(long shopId);
    ParsedOffer getByEan(long shopId, String ean);
    ParsedOffer getByEanWithVariation(long shopId, String ean);
    ParsedOffer getByHan(long shopId, String han);
    void deleteParsedOffer(long shopId, String url);
    List<ParsedOffer> getOffers(long shopId, int count);

}
