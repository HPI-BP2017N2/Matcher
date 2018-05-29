package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.MatchingResult;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.repo.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    @Getter(AccessLevel.PRIVATE) private static final byte PHASE = 0;

    private final ParsedOffer parsedOffer = new ParsedOffer();
    private final ShopOffer shopOffer = new ShopOffer();

    @Mock private ParsedOfferRepository parsedOfferRepository;
    @Mock private MatchingResultRepository matchingResultRepository;
    @Mock private Cache cache;
    @Mock private ModelRepository modelRepository;
    @Mock private MatcherStateRepository matcherStateRepository;

    private MatcherService service;
    private MatchingResult result;

    @Before
    public void setup() {
        initMocks(this);

        getParsedOffer().setEan(getEXAMPLE_EAN());
        getShopOffer().setEan(getEXAMPLE_EAN());
        getShopOffer().setHan(getEXAMPLE_HAN());
        getShopOffer().setOfferKey(getEXAMPLE_OFFER_KEY());
        getShopOffer().setShopId(getEXAMPLE_SHOP_ID());

        doNothing().when(getMatchingResultRepository()).save(anyLong(), any(MatchingResult.class));
        doNothing().when(getMatcherStateRepository()).saveState(anyLong(), anyByte());

        doReturn(false).when(getModelRepository()).allClassifiersExist();

        setService(new MatcherService(
                getCache(),
                getMatcherStateRepository(),
                getParsedOfferRepository(),
                getMatchingResultRepository(),
                getModelRepository(),
                null,
                null,
                null
        ));

    }

    @Test
    public void matchWithEan() throws Exception {
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
        doReturn(false).when(getParsedOfferRepository()).eanFound(getEXAMPLE_SHOP_ID());
        doReturn(false).when(getParsedOfferRepository()).hanFound(getEXAMPLE_SHOP_ID());

        getService().matchShop(getEXAMPLE_SHOP_ID(), getPHASE());

        verify(getParsedOfferRepository(), never()).getByEan(anyLong(), anyString());
        verify(getParsedOfferRepository(), never()).getByHan(anyLong(), anyString());
    }

}