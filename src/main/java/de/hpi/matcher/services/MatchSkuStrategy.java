package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.repo.ParsedOfferRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MatchSkuStrategy implements IMatchIdentifierStrategy {

    private final ParsedOfferRepository repository;

    @Override
    public ParsedOffer match(long shopId, ShopOffer offer) {
        return getRepository().getBySku(shopId, offer.getSku());
    }

    @Override
    public String getMatchingReason() {
        return "sku";
    }
}
