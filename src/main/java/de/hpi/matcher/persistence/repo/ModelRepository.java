package de.hpi.matcher.persistence.repo;

import de.hpi.machinelearning.persistence.ScoredModel;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;

import java.io.IOException;

public interface ModelRepository {

    boolean allClassifiersExist();

    ParagraphVectors getCategoryClassifier() throws IOException;

    ParagraphVectors getBrandClassifier() throws IOException;

    ScoredModel getModel() throws IOException;
}
