package de.hpi.matcher.persistence.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hpi.machinelearning.persistence.ScoredModel;
import de.hpi.machinelearning.persistence.SerializedParagraphVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;

@Repository
public class ModelFileRepository implements ModelRepository {

    private static final String MODEL = "model";
    private static final String BRAND = "brand";
    private static final String CATEGORY = "category";
    private static final String CLASSIFIER_PATH = "/models/";
    private static final String USER_DIR = "user.dir";
    private static final String FILE_EXTENSION = ".json";

    @Override
    public boolean allClassifiersExist() {
        return classifierExists(MODEL) && classifierExists(BRAND) && classifierExists(CATEGORY);
    }

    @Override
    public ParagraphVectors getCategoryClassifier() throws IOException {
        return getClassifier(CATEGORY);
    }

    @Override
    public ParagraphVectors getBrandClassifier() throws IOException {
        return getClassifier(BRAND);
    }

    @Override
    public ScoredModel getModel() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String path = System.getProperty(USER_DIR);
        return mapper.readValue(new File(path + CLASSIFIER_PATH + MODEL + FILE_EXTENSION), ScoredModel.class);
    }

    private boolean classifierExists(String name) {
        String path = System.getProperty(USER_DIR);
        return (new File(path + CLASSIFIER_PATH + name + FILE_EXTENSION)).exists();

    }

    private ParagraphVectors getClassifier(String name) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String path = System.getProperty(USER_DIR);
        SerializedParagraphVectors classifier = mapper.readValue(new File(path + CLASSIFIER_PATH + name + FILE_EXTENSION), SerializedParagraphVectors.class);
        return classifier.getNeuralNetwork();
    }
}
