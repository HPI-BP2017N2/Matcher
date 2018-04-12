package de.hpi.matcher.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishedShop {

    private long shopId;

    @JsonCreator
    public FinishedShop(@JsonProperty("shopId") long shopId) {
        setShopId(shopId);
    }
}
