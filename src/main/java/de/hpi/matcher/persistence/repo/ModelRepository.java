package de.hpi.matcher.persistence.repo;

import de.hpi.machinelearning.persistence.ScoredModel;
import de.hpi.machinelearning.persistence.SerializedParagraphVectors;
import de.hpi.matcher.properties.RetryProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
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
public class ModelRepository {

    public static final String CATEGORY = "category";
    public static final String BRAND = "brand";
    @Autowired
    @Qualifier(value = "modelTemplate")
    private final MongoTemplate mongoTemplate;

    @Autowired
    private final RetryProperties retryProperties;

    public boolean categoryClassifierExists() {
        return classifierExists("category");
    }

    public boolean brandClassifierExists() {
        return classifierExists("brand");
    }

    public boolean modelExists() {
        return getMongoTemplate().exists(query(where("_id").exists(true)), ScoredModel.class);
    }

    public boolean allClassifiersExist() {
        return categoryClassifierExists() && brandClassifierExists() && modelExists();
    }

    public ParagraphVectors getCategoryClassifier() throws IOException {
        return getNeuralNetworkById(CATEGORY);
    }

    public ParagraphVectors getBrandClassifier() throws IOException {
        return getNeuralNetworkById(BRAND);
    }

    public ScoredModel getModel() {
        return getMongoTemplate().findOne(query(where("_id").exists(true)), ScoredModel.class);
    }

    private boolean classifierExists(String id) {
        return getMongoTemplate().findById(id, SerializedParagraphVectors.class) != null;
    }

    private ParagraphVectors getNeuralNetworkById(String id) throws IOException {
        return getMongoTemplate().findById(id, SerializedParagraphVectors.class).getNeuralNetwork();

    }
}