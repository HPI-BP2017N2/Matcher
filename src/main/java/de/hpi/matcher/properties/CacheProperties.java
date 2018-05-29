package de.hpi.matcher.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("cache")
@Getter
@Setter
@Primary
public class CacheProperties {

    private String uri;
    private String getOfferRoute;
    private String deleteOfferRoute;
    private String deleteAllRoute;
    private String warmupRoute;
    private String getUnmatchedOfferRoute;
    private String updatePhaseRoute;

}
