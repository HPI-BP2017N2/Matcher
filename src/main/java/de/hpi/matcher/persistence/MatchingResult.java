package de.hpi.matcher.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.hpi.matcher.dto.ShopOffer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter(AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class MatchingResult {

    @Id private final String url;
    private final long shopId;
    private final String matchingReason;
    private final int confidence;
    private final String offerKey;
    private final String idealoBrand;
    private final String idealoCategory;
    private final String idealoCategoryName;
    private final String higherLevelIdealoCategory;
    private final String higherLevelIdealoCategoryName;
    private final ParsedOffer parsedData;

    public MatchingResult(long shopId, ParsedOffer offer, int confidence, String idealoBrand, String idealoCategory) {
        this.url = offer.getUrl();
        this.shopId = shopId;
        this.matchingReason = null;
        this.confidence = confidence;
        this.offerKey = null;
        this.idealoCategory = null;
        this.idealoCategoryName = null;
        this.higherLevelIdealoCategory = idealoCategory;
        this.higherLevelIdealoCategoryName = null;
        this.parsedData = offer;
        this.idealoBrand = idealoBrand;
    }
}
