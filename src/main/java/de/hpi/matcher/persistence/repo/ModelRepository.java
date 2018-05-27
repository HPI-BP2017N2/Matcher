package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ScoredModel;
import de.hpi.matcher.persistence.SerializedParagraphVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

import java.io.IOException;

public interface ModelRepository {

    boolean categoryClassifierExists();
    boolean brandClassifierExists();
    boolean modelExists();
    SerializedParagraphVectors getCategoryClassifier() throws IOException;
    SerializedParagraphVectors getBrandClassifier() throws IOException;
    ScoredModel getModel() throws IOException;

}
