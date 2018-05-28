package de.hpi.machinelearning.persistence;

import lombok.*;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.data.annotation.Id;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter(AccessLevel.PRIVATE)
@Setter
@RequiredArgsConstructor
@EqualsAndHashCode
public class SerializedParagraphVectors {

    @Id private final String modelType;
    private final byte[] serializedNeuralNetwork;

    public ParagraphVectors getNeuralNetwork() throws IOException {
        InputStream in = new ByteArrayInputStream(getSerializedNeuralNetwork());
        return VectorSerializer.readParagraphVectors(in);
    }
}
