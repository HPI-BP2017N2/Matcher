package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ScoredModel;
import de.hpi.matcher.persistence.SerializedParagraphVectors;
import de.hpi.matcher.properties.MatcherProperties;
import lombok.AccessLevel;
import lombok.Getter;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import weka.classifiers.Classifier;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@Getter(AccessLevel.PRIVATE)
public class ModelRepositoryImpl implements ModelRepository {

    @Autowired
    @Qualifier(value = "modelTemplate")
    private MongoTemplate mongoTemplate;

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
    public ParagraphVectors getCategoryClassifier() {
        return getMongoTemplate().findById("category", SerializedParagraphVectors.class).getNeuralNetwork();
    }

    @Override
    public ParagraphVectors getBrandClassifier() {
        return getMongoTemplate().findById("brand", SerializedParagraphVectors.class).getNeuralNetwork();
    }

    @Override
    public ScoredModel getModel() {
        return getMongoTemplate().findOne(query(where("_id").exists(true)), ScoredModel.class);
    }

    private boolean classifierExists(String id) {
        return getMongoTemplate().exists(query(where("_id").is(id)), SerializedParagraphVectors.class);
    }
}
