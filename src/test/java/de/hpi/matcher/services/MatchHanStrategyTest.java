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
public class MatchHanStrategyTest {

    @Getter(AccessLevel.PRIVATE) private static final long EXAMPLE_SHOP_ID = 1234;
    @Getter(AccessLevel.PRIVATE) private static final String EXAMPLE_HAN = "123456";

    private final ParsedOffer parsedOffer = new ParsedOffer();
    private final ShopOffer shopOffer = new ShopOffer();

    @Mock
    private ParsedOfferRepository repository;

    private MatchHanStrategy strategy;

    @Before
    public void setup() {
        getParsedOffer().setHan(getEXAMPLE_HAN());
        getShopOffer().setHan(getEXAMPLE_HAN());

        doReturn(getParsedOffer()).when(getRepository()).getByHan(getEXAMPLE_SHOP_ID(), getEXAMPLE_HAN());
        setStrategy(new MatchHanStrategy(getRepository()));
    }

    @Test
    public void match() {
        assertEquals(getParsedOffer(), getStrategy().match(getEXAMPLE_SHOP_ID(), getShopOffer()));
    }
}