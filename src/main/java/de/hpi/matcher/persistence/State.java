package de.hpi.matcher.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class State {

    @Id
    private long shopId;
    private byte phase;

    public State(long shopId, byte phase) {
        setPhase(phase);
        setShopId(shopId);
    }

}
