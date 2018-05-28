package de.hpi.matcher.persistence.repo;

import de.hpi.machinelearning.persistence.ScoredModel;
import de.hpi.machinelearning.persistence.SerializedParagraphVectors;
import de.hpi.matcher.properties.RetryProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.io.IOException;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ModelRepositoryImpl implements ModelRepository {

    @Autowired
    @Qualifier(value = "modelTemplate")
    private final MongoTemplate mongoTemplate;

    @Autowired
    private final RetryProperties retryProperties;

    @Override
    public boolean categoryClassifierExists() {
        return classifierExists("category");
    }

    @Override
    public boolean brandClassifierExists() {
        return classifierExists("brand");
    }

    @Override
    public boolean modelExists() {
        return getMongoTemplate().exists(query(where("_id").exists(true)), ScoredModel.class);
    }

    @Override
    public SerializedParagraphVectors getCategoryClassifier() throws IOException {
        return getClassifierByType("category",
                getNeuralNetworkQueryByType("category"),
                new SerializedParagraphVectors(null, null));
    }

    @Override
    public SerializedParagraphVectors getBrandClassifier() throws IOException {
        return getClassifierByType("brand",
                getNeuralNetworkQueryByType("brand"),
                new SerializedParagraphVectors(null, null));
    }

    @Override
    public ScoredModel getModel() throws IOException {
        return getClassifierByType("model",
                query(where("_id").exists(true)),
                new ScoredModel(null, null, 0.0));
    }

    private boolean classifierExists(String id) {
        return getMongoTemplate().findById(id, SerializedParagraphVectors.class) != null;
    }

    private <T> T getClassifierByType(String type, Query query, T expectedObject) throws IOException {
        int retryTime = getRetryProperties().getModelGeneratingBaseRetry();
        T classifier;

        for(int attempt = 1; attempt <= getRetryProperties().getModelGeneratingMaxAttempts(); attempt++) {
            classifier = getClassifierByType(query, expectedObject);
            if(classifier != null) {
                return classifier;
            }

            try {
                Thread.sleep(retryTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryTime *= getRetryProperties().getModelGeneratingRetryMultiplier();
        }

        throw new IOException("could not get " + type + " classifier");
    }

    private <T> T getClassifierByType(Query query, T resultClass) {
        return (T) getMongoTemplate().findOne(query, resultClass.getClass()) ;
    }

    private Query getNeuralNetworkQueryByType(String type) {
        return query(where("_id").is(type));
    }

}