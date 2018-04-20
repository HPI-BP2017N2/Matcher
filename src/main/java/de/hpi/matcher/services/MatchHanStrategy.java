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
public class MatchHanStrategy implements MatchIdentifierStrategy {

    private final ParsedOfferRepository repository;

    @Override
    public ParsedOffer match(long shopId, ShopOffer offer) {
        return getRepository().getByHan(shopId, offer.getHan());
    }

    @Override
    public String getMatchingReason() {
        return "han";
    }
}
