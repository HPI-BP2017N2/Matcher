package de.hpi.matcher.services;

import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.VectorsConfiguration;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.tokenization.tokenizer.TokenPreProcess;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.deeplearning4j.models.embeddings.loader.WordVectorSerializer.decodeB64;
import static org.deeplearning4j.models.embeddings.loader.WordVectorSerializer.printOutProjectedMemoryUse;

public class VectorSerializer {

    private final static Logger log = LogManager.getLogger(VectorSerializer.class);

    public static ParagraphVectors readParagraphVectors2(InputStream stream) throws IOException {
        File tmpFile = File.createTempFile("pattern", ".1");
        tmpFile.deleteOnExit();
        FileUtils.copyInputStreamToFile(stream, tmpFile);
        return WordVectorSerializer.readParagraphVectors(tmpFile);
    }

    public static ParagraphVectors readParagraphVectors(InputStream inputStream) throws IOException {
        Word2Vec w2v = readWord2Vec(inputStream);

        // and "convert" it to ParaVec model + optionally trying to restore labels information
        ParagraphVectors vectors = new ParagraphVectors.Builder(w2v.getConfiguration()).vocabCache(w2v.getVocab())
                .lookupTable(w2v.getLookupTable()).resetModel(false).build();

        try (ZipInputStream zipStream = new ZipInputStream(inputStream)) {
            // now we try to restore labels information
            InputStream labelStream = null;

            for (ZipEntry e; (e = zipStream.getNextEntry()) != null;) {
                if (e.getName().equals("labels.txt")) {
                    labelStream = zipStream;
                }
            }

            if (labelStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(labelStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        VocabWord word = vectors.getVocab().tokenFor(decodeB64(line.trim()));
                        if (word != null) {
                            word.markAsLabel(true);
                        }
                    }
                }
            }
        }

        vectors.extractLabels();

