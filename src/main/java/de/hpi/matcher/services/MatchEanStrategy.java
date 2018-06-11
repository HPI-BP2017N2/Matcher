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
public class MatchEanStrategy implements MatchIdentifierStrategy {

    private final ParsedOfferRepository repository;

    @Override
    public ParsedOffer match(long shopId, ShopOffer offer) {
        if(offer.getEan() == null) return null;
        String normalizedEan = deleteLeadingZeros(offer.getEan());
        ParsedOffer exactMatch = getRepository().getByEan(shopId, normalizedEan);
        return exactMatch;
        /*if(exactMatch != null) return exactMatch;

        return getRepository().getByEanWithVariation(shopId, deleteLeadingZeros(offer.getEan()));*/
    }

    private String deleteLeadingZeros(String ean) {
        return ean.replaceFirst("^0+", "");
    }

    @Override
    public String getMatchingReason() {
        return "ean";
    }
}
