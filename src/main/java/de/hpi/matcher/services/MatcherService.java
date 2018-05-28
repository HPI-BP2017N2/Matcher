package de.hpi.matcher.services;

import de.hpi.machinelearning.PictureIdFinder;
import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.MatchingResult;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.State;
import de.hpi.matcher.persistence.repo.*;
import de.hpi.matcher.properties.MatcherProperties;
import de.hpi.matcher.properties.RetryProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.nd4j.linalg.primitives.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MatcherService {

    private byte phase = 0;
    private long shopId = 0;
    private List<Integer> pictureIds;
    private final Cache cache;
    private final MatcherStateRepository matcherStateRepository;
    private final ParsedOfferRepository parsedOfferRepository;
    private final MatchingResultRepository matchingResultRepository;
    private final ModelRepository modelRepository;
    private final ModelGenerator modelGenerator;
    private final ProbabilityClassifier classifier;
    private final MatcherProperties properties;
    private List<MatchIdentifierStrategy> identifierStrategies = new ArrayList<>();
    private List<Integer> imageUrlIdPosition = new ArrayList<>();

    @PostConstruct
    public void restartInterruptedMatching() throws Exception {
        State state = getMatcherStateRepository().popState();
        if(state != null) {
            matchShop(state.getShopId(), state.getPhase());
        }
        getClassifier().setup();
    }

    @PreDestroy
    public void saveState() {
        if(getShopId() != 0) {
            getMatcherStateRepository().saveState(getShopId(), getPhase());
        }
    }

    public void matchShop(long shopId, byte phase) {
        setupState(shopId, phase);
        getCache().warmup(shopId);
        setStrategies(shopId);
        setImageIds(shopId);

        if(!getIdentifierStrategies().isEmpty()){
            matchAllByIdentifier(shopId);
        }
        setPhase((byte)(getPhase() + 1));


        clearState();
    }

    private void setImageIds(long shopId) {
        List<ParsedOffer> offers = getParsedOfferRepository().getOffersWithImageUrl(shopId, 50);
        setPictureIds(PictureIdFinder.findPictureId(offers));
    }

    private void setStrategies(long shopId) {
        setIdentifierStrategies(new ArrayList<>());
        if(getParsedOfferRepository().eanFound(shopId)) {
            getIdentifierStrategies().add(new MatchEanStrategy(getParsedOfferRepository()));
        }

        if(getParsedOfferRepository().hanFound(shopId)) {
            getIdentifierStrategies().add(new MatchHanStrategy(getParsedOfferRepository()));
        }
    }

    private void clearState() {
        setShopId(0);
        setPhase((byte)0);
    }

    private void setupState(long shopId, byte phase) {
        setShopId(shopId);
        setPhase(phase);
    }

    private void matchAllByIdentifier(long shopId) {
        ShopOffer offer;
        List<ParsedOffer> parsedOffers = getParsedOfferRepository().getOffers(shopId, 100);
        setImageUrlIdPosition(PictureIdFinder.findPictureId(parsedOffers));
        do {
            offer = getCache().getOffer(shopId, (byte)0);
            matchSingleByIdentifier(shopId, offer);
        } while (offer != null);

    }

    private void matchSingleByIdentifier(long shopId, ShopOffer offer) {
        for(MatchIdentifierStrategy strategy : getIdentifierStrategies()) {
            ParsedOffer match = (offer != null)? strategy.match(shopId, offer) : null;
            if(match != null) {
                if (match.getImageUrl()!= null) {
                    match.setImageId(PictureIdFinder.getImageId(match.getImageUrl(), getPictureIds()));
                }

                match.setBrandName(getBrand(match));
                match.setCategory(getCategory(match));
                saveResult(offer, match, strategy.getMatchingReason());
                deleteShopOfferAndParsedOffer(shopId, offer, match);
                return;
            }
        }
    }

    private String getCategory(ParsedOffer match) {
        if(match.getTitle() != null) {
            Pair<String, Double> pair = getClassifier().getCategory(match);
            return pair.getRight() < getProperties().getLabelThreshold() ? null : pair.getLeft();
        }

        return null;
    }

    private String getBrand(ParsedOffer match) {
        if(match.getTitle() != null) {
            Pair<String, Double> pair = getClassifier().getBrand(match);
            return pair.getRight() < getProperties().getLabelThreshold() ? null : pair.getLeft();
        }

        return null;

    }

    private void saveResult(ShopOffer offer,
                            ParsedOffer match,
                            String matchingReason) {
        MatchingResult result = new MatchingResult(
                offer.getShopId(),
                matchingReason,
                100,
                offer.getOfferKey(),
                offer.getMappedCatalogCategory(),
                offer.getCategoryName(),
                offer.getHigherLevelCategory(),
                offer.getHigherLevelCategoryName(),
                match);

        getMatchingResultRepository().save(offer.getShopId(), result);

    }

    private void deleteShopOfferAndParsedOffer(long shopId, ShopOffer shopOffer, ParsedOffer parsedOffer) {
        getCache().deleteOffer(shopId, shopOffer.getOfferKey());
        getParsedOfferRepository().deleteParsedOffer(shopId, parsedOffer.getUrl());

    }

}