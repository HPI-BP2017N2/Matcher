package de.hpi.matcher.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import weka.classifiers.Classifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class ScoredModel {

    private byte[] modelByteArray;
    private String modelType;
    private double score;

    public Classifier getModel() {
        InputStream in = new ByteArrayInputStream(getModelByteArray());
        Classifier model = null;
        try {
            model = (Classifier) weka.core.SerializationHelper.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

}
