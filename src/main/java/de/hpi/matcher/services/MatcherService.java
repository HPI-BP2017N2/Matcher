package de.hpi.matcher.services;

import de.hpi.machinelearning.PictureIdFinder;
import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.MatchingResult;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.State;
import de.hpi.matcher.persistence.repo.*;
import de.hpi.matcher.properties.MatcherProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.primitives.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Service
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class MatcherService {

    private final Cache cache;
    private final MatcherStateRepository matcherStateRepository;
    private final ParsedOfferRepository parsedOfferRepository;
    private final MatchingResultRepository matchingResultRepository;
    private final ModelRepository modelRepository;
    private final ProbabilityClassifier classifier;
    private final MatcherProperties properties;

    private byte phase = 0;
    private long shopId = 0;
    private List<Integer> pictureIds;
    private List<MatchIdentifierStrategy> identifierStrategies = new ArrayList<>();
    private List<State> remainingStates = new ArrayList<>();

    /**
     * This method gets all shops that were interrupted during matching and continues matching them.
     * @throws Exception when the machine learning classifiers cannot be deserialized
     */
    @PostConstruct
    public void restartInterruptedMatching() throws Exception {
        if(getProperties().isCollectTrainingData()) {
            return;
        }
        
        List<State> states;
        states = getMatcherStateRepository().popAllStates();
        if (states != null) {
            setRemainingStates(states);
            for(State state : getRemainingStates()) {
                if (state.getPhase() == (byte) 1) {
                    getCache().updatePhase(state.getShopId(), (byte) (state.getPhase() + 1), state.getPhase());
                }

                matchShop(state.getShopId(), state.getPhase(), state.getImageIds());
                getRemainingStates().remove(state);
            }
        }
    }

    /**
     * This method saves all shops that were not completely matched yet.
     */
    @PreDestroy
    public void saveStates() {
        if(getShopId() != 0) {
            log.info("Interrupt matching for shop {} during phase {} at {} ", getShopId(), getPhase(), new Date());
            getRemainingStates().add(new State(getShopId(), getPhase(), getPictureIds()));
        }
        getMatcherStateRepository().saveAllStates(getRemainingStates());
    }

    /**
     * This method matches all parsed offers in database with their corresponding offers already known by idealo.<br />
     * In phase 0, "safe" matches (EAN or HAN + brand) are found.
     * Phase 1 will use a machine learning classifier to determine the matches.<br />
     * If there are no classifiers stored in the database, the shop ID will be saved for matching later on.
     * @param shopId The ID of the shop
     * @param phase The phase to start in
     * @throws Exception when the machine learning classifiers cannot be deserialized
     */
    public void matchShop(long shopId, byte phase) throws Exception {
        if(getParsedOfferRepository().collectionIsEmpty(shopId)) {
            return;
        }

        log.info("(Re-)Start matching shop {} in phase {} at {}", shopId, phase, new Date());

        setupState(shopId, phase);
        getCache().warmup(shopId);
        setStrategies(shopId);
        setImageIds(shopId);

        if(!getIdentifierStrategies().isEmpty() && phase == (byte) 0){
            log.info("Start matching unique identifiers for shop {} at {}", shopId, new Date());
            matchAllByIdentifier(shopId);
            log.info("Finished matching unique identifiers for shop {} at {}", shopId, new Date());
        }

        setPhase((byte)(getPhase() + 1));
        if(!getModelRepository().allClassifiersExist()) {
            getMatcherStateRepository().saveState(shopId, getPhase(), getPictureIds());
            clearState();
            log.info("Stopped matching shop {} at {} since there are no classifiers.", shopId, new Date());
            return;
        }

        getClassifier().loadModels();

        log.info("Start matching offers with classifier for shop {} at {}", shopId, new Date());
        matchRemaining(shopId);
        log.info("Finished matching offers with classifier for shop {} at {}", shopId, new Date());

        clearState();
        log.info("Finished matching shop {} at {}", shopId, new Date());
    }

    private void matchShop(long shopId, byte phase, List<Integer> imageIds) throws Exception {
        setPictureIds(imageIds);
        matchShop(shopId, phase);
    }

    private void setImageIds(long shopId) {
        if (getPictureIds() == null || getPictureIds().size() == 0) {
            List<ParsedOffer> offers = getParsedOfferRepository().getOffersWithImageUrl(shopId, 50);
            setPictureIds(PictureIdFinder.findPictureId(offers));
        }
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
        setPictureIds(null);
    }

    private void setupState(long shopId, byte phase) {
        setShopId(shopId);
        setPhase(phase);
    }

    private void matchAllByIdentifier(long shopId) {
        ShopOffer offer;

        do {
            offer = getCache().getOffer(shopId, (byte)0);
            matchSingleByIdentifier(shopId, offer);
        } while (offer != null);

        saveNewEanOffers(shopId);
    }

    private void matchRemaining(long shopId) {
        List<ShopOffer> shopOffers = new LinkedList<>();
        ShopOffer offer;
        do {
            offer = getCache().getUnmatchedOffer(shopId, (byte)1);
            shopOffers.add(offer);
        } while (offer != null);

        List<ParsedOffer> parsedOffers = getParsedOfferRepository().getAllOffers(shopId);



        double[][] matchScores = generateScoreMatrix(shopOffers, parsedOffers);
        findAndSaveBestMatches(matchScores, shopOffers, parsedOffers);
    }

    private void matchSingleByIdentifier(long shopId, ShopOffer offer) {
        for(MatchIdentifierStrategy strategy : getIdentifierStrategies()) {
            ParsedOffer match = (offer != null) ? strategy.match(shopId, offer) : null;
            if(match != null) {
                if (match.getImageUrl()!= null) {
                    match.setImageId(PictureIdFinder.getImageId(match.getImageUrl(), getPictureIds()));
                }
                saveMatch(offer, match, strategy.getMatchingReason(), 100);
                deleteShopOfferAndParsedOffer(shopId, offer, match);
                return;
            }
        }
    }

    private void saveNewEanOffers(long shopId) {
        List<ParsedOffer> offersWithEan = getParsedOfferRepository().getOffersWithEan(shopId);

        for(ParsedOffer offer : offersWithEan) {
            saveNewOffer(shopId, offer);
            getParsedOfferRepository().deleteParsedOffer(shopId, offer.getUrl());
        }
    }

    private double[][] generateScoreMatrix(List<ShopOffer> shopOffers, List<ParsedOffer> parsedOffers) {
        double[][] scoreMatrix = new double[parsedOffers.size()][shopOffers.size()];

        for(int i = 0; i < parsedOffers.size(); i++) {
            ParsedOffer parsedOffer = parsedOffers.get(i);
            parsedOffer.setCategory(getCategory(parsedOffer.getTitle()));

            for(int j = 0; j < shopOffers.size(); j++) {
                scoreMatrix[i][j] = getClassifier().getMatchProbability(shopOffers.get(j), parsedOffer, getBrand(parsedOffer.getTitle()));
            }
        }

        return scoreMatrix;
    }

    private void findAndSaveBestMatches(double[][] matchScores, List<ShopOffer> shopOffers, List<ParsedOffer> parsedOffers) {

    }

    private String getCategory(String offerTitle) {
        if(offerTitle != null) {
            Pair<String, Double> pair = getClassifier().getCategory(offerTitle);
            return pair.getRight() < getProperties().getLabelThreshold() ? null : pair.getLeft();
        }

        return null;
    }

    private String getBrand(String offerTitle) {
        if(offerTitle != null) {
            Pair<String, Double> pair = getClassifier().getBrand(offerTitle);
            return pair.getRight() < getProperties().getLabelThreshold() ? null : pair.getLeft();
        }

        return null;
    }

    private void saveMatch(ShopOffer offer,
                           ParsedOffer match,
                           String matchingReason,
                           int confidence) {
        MatchingResult result = new MatchingResult(
                offer.getShopId(),
                matchingReason,
                confidence,
                offer.getOfferKey(),
                offer.getMappedCatalogCategory(),
                offer.getCategoryName(),
                offer.getHigherLevelCategory(),
                offer.getHigherLevelCategoryName(),
                match);

        getMatchingResultRepository().save(offer.getShopId(), result);
    }

    private void saveNewOffer(long shopId, ParsedOffer offer) {
        getMatchingResultRepository().save(shopId, new MatchingResult(shopId, offer));
    }

    private void deleteShopOfferAndParsedOffer(long shopId, ShopOffer shopOffer, ParsedOffer parsedOffer) {
        getCache().deleteOffer(shopId, shopOffer.getOfferKey());
        getParsedOfferRepository().deleteParsedOffer(shopId, parsedOffer.getUrl());
    }

}