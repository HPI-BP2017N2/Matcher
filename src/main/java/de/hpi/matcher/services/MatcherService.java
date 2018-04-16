package de.hpi.matcher.services;

import de.hpi.matcher.persistence.State;
import de.hpi.matcher.persistence.repo.MatcherStateRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MatcherService {

    private byte phase = 0;
    private long shopId;
    @Autowired
    private MatcherStateRepository matcherStateRepository;

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

    }
}