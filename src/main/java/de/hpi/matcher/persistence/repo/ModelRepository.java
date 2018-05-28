package de.hpi.matcher.persistence.repo;

import de.hpi.machinelearning.persistence.ScoredModel;
import de.hpi.machinelearning.persistence.SerializedParagraphVectors;

import java.io.IOException;

public interface ModelRepository {

    boolean categoryClassifierExists();
    boolean brandClassifierExists();
    boolean modelExists();
    SerializedParagraphVectors getCategoryClassifier() throws IOException;
    SerializedParagraphVectors getBrandClassifier() throws IOException;
    ScoredModel getModel() throws IOException;

}
