package de.hpi.matcher.persistence;

import lombok.*;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.data.annotation.Id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SerializedParagraphVectors {

    @Id private String modelType;
    private byte[] serializedNeuralNetwork;

    public ParagraphVectors getNeuralNetwork() {
        InputStream in = new ByteArrayInputStream(getSerializedNeuralNetwork());
        ParagraphVectors neuralNetwork = null;
        try {
            neuralNetwork = WordVectorSerializer.readParagraphVectors(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return neuralNetwork;
    }

    public SerializedParagraphVectors(byte[] bytes, String type) {
        this.serializedNeuralNetwork = bytes;
        this.modelType = type;
    }
}
