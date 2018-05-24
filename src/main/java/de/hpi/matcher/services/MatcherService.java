package de.hpi.matcher.services;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.MatchingResult;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.State;
import de.hpi.matcher.persistence.repo.Cache;
import de.hpi.matcher.persistence.repo.MatcherStateRepository;
import de.hpi.matcher.persistence.repo.MatchingResultRepository;
import de.hpi.matcher.persistence.repo.ParsedOfferRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MatcherService {

    private byte phase = 0;
    private long shopId = 0;
    private final Cache cache;
    private final MatcherStateRepository matcherStateRepository;
    private final ParsedOfferRepository parsedOfferRepository;
    private final MatchingResultRepository matchingResultRepository;
    private List<MatchIdentifierStrategy> identifierStrategies = new ArrayList<>();
    private List<Integer> imageUrlIdPosition = new ArrayList<>();

    @PostConstruct
    public void restartInterruptedMatching() {
        State state = getMatcherStateRepository().popState();
        if(state != null) {
            matchShop(state.getShopId(), state.getPhase());
        }
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

        if(!getIdentifierStrategies().isEmpty()){
            matchAllByIdentifier(shopId);
        }
        setPhase((byte)(getPhase() + 1));


        clearState();
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
                    String id = "";
                    String[] urlParts = PictureIdFinder.splitUrl(match.getImageUrl());
                    for (int index : getImageUrlIdPosition()) {
                        id = id.concat(urlParts[index]);
                    }
                    match.setImageUrl(id);
                }
                saveResult(offer, match, strategy.getMatchingReason());
                deleteShopOfferAndParsedOffer(shopId, offer, match);
                return;
            }
        }
    }

    private void saveResult(ShopOffer offer,
                            ParsedOffer match,
                            String matchingReason) {
        MatchingResult result = new MatchingResult(
                offer.getShopId(),
                matchingReason,
                100,
                offer.getOfferKey(),
                offer,
                match);

        getMatchingResultRepository().save(offer.getShopId(), result);

    }

    private void deleteShopOfferAndParsedOffer(long shopId, ShopOffer shopOffer, ParsedOffer parsedOffer) {
        getCache().deleteOffer(shopId, shopOffer.getOfferKey());
        getParsedOfferRepository().deleteParsedOffer(shopId, parsedOffer.getUrl());

    }
}