package de.hpi.machinelearning.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import weka.classifiers.Classifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@EqualsAndHashCode
public class ScoredModel {

    private byte[] modelByteArray;
    private String modelType;
    private double score;

    @JsonIgnore
    public Classifier getModel() throws Exception {
        InputStream in = new ByteArrayInputStream(getModelByteArray());
        return (Classifier) weka.core.SerializationHelper.read(in);
    }
}