        return vectors;
    }

    private static Word2Vec readWord2Vec(InputStream inputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        int originalFreq = Nd4j.getMemoryManager().getOccasionalGcFrequency();
        boolean originalPeriodic = Nd4j.getMemoryManager().isPeriodicGcActive();

        ZipInputStream syn0 = null;
        ZipInputStream syn1 = null;
        ZipInputStream codes = null;
        ZipInputStream huffmann = null;
        ZipInputStream config = null;
        ZipInputStream zsyn1Neg = null;
        ZipInputStream frequencies = null;


        if (originalPeriodic)
            Nd4j.getMemoryManager().togglePeriodicGc(false);

        Nd4j.getMemoryManager().setOccasionalGcFrequency(50000);

        for (ZipEntry e; (e = zipInputStream.getNextEntry()) != null;) {
            if (e.getName().equals("syn0.txt")) {
                syn0 = zipInputStream;
            } else if (e.getName().equals("syn1.txt")) {
                syn1 = zipInputStream;
            } else if (e.getName().equals("codes.txt")) {
                codes = zipInputStream;
            } else if (e.getName().equals("huffmann.txt")) {
                huffmann = zipInputStream;
            } else if (e.getName().equals("config.json")) {
                config = zipInputStream;
            } else if (e.getName().equals("frequencies.txt")) {
                frequencies = zipInputStream;
            } else if (e.getName().equals("syn1Neg.txt")) {
                zsyn1Neg = zipInputStream;
            }
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(config))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        String test = builder.toString().trim();
        System.out.println(test.length());

        VectorsConfiguration configuration = VectorsConfiguration.fromJson(test);

        // we read first 4 files as w2v model
        Word2Vec w2v = readWord2VecFromText(syn0, syn1, codes, huffmann, configuration);

        // we read frequencies from frequencies.txt, however it's possible that we might not have this file
        if (frequencies != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(frequencies))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(" ");
                    VocabWord word = w2v.getVocab().tokenFor(decodeB64(split[0]));
                    word.setElementFrequency((long) Double.parseDouble(split[1]));
                    word.setSequencesCount((long) Double.parseDouble(split[2]));
                }
            }
        }


        if (zsyn1Neg != null) {
            try (InputStreamReader isr = new InputStreamReader(zsyn1Neg);
                 BufferedReader reader = new BufferedReader(isr)) {
                String line = null;
                List<INDArray> rows = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(" ");
                    double array[] = new double[split.length];
                    for (int i = 0; i < split.length; i++) {
                        array[i] = Double.parseDouble(split[i]);
                    }
                    rows.add(Nd4j.create(array));
                }

                    // it's possible to have full model without syn1Neg
                if (!rows.isEmpty()) {
                    INDArray syn1Neg = Nd4j.vstack(rows);
                    ((InMemoryLookupTable) w2v.getLookupTable()).setSyn1Neg(syn1Neg);
                }
            }
        }

        if(originalPeriodic) {
            Nd4j.getMemoryManager().togglePeriodicGc(true);
        }
        Nd4j.getMemoryManager().setOccasionalGcFrequency(originalFreq);

            return w2v;
    }

    private static Word2Vec readWord2VecFromText(@NonNull ZipInputStream vectors, @NonNull ZipInputStream hs, @NonNull ZipInputStream h_codes,
                                                @NonNull ZipInputStream h_points, @NonNull VectorsConfiguration configuration) throws IOException {
        // first we load syn0
        Pair<InMemoryLookupTable, VocabCache> pair = loadTxt(vectors);
        InMemoryLookupTable lookupTable = pair.getFirst();
        lookupTable.setNegative(configuration.getNegative());
        if (configuration.getNegative() > 0)
            lookupTable.initNegative();
        VocabCache<VocabWord> vocab = (VocabCache<VocabWord>) pair.getSecond();

        // now we load syn1
        BufferedReader reader = new BufferedReader(new InputStreamReader(hs, StandardCharsets.UTF_8));
        String line = null;
        List<INDArray> rows = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(" ");
            double array[] = new double[split.length];
            for (int i = 0; i < split.length; i++) {
                array[i] = Double.parseDouble(split[i]);
            }
            rows.add(Nd4j.create(array));
        }
        reader.close();

        // it's possible to have full model without syn1
        if (!rows.isEmpty()) {
            INDArray syn1 = Nd4j.vstack(rows);
            lookupTable.setSyn1(syn1);
        }

        // now we transform mappings into huffman tree points
        reader = new BufferedReader(new InputStreamReader(h_points, StandardCharsets.UTF_8));
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(" ");
            VocabWord word = vocab.wordFor(decodeB64(split[0]));
            List<Integer> points = new ArrayList<>();
            for (int i = 1; i < split.length; i++) {
                points.add(Integer.parseInt(split[i]));
            }
            word.setPoints(points);
        }
        reader.close();


        // now we transform mappings into huffman tree codes
        reader = new BufferedReader(new InputStreamReader(h_codes, StandardCharsets.UTF_8));
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(" ");
            VocabWord word = vocab.wordFor(decodeB64(split[0]));
            List<Byte> codes = new ArrayList<>();
            for (int i = 1; i < split.length; i++) {
                codes.add(Byte.parseByte(split[i]));
            }
            word.setCodes(codes);
            word.setCodeLength((short) codes.size());
        }
        reader.close();

        Word2Vec.Builder builder = new Word2Vec.Builder(configuration).vocabCache(vocab).lookupTable(lookupTable)
                .resetModel(false);

        TokenizerFactory factory = getTokenizerFactory(configuration);

        if (factory != null)
            builder.tokenizerFactory(factory);

        Word2Vec w2v = builder.build();

        return w2v;
    }

    private static TokenizerFactory getTokenizerFactory(VectorsConfiguration configuration) {
        if (configuration == null)
            return null;

        if (configuration.getTokenizerFactory() != null && !configuration.getTokenizerFactory().isEmpty()) {
            try {
                TokenizerFactory factory =
                        (TokenizerFactory) Class.forName(configuration.getTokenizerFactory()).newInstance();

                if (configuration.getTokenPreProcessor() != null && !configuration.getTokenPreProcessor().isEmpty()) {
                    TokenPreProcess preProcessor =
                            (TokenPreProcess) Class.forName(configuration.getTokenPreProcessor()).newInstance();
                    factory.setTokenPreProcessor(preProcessor);
                }

                return factory;

            } catch (Exception e) {
                log.error("Can't instantiate saved TokenizerFactory: {}", configuration.getTokenizerFactory());
            }
        }
        return null;
    }

    private static Pair<InMemoryLookupTable, VocabCache> loadTxt(ZipInputStream vectors) {

        BufferedReader reader =new BufferedReader(new InputStreamReader(vectors, StandardCharsets.UTF_8));
        AbstractCache cache = new AbstractCache<>();

        LineIterator iter = IOUtils.lineIterator(reader);
        String line = null;
        boolean hasHeader = false;
        if (iter.hasNext()) {
            line = iter.nextLine(); // skip header line
            //look for spaces
            if (!line.contains(" ")) {
                log.debug("Skipping first line");
                hasHeader = true;
            } else {
                // we should check for something that looks like proper word vectors here. i.e: 1 word at the 0 position, and bunch of floats further
                String[] split = line.split(" ");
                try {
                    long[] header = new long[split.length];
                    for (int x = 0; x < split.length; x++) {
                        header[x] = Long.parseLong(split[x]);
                    }
                    if (split.length < 4)
                        hasHeader = true;
                    // now we know, if that's all ints - it's just a header
                    // [0] - number of words
                    // [1] - vectorSize
                    // [2] - number of documents <-- DL4j-only value
                    if (split.length == 3)
                        cache.incrementTotalDocCount(header[2]);

                    printOutProjectedMemoryUse(header[0], (int) header[1], 1);

                    hasHeader = true;

                    try {
                        reader.close();
                    } catch (Exception ex) {
                    }
                } catch (Exception e) {
                    // if any conversion exception hits - that'll be considered header
                    hasHeader = false;

                }
            }

        }

        //reposition buffer to be one line ahead
        if (hasHeader) {
            line = "";
            iter.close();
            reader = new BufferedReader(new InputStreamReader(vectors, StandardCharsets.UTF_8));
            iter = IOUtils.lineIterator(reader);
            iter.nextLine();
        }

        List<INDArray> arrays = new ArrayList<>();
        while (iter.hasNext()) {
            if (line.isEmpty())
                line = iter.nextLine();
            String[] split = line.split(" ");
            String word = decodeB64(split[0]); //split[0].replaceAll(whitespaceReplacement, " ");
            VocabWord word1 = new VocabWord(1.0, word);

            word1.setIndex(cache.numWords());

            cache.addToken(word1);

            cache.addWordToIndex(word1.getIndex(), word);

            cache.putVocabWord(word);

            float[] vector = new float[split.length - 1];

            for (int i = 1; i < split.length; i++) {
                vector[i - 1] = Float.parseFloat(split[i]);
            }

            INDArray row = Nd4j.create(vector);

            arrays.add(row);

            // workaround for skipped first row
            line = "";
        }

        INDArray syn = Nd4j.vstack(arrays);

        InMemoryLookupTable lookupTable =
                (InMemoryLookupTable) new InMemoryLookupTable.Builder().vectorLength(arrays.get(0).columns())
                        .useAdaGrad(false).cache(cache).useHierarchicSoftmax(false).build();
        if (Nd4j.ENFORCE_NUMERICAL_STABILITY)
            Nd4j.clearNans(syn);

        lookupTable.setSyn0(syn);

        iter.close();

        try {
            reader.close();
        } catch (Exception e) {
        }

        return new Pair<>(lookupTable, (VocabCache) cache);
    }

}
