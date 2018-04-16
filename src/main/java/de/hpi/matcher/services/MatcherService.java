package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.MatchingResult;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.State;
import de.hpi.matcher.persistence.repo.MatcherStateRepository;
import de.hpi.matcher.persistence.repo.MatchingResultRepository;
import de.hpi.matcher.persistence.repo.ParsedOfferRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MatcherService {

    private byte phase = 0;
    private long shopId;
    private Cache cache;
    private MatcherStateRepository matcherStateRepository;
    private ParsedOfferRepository parsedOfferRepository;
    private MatchingResultRepository matchingResultRepository;
    private List<IMatchIdentifierStrategy> identifierStrategies = new ArrayList<>();

    @Autowired
    public MatcherService(MatcherStateRepository matcherStateRepository,
                          ParsedOfferRepository parsedOfferRepository,
                          MatchingResultRepository matchingResultRepository,
                          Cache cache){
        setMatcherStateRepository(matcherStateRepository);
        setParsedOfferRepository(parsedOfferRepository);
        setMatchingResultRepository(matchingResultRepository);
        setCache(cache);
        getIdentifierStrategies().add(new MatchEanStrategy(getParsedOfferRepository()));
        getIdentifierStrategies().add(new MatchHanStrategy(getParsedOfferRepository()));
        getIdentifierStrategies().add(new MatchSkuStrategy(getParsedOfferRepository()));

    }


    @PostConstruct
    public void restartInterruptedMatching() {
        State state = getMatcherStateRepository().popState();
        if(state == null) {
            return;
        }

        setPhase(state.getPhase());
        matchShop(state.getShopId(), state.getPhase());
    }

    @PreDestroy
    public void saveState() {
        getMatcherStateRepository().saveState(getShopId(), getPhase());

    }

    public void matchShop(long shopId, byte phase) {
        matchByIdentifiers(shopId);

    }

    private void matchByIdentifiers(long shopId) {
        ShopOffer offer = null;
        do {
            offer = getCache().getOffer(shopId, (byte)0);
            ParsedOffer match = null;
            for(IMatchIdentifierStrategy strategy : getIdentifierStrategies()) {
                match = strategy.match(shopId, offer);
                if(match != null) {
                    saveResultAndDeleteShopOffer(shopId, offer, match, strategy.matchingReason());
                    break;
                }

            }

        } while (offer != null);

    }

    private void saveResultAndDeleteShopOffer(long shopId,
                                              ShopOffer offer,
                                              ParsedOffer match,
                                              String matchingReason) {
        MatchingResult result = new MatchingResult(
                shopId,
                matchingReason,
                100,
                offer.getOfferKey(),
                match);

        getMatchingResultRepository().save(shopId, result);
        getCache().deleteOffer(shopId, offer.getOfferKey());
    }
}