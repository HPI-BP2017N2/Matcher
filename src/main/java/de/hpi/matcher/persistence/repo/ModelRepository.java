package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ScoredModel;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

public interface ModelRepository {

    boolean categoryClassifierExists();
    boolean brandClassifierExists();
    boolean modelExists();
    ParagraphVectors getCategoryClassifier();
    ParagraphVectors getBrandClassifier();
    ScoredModel getModel();

}
