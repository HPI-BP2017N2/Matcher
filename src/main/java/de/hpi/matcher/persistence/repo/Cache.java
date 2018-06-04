package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.properties.CacheProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Repository
public class Cache {

    private final RestTemplate restTemplate;
    private final CacheProperties properties;

    @Autowired
    public Cache(RestTemplateBuilder restTemplateBuilder, CacheProperties cacheProperties) {
        this.properties = cacheProperties;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void warmup(long shopId) {
        getRestTemplate().getForObject(warmupURI(shopId), Object.class);
    }
    

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000, multiplier = 5))
    public ShopOffer getOffer(long shopId, byte phase) {
        return getRestTemplate().getForObject(getOffersURI(shopId, phase), ShopOffer.class);
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000, multiplier = 5))
    public ShopOffer getUnmatchedOffer(long shopId, byte phase) {
        return getRestTemplate().getForObject(getUnmatchedOffersURI(shopId, phase), ShopOffer.class);
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000))
    public void deleteOffer(long shopId, String offerKey) {
        getRestTemplate().delete(deleteOfferURI(shopId, offerKey));
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000))
    public void updatePhase(long shopId, byte oldPhase, byte newPhase) {
        getRestTemplate().postForLocation(updatePhaseURI(shopId, oldPhase, newPhase), null);
    }



    public void deleteAll(long shopId) {
        getRestTemplate().delete(deleteAllURI(shopId));
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000))
    private URI warmupURI(long shopID) {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getWarmupRoute() + shopID)
                .build()
                .encode()
                .toUri();
    }

    private URI getOffersURI(long shopID, byte phase) {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getGetOfferRoute() + shopID)
                .queryParam("phase", phase)
                .build()
                .encode()
                .toUri();
    }

    private URI getUnmatchedOffersURI(long shopID, byte phase) {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getGetUnmatchedOfferRoute() + shopID)
                .queryParam("phase", phase)
                .build()
                .encode()
                .toUri();
    }

    private URI deleteOfferURI(long shopID, String offerKey) {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getDeleteOfferRoute() + shopID)
                .queryParam("offerKey", offerKey)
                .build()
                .encode()
                .toUri();
    }

    private URI deleteAllURI(long shopID) {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getDeleteAllRoute() + shopID)
                .build()
                .encode()
                .toUri();
    }

    private URI updatePhaseURI(long shopID, byte oldPhase, byte newPhase) {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getDeleteAllRoute() + shopID)
                .queryParam("oldPhase", oldPhase)
                .queryParam("newPhase", newPhase)
                .build()
                .encode()
                .toUri();
    }

}
