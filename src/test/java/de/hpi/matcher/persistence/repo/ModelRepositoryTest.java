package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ScoredModel;
import de.hpi.matcher.persistence.SerializedParagraphVectors;
import de.hpi.matcher.properties.RetryProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class ModelRepositoryTest {

    @Getter(AccessLevel.PRIVATE) private final static String MODEL_TYPE = "modelType";
    @Getter(AccessLevel.PRIVATE) private final static String CATEGORY_IDENTIFIER = "category";
    @Getter(AccessLevel.PRIVATE) private final static String BRAND_IDENTIFIER ="brand";
    @Getter(AccessLevel.PRIVATE) private final static String EXAMPLE_TITLE = "abc";
    @Getter(AccessLevel.PRIVATE) private final static String EXAMPLE_LABEL = "abc";
    @Getter(AccessLevel.PRIVATE) private final static double EXAMPLE_SCORE = 0.5;

    private final RetryProperties retryProperties = new RetryProperties();

    private SerializedParagraphVectors exampleCategoryClassifier;
    private SerializedParagraphVectors exampleBrandClassifier;
    private ScoredModel exampleScoredModel;
    private ParagraphVectors paragraphVectors;
    private ModelRepository modelRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Before
    public void setup() {
        initMocks(this);
        setExampleScoredModel(new ScoredModel(null, getMODEL_TYPE(), getEXAMPLE_SCORE()));
        setExampleBrandClassifier(new SerializedParagraphVectors(null, getBRAND_IDENTIFIER()));
        setExampleCategoryClassifier(new SerializedParagraphVectors(null, getCATEGORY_IDENTIFIER()));

        setModelRepository(new ModelRepositoryImpl(getMongoTemplate(), getRetryProperties()));

        getRetryProperties().setModelGeneratingBaseRetry(1);
        getRetryProperties().setModelGeneratingMaxAttempts(1);
        getRetryProperties().setModelGeneratingRetryMultiplier(1);
    }

    @Test
    public void getCategoryClassifier() throws IOException {
        doReturn(getExampleCategoryClassifier()).when(getMongoTemplate()).findOne(any(Query.class), eq(SerializedParagraphVectors.class));
        SerializedParagraphVectors vectors = getModelRepository().getCategoryClassifier();
        verify(getMongoTemplate()).findOne(any(Query.class), eq(SerializedParagraphVectors.class));
        assertEquals(getExampleCategoryClassifier(), vectors);
    }

    @Test
    public void getBrandClassifier() throws IOException {
        doReturn(getExampleBrandClassifier()).when(getMongoTemplate()).findOne(any(Query.class), eq(SerializedParagraphVectors.class));
        SerializedParagraphVectors vectors = getModelRepository().getBrandClassifier();
        verify(getMongoTemplate()).findOne(any(Query.class), eq(SerializedParagraphVectors.class));
        assertEquals(getExampleBrandClassifier(), vectors);
    }

    @Test
    public void getModel() throws IOException{
        doReturn(getExampleScoredModel()).when(getMongoTemplate()).findOne(any(Query.class), eq(ScoredModel.class));
        ScoredModel model = getModelRepository().getModel();
        verify(getMongoTemplate()).findOne(any(Query.class), eq(ScoredModel.class));
        assertEquals(getExampleScoredModel(), model);
    }

    @Test (expected = IOException.class)
    public void doNotGetModel() throws IOException {
        doReturn(null).when(getMongoTemplate()).findOne(any(Query.class), eq(ScoredModel.class));
        getModelRepository().getModel();
        verify(getMongoTemplate(), times(getRetryProperties().getModelGeneratingMaxAttempts()))
                .findOne(any(Query.class), eq(ScoredModel.class));

    }

    @Test
    public void getModelAfterRetry() throws IOException {
        getRetryProperties().setModelGeneratingMaxAttempts(2);
        doReturn(null, getExampleScoredModel()).when(getMongoTemplate()).findOne(any(Query.class), eq(ScoredModel.class));
        ScoredModel model = getModelRepository().getModel();
        verify(getMongoTemplate() ,times(getRetryProperties().getModelGeneratingMaxAttempts()))
                .findOne(any(Query.class), eq(ScoredModel.class));
        assertEquals(getExampleScoredModel(), model);
    }
}