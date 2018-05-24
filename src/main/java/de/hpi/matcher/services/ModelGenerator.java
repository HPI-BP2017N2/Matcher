package de.hpi.matcher.services;

import de.hpi.matcher.persistence.ScoredModel;
import de.hpi.matcher.persistence.SerializedParagraphVectors;
import de.hpi.matcher.properties.ModelGeneratorProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Service
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
            maxAttempts = 6,
            backoff = @Backoff(delay = 5000, multiplier = 5))
    public ScoredModel getModel() {
        return getRestTemplate().getForObject(getModelURI(), ScoredModel.class);
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 6,
            backoff = @Backoff(delay = 5000, multiplier = 5))
    public ParagraphVectors getCategoryClassifier() {
        return getRestTemplate().getForObject(getCategoryClassifierURI(), SerializedParagraphVectors.class).getNeuralNetwork();
    }

    @Retryable(
            value = {HttpClientErrorException.class },
            maxAttempts = 6,
            backoff = @Backoff(delay = 5000, multiplier = 5))
    public ParagraphVectors getBrandClassifier() {
        return getRestTemplate().getForObject(getBrandClassifierURI() , SerializedParagraphVectors.class).getNeuralNetwork();
    }


    private URI getModelURI() {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getModelRoute())
                .build()
                .encode()
                .toUri();
    }

    private URI getCategoryClassifierURI() {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getCategoryClassifierRoute())
                .build()
                .encode()
                .toUri();
    }

    private URI getBrandClassifierURI() {
        return UriComponentsBuilder.fromUriString(getProperties().getUri())
                .path(getProperties().getBrandClassifierRoute())
                .build()
                .encode()
                .toUri();
    }

}
