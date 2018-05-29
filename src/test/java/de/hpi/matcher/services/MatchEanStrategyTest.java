package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.repo.ParsedOfferRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MatchEanStrategyTest {

    @Getter(AccessLevel.PRIVATE) private static final long EXAMPLE_SHOP_ID = 1234;
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_EAN = "1234567890123";
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_EAN_LEADING_ZEROS = "001234567890123";

    private final ParsedOffer parsedOffer = new ParsedOffer();
    private final ShopOffer shopOffer = new ShopOffer();

    @Mock
    private ParsedOfferRepository repository;

    private MatchEanStrategy strategy;

    @Before
    public void setup() {
        getParsedOffer().setEan(getEXAMPLE_EAN());
        getShopOffer().setEan(getEXAMPLE_EAN());

        setStrategy(new MatchEanStrategy(getRepository()));
    }

    @Test
    public void matchExactEan() {
        doReturn(getParsedOffer()).when(getRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        assertEquals(getParsedOffer(), getStrategy().match(getEXAMPLE_SHOP_ID(), getShopOffer()));
    }

    @Test
    public void matchEanWithLeadingZeros(){
        doReturn(getParsedOffer()).when(getRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        assertEquals(getParsedOffer(), getStrategy().match(getEXAMPLE_SHOP_ID(), getShopOffer()));
        verify(getRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());

    }

    @Test
    public void matchDirtyEan(){
        doReturn(null).when(getRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        doReturn(getParsedOffer()).when(getRepository()).getByEanWithVariation(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());

        assertEquals(getParsedOffer(), getStrategy().match(getEXAMPLE_SHOP_ID(), getShopOffer()));
        verify(getRepository()).getByEan(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
        verify(getRepository()).getByEanWithVariation(getEXAMPLE_SHOP_ID(), getEXAMPLE_EAN());
    }
}