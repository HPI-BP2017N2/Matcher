package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.MatchingResult;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.repo.*;
import de.hpi.matcher.properties.MatcherProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.nd4j.linalg.primitives.Pair;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MatcherServiceTest {

    @Getter(AccessLevel.PRIVATE) private static final long EXAMPLE_SHOP_ID = 1234;
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_EAN = "1234567890123";
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_HAN = "123456";
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_OFFER_KEY = "abc";
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_TITLE = "iPhone7";
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_CATEGORY = "Handy";
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_BRAND= "Apple";
    @Getter(AccessLevel.PRIVATE) private static final double EXAMPLE_LABEL_PROBABILITY = 0.6;
    @Getter(AccessLevel.PRIVATE) private static final byte PHASE = 0;

    private final ParsedOffer parsedOffer = new ParsedOffer();
    private final ShopOffer shopOffer = new ShopOffer();
    private final List<ParsedOffer> parsedOffers = new LinkedList<>();
    private final Pair<String, Double> brandPair = new Pair<>();
    private final Pair<String, Double> categoryPair = new Pair<>();

    @Mock private ParsedOfferRepository parsedOfferRepository;
    @Mock private MatchingResultRepository matchingResultRepository;
    @Mock private Cache cache;
    @Mock private ModelRepository modelRepository;
    @Mock private MatcherStateRepository matcherStateRepository;
    @Mock private ProbabilityClassifier classifier;
    @Mock private MatcherProperties properties;

    private MatcherService service;
    private MatchingResult result;

    @Before
    public void setup() {
        initMocks(this);

        getParsedOffer().setEan(getEXAMPLE_EAN());
        getParsedOffer().setTitle(getEXAMPLE_TITLE());
        getShopOffer().setEan(getEXAMPLE_EAN());
        getShopOffer().setHan(getEXAMPLE_HAN());
        getShopOffer().setOfferKey(getEXAMPLE_OFFER_KEY());
        getShopOffer().setShopId(getEXAMPLE_SHOP_ID());
        getParsedOffers().add(getParsedOffer());

        doNothing().when(getMatchingResultRepository()).save(anyLong(), any(MatchingResult.class));
        doNothing().when(getMatcherStateRepository()).saveState(anyLong(), anyByte(), anyList());

        setService(new MatcherService(
                getCache(),
                getMatcherStateRepository(),
                getParsedOfferRepository(),
                getMatchingResultRepository(),
                getModelRepository(),
                getClassifier(),
                getProperties()
        ));

        getBrandPair().setFirst(getEXAMPLE_BRAND());
        getBrandPair().setSecond(getEXAMPLE_LABEL_PROBABILITY());
        getCategoryPair().setFirst(getEXAMPLE_CATEGORY());
        getCategoryPair().setSecond(getEXAMPLE_LABEL_PROBABILITY());

    }

    @Test
    public void matchWithEan() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(false).when(getModelRepository()).allClassifiersExist();
        doReturn(true).when(getParsedOfferRepository()).eanFound(getEXAMPLE_SHOP_ID());
        doReturn(false).when(getParsedOfferRepository()).hanFound(getEXAMPLE_SHOP_ID());
        doReturn(getShopOffer(), null, null).when(getCache()).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        doReturn(getParsedOffer()).when(getParsedOfferRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());
        verify(getCache(), times(2)).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        verify(getParsedOfferRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        verify(getMatchingResultRepository()).save(anyLong(), any(MatchingResult.class));
    }

    @Test
    public void matchWithHan() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(false).when(getModelRepository()).allClassifiersExist();
        doReturn(false).when(getParsedOfferRepository()).eanFound(getEXAMPLE_SHOP_ID());
        doReturn(true).when(getParsedOfferRepository()).hanFound(getEXAMPLE_SHOP_ID());

        doReturn(getShopOffer(), null, null).when(getCache()).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        doReturn(getParsedOffer()).when(getParsedOfferRepository()).getByHan(getEXAMPLE_SHOP_ID(), getEXAMPLE_HAN());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getCache(), times(2)).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        verify(getParsedOfferRepository()).getByHan(getEXAMPLE_SHOP_ID(), getEXAMPLE_HAN());
        verify(getMatchingResultRepository()).save(anyLong(), any(MatchingResult.class));
    }

    @Test
    public void doNotFindMatch() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(false).when(getModelRepository()).allClassifiersExist();
        doReturn(true).when(getParsedOfferRepository()).eanFound(getEXAMPLE_SHOP_ID());
        doReturn(true).when(getParsedOfferRepository()).hanFound(getEXAMPLE_SHOP_ID());

        doReturn(getShopOffer(), null, null).when(getCache()).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        doReturn(null).when(getParsedOfferRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        doReturn(null).when(getParsedOfferRepository()).getByHan(getEXAMPLE_SHOP_ID(), getEXAMPLE_HAN());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getCache(), times(2)).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        verify(getParsedOfferRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        verify(getParsedOfferRepository()).getByHan(getEXAMPLE_SHOP_ID(), getEXAMPLE_HAN());
        verify(getMatchingResultRepository(), never()).save(anyLong(), any(MatchingResult.class));
    }

    @Test
    public void doNotGetShopOffer() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(false).when(getModelRepository()).allClassifiersExist();
        doReturn(true).when(getParsedOfferRepository()).eanFound(getEXAMPLE_SHOP_ID());
        doReturn(true).when(getParsedOfferRepository()).hanFound(getEXAMPLE_SHOP_ID());

        doReturn(null).when(getCache()).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getCache()).getOffer(getEXAMPLE_SHOP_ID(), getPHASE());
        verify(getParsedOfferRepository(), never()).getByEan(anyLong(), anyString());
        verify(getMatchingResultRepository(), never()).save(anyLong(), any(MatchingResult.class));
    }

    @Test
    public void doNotMatchIdentifiers() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(false).when(getModelRepository()).allClassifiersExist();
        doReturn(false).when(getParsedOfferRepository()).eanFound(getEXAMPLE_SHOP_ID());
        doReturn(false).when(getParsedOfferRepository()).hanFound(getEXAMPLE_SHOP_ID());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getParsedOfferRepository(), never()).getByEan(anyLong(), anyString());
        verify(getParsedOfferRepository(), never()).getByHan(anyLong(), anyString());
    }

    @Test
    public void abortWhenNoParsedOffers() throws Exception {
        doReturn(true).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getCache(), never()).getOffer(anyLong(), anyByte());
        verify(getCache(), never()).getUnmatchedOffer(anyLong(), anyByte());
        verify(getMatcherStateRepository(), times(0)).saveState(eq(getEXAMPLE_SHOP_ID()), eq((byte) (getPHASE() + 1)), anyList());

    }

    @Test
    public void abortWhenNoClassifiers() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(false).when(getModelRepository()).allClassifiersExist();

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getCache(), never()).getUnmatchedOffer(anyLong(), anyByte());
        verify(getClassifier(), never()).loadModels();
        verify(getMatcherStateRepository()).saveState(eq(getEXAMPLE_SHOP_ID()), eq((byte) (getPHASE() + 1)), anyList());
    }

    @Test
    public void askForBrandAndCategoryForNonUniqueMatch() throws Exception {
        doReturn(false).when(getParsedOfferRepository()).collectionIsEmpty(anyLong());
        doReturn(true).when(getModelRepository()).allClassifiersExist();
        doReturn(getParsedOffers()).when(getParsedOfferRepository()).getAllOffers(anyLong());
        doReturn(getBrandPair()).when(getClassifier()).getBrand(anyString());
        doReturn(getCategoryPair()).when(getClassifier()).getCategory(anyString());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getCache(), times(getParsedOffers().size())).getUnmatchedOffer(anyLong(), anyByte());
        verify(getClassifier()).getBrand(anyString());
        verify(getClassifier()).getCategory(anyString());
    }

}