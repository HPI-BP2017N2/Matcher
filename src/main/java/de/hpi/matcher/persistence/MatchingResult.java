package de.hpi.matcher.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hpi.matcher.dto.ShopOffer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchingResult {

    private long shopId;
    private String matchingReason;
    private int confidence;
    private String offerKey;
    private String idealoCategory;
    private String idealoCategoryName;
    private String higherLevelIdealoCategory;
    private String higherLevelIdealoCategoryName;
    private ParsedOffer parsedData;

    public MatchingResult(long shopId,
                          String matchingReason,
                          int confidence,
                          String offerKey,
                          ShopOffer idealoOffer,
                          ParsedOffer parsedData) {
        setShopId(shopId);
        setMatchingReason(matchingReason);
        setConfidence(confidence);
        setOfferKey(offerKey);
        setParsedData(parsedData);
        setIdealoCategory(idealoOffer.getMappedCatalogCategory());
        setIdealoCategoryName(idealoOffer.getCategoryName());
        setHigherLevelIdealoCategory(idealoOffer.getHigherLevelCategory());
        setHigherLevelIdealoCategoryName(idealoOffer.getHigherLevelCategoryName());
    }

}
