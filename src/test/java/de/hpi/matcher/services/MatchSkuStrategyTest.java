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

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MatchSkuStrategyTest {

    @Getter(AccessLevel.PRIVATE) private static final long EXAMPLE_SHOP_ID = 1234;
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_SKU = "123456";

    private final ParsedOffer parsedOffer = new ParsedOffer();
    private final ShopOffer shopOffer = new ShopOffer();

    @Mock
    private ParsedOfferRepository repository;

    private MatchSkuStrategy strategy;

    @Before
    public void setup() {
        getParsedOffer().setSku(getEXAMPLE_SKU());
        getShopOffer().setSku(getEXAMPLE_SKU());

        doReturn(getParsedOffer()).when(getRepository()).getBySku(getEXAMPLE_SHOP_ID(), getEXAMPLE_SKU());
        setStrategy(new MatchSkuStrategy(getRepository()));
    }

    @Test
    public void match() {
        assertEquals(getParsedOffer(), getStrategy().match(getEXAMPLE_SHOP_ID(), getShopOffer()));
    }
}