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
import org.nd4j.linalg.primitives.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

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
        State state = null;
        do {
            state = getMatcherStateRepository().popState();
            if (state != null) {
                if (state.getPhase() == (byte) 1) {
                    getCache().updatePhase(state.getShopId(), (byte) (state.getPhase() + 1), state.getPhase());
                }

                matchShop(state.getShopId(), state.getPhase());
            }

        } while (state != null );
    }

    @PreDestroy
    public void saveState() {
        if(getShopId() != 0) {
            getMatcherStateRepository().saveState(getShopId(), getPhase());
        }
    }

    public void matchShop(long shopId, byte phase) throws Exception {
        setupState(shopId, phase);
        getCache().warmup(shopId);
        setStrategies(shopId);
        setImageIds(shopId);

        if(!getIdentifierStrategies().isEmpty()){
            matchAllByIdentifier(shopId);
        }

        if(!getModelRepository().allClassifiersExist()) {
            saveState();
            clearState();
            return;
        }

        setPhase((byte)(getPhase() + 1));
        getClassifier().loadModels();
        matchRemaining(shopId);


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

    private void matchRemaining(long shopId) {
        List<ShopOffer> shopOffers = new LinkedList<>();
        ShopOffer offer = null;
        do {
            offer = getCache().getOffer(shopId, (byte)1);
            shopOffers.add(offer);
        } while (offer != null);

        List<ParsedOffer> parsedOffers = getParsedOfferRepository().getAllOffers(shopId);



        double[][] matchScores = generateScoreMatrix(shopOffers, parsedOffers);
        findAndSaveBestMatches(matchScores, shopOffers, parsedOffers);
    }

    private void matchSingleByIdentifier(long shopId, ShopOffer offer) {
        for(MatchIdentifierStrategy strategy : getIdentifierStrategies()) {
            ParsedOffer match = (offer != null)? strategy.match(shopId, offer) : null;
            if(match != null) {
                if (match.getImageUrl()!= null) {
                    match.setImageId(PictureIdFinder.getImageId(match.getImageUrl(), getPictureIds()));
                }
                saveResult(offer, match, strategy.getMatchingReason());
                deleteShopOfferAndParsedOffer(shopId, offer, match);
                return;
            }
        }
    }

    private double[][] generateScoreMatrix(List<ShopOffer> shopOffers, List<ParsedOffer> parsedOffers) {
        double[][] scoreMatrix = new double[parsedOffers.size()][shopOffers.size()];

        for(int i = 0; i < parsedOffers.size(); i++) {
            ParsedOffer parsedOffer = parsedOffers.get(i);
            parsedOffer.setBrandName(getBrand(parsedOffer.getTitle()));
            parsedOffer.setCategory(getCategory(parsedOffer.getTitle()));

            for(int j = 0; j < shopOffers.size(); j++) {
                scoreMatrix[i][j] = getClassifier().getMatchProbability(shopOffers.get(j), parsedOffer);
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

    public void classify() {
        ShopOffer shopOffer = new ShopOffer();
        ParsedOffer parsedOffer = new ParsedOffer();
        Map<String, String> title = new HashMap<>();
        title.put("0", "iPhone6");
        shopOffer.setTitles(title);
        parsedOffer.setTitle("iPhone7");
        shopOffer.setDescriptions(title);
        parsedOffer.setBrandName("Appe");
        shopOffer.setBrandName("Apple");
        parsedOffer.setPrice("1000");
        Map<String, Double> price = new HashMap<>();
        price.put("0", 1000d);
        shopOffer.setPrices(price);
        shopOffer.setMappedCatalogCategory("12345");
        parsedOffer.setCategory("12345");
        Map<String, String> url = new HashMap<>();
        url.put("0", "http://example.com/123");
        shopOffer.setUrls(url);
        parsedOffer.setUrl("http://example.com/123");
        shopOffer.setImageId("qwerty");
        parsedOffer.setImageUrl( "qwerty");
        parsedOffer.setSku("abc");
        shopOffer.setSku("abc");

        System.out.println(getClassifier().getMatchProbability(shopOffer, parsedOffer));
    }

}