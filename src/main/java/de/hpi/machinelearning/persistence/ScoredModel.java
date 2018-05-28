package de.hpi.machinelearning.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import weka.classifiers.Classifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
@EqualsAndHashCode
public class ScoredModel {

    private  final byte[] modelByteArray;
    private final String modelType;
    private final double score;

    public Classifier getModel() throws Exception {
        InputStream in = new ByteArrayInputStream(getModelByteArray());
        return (Classifier) weka.core.SerializationHelper.read(in);
    }


    /*
    public ScoredModel(byte[] bytes, String modelType, double score) {
        this.modelByteArray = bytes;
        this.modelType = modelType;
        this.score = score;
    }
*/
}
