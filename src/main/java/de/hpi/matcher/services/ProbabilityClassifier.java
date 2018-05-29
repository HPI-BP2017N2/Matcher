package de.hpi.matcher.services;

import de.hpi.machinelearning.LabelSeeker;
import de.hpi.machinelearning.MeansBuilder;
import de.hpi.machinelearning.persistence.FeatureInstance;
import de.hpi.machinelearning.persistence.ScoredModel;
import de.hpi.matcher.dto.ShopOffer;
import de.hpi.matcher.persistence.ParsedOffer;
import de.hpi.matcher.persistence.repo.ModelRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;

import java.io.IOException;
import java.util.List;

@Service
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ProbabilityClassifier {

    private final ModelRepository modelRepository;

    private final ModelGenerator modelGenerator;

    private final TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();

    private ParagraphVectors categoryClassifier;

    private ParagraphVectors brandClassifier;

    private Classifier model;

    private String modelType;

    private double modelScore;

    private MeansBuilder categoryMeansBuilder;

    private MeansBuilder brandMeansBuilder;

    private LabelSeeker categoryLabelSeeker;

    private LabelSeeker brandLabelSeeker;


    public void loadModels() throws Exception {
        startClassifierGeneration();
        getTokenizerFactory().setTokenPreProcessor(new CommonPreprocessor());

        loadCategoryClassifier();
        loadBrandClassifier();
        loadModel();
    }

    public Pair<String, Double> getBrand(String offerTitle) {
        LabelledDocument document = getLabelledDocumentFromTitle(offerTitle);
        INDArray documentAsCentroid = getBrandMeansBuilder().documentAsVector(document);
        List<Pair<String, Double>> scores = getBrandLabelSeeker().getScores(documentAsCentroid);

        return getBestScoredLabel(scores);
    }

    public Pair<String, Double> getCategory(String offerTitle) {
        LabelledDocument document = getLabelledDocumentFromTitle(offerTitle);
        INDArray documentAsCentroid = getCategoryMeansBuilder().documentAsVector(document);
        List<Pair<String, Double>> scores = getCategoryLabelSeeker().getScores(documentAsCentroid);

        return getBestScoredLabel(scores);
    }

    public double getMatchProbability(ShopOffer shopOffer, ParsedOffer parsedOffer) {
        FeatureInstance instance = new FeatureInstance(shopOffer, parsedOffer);
        double[] classifyingProbabilities = null;
        try {
            classifyingProbabilities = getModel().distributionForInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(classifyingProbabilities != null) {
            // first value is match probability, second not-match probability
            return classifyingProbabilities[0];
        }

        return 0.0;
    }


    private void startClassifierGeneration() {
        if(!getModelRepository().categoryClassifierExists()) {
            getModelGenerator().generateCategoryClassifier();
        }

        if(!getModelRepository().brandClassifierExists()) {
            getModelGenerator().generateBrandClassifier();
        }

        if(!getModelRepository().modelExists()) {
            getModelGenerator().generateModel();
        }
    }

    private void loadModel() throws Exception {
        ScoredModel model = getModelRepository().getModel();
        setModel(model.getModel());
        setModelScore(model.getScore());
        setModelType(model.getModelType());

        log.info("Loaded model");
    }

    private void loadBrandClassifier() throws IOException {
        setBrandClassifier(getModelRepository().getBrandClassifier().getNeuralNetwork());
        setBrandMeansBuilder(new MeansBuilder(
                (InMemoryLookupTable<VocabWord>)getBrandClassifier().getLookupTable(),
                getTokenizerFactory()));
        setBrandLabelSeeker(new LabelSeeker(getBrandClassifier().getLabelsSource().getLabels(),
                (InMemoryLookupTable<VocabWord>) getBrandClassifier().getLookupTable()));

        log.info("Loaded brand classifier");
    }

    private void loadCategoryClassifier() throws IOException {
        setCategoryClassifier(getModelRepository().getCategoryClassifier().getNeuralNetwork());
        setCategoryMeansBuilder(new MeansBuilder(
                (InMemoryLookupTable<VocabWord>)getCategoryClassifier().getLookupTable(),
                getTokenizerFactory()));
        setCategoryLabelSeeker(new LabelSeeker(getCategoryClassifier().getLabelsSource().getLabels(),
                (InMemoryLookupTable<VocabWord>) getCategoryClassifier().getLookupTable()));

        log.info("Loaded category classifier");
    }


    private LabelledDocument getLabelledDocumentFromTitle(String offerTitle) {
        LabelledDocument document = new LabelledDocument();
        document.setContent(offerTitle);
        return document;
    }

    private Pair<String, Double> getBestScoredLabel(List<Pair<String, Double>> scores) {
        Double bestScore = Double.MIN_VALUE;
        Pair<String, Double> bestPair = null;

        for(Pair<String, Double> score : scores) {
            if(score.getSecond() > bestScore) {
                bestScore = score.getSecond();
                bestPair = score;
            }
        }

        return bestPair;
    }
}
