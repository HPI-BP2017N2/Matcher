package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.repo.ParsedOfferRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MatchEanStrategy implements IMatchIdentifierStrategy {

    private ParsedOfferRepository repository;

    public MatchEanStrategy(ParsedOfferRepository repository) {
        setRepository(repository);
    }

    @Override
    public ParsedOffer match(long shopId, ShopOffer offer) {
        return getRepository().getByEan(shopId, offer.getEan());
    }

    @Override
    public String matchingReason() {
        return "ean";
    }
}
