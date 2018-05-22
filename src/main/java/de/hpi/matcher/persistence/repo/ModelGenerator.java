package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.dto.ScoredModel;
import de.hpi.matcher.properties.CacheProperties;
import de.hpi.matcher.properties.ModelGeneratorProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ModelGenerator {

    private final RestTemplate restTemplate;

    private final ModelGeneratorProperties properties;

    @Autowired
    public ModelGenerator(RestTemplateBuilder restTemplateBuilder, ModelGeneratorProperties properties) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000))
    public ScoredModel getModel() {
        return getRestTemplate().getForObject(getModelURI(), ScoredModel.class);
    }

    private URI getModelURI() {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getGetModelRoute())
                .build()
                .encode()
                .toUri();
    }

}
