package de.hpi.machinelearning.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.data.annotation.Id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class SerializedParagraphVectors {

    @Id private String modelType;
    private byte[] serializedNeuralNetwork;

    @JsonIgnore
    public ParagraphVectors getNeuralNetwork() throws IOException {
        InputStream in = new ByteArrayInputStream(getSerializedNeuralNetwork());
        return VectorSerializer.readParagraphVectors(in);
    }
}
