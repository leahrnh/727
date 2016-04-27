import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.core.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by shrimai on 4/1/16.
 */
public class ScoreCalculator {
    static ArrayList<ScoreWeight<Scorer, Double>> scorersList = new ArrayList<ScoreWeight<Scorer, Double>>();
    private static List<Document> TrainDocumentList;
    private static List<Document> TestDocumentList;
    private List<Double> weights;
    private Classifier model;
    private Instances trainingSet;

    class ScoreWeight<S, W> {
        Scorer scorer;
        double weight;

        public ScoreWeight(Scorer scorer, double weight) {
            this.scorer = scorer;
            this.weight = weight;
        }
    }

    //create a list of all the scoring methods (represented by the abstract class Scorer), which will be applied to the data
    public ScoreCalculator(List<Document> trainDocs, List<Document> testDocs){

        //set up document lists
        this.TrainDocumentList = trainDocs;
        this.TestDocumentList = testDocs;

        //initialize parsing models. This is done here so it can be used by different scorers without initializing multiple times.
        //comment this section out if not using relevant scorers
        LexicalizedParser lp = initializeLP(); //Stanford parser
        GrammaticalStructureFactory gsf = initializeGSF(lp); //Stanford Grammatical Structure Factory
        Semafor semafor = initializeSemafor(); //Semafor
        Word2Vec wordToVec = initalizeWord2Vec();

        //set up scorers
        //scorersList.add(new ScoreWeight<Scorer, Double>(new WordcountScorer(), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new SemaforScorer(lp, gsf, semafor), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new PowerloomScorer(), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new DependecyScorer(lp, gsf, semafor), 1.0));
        scorersList.add(new ScoreWeight<Scorer, Double>(new DependecyWordVectorScorer(lp, gsf, semafor, wordToVec), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new SentenceToVector(DocumentList), 0.3));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new wordCountandVector(wordToVec), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new DependecyScorer(lp, gsf, semafor), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new SentenceToVector(TrainDocumentList), 0.3));
    }

    private Word2Vec initalizeWord2Vec() {
        File gModel = new File("src/main/resources/wordVector/GoogleNews-vectors-negative300.bin.gz");
        Word2Vec vec = null;
        {
            try {
                vec = (Word2Vec) WordVectorSerializer.loadGoogleModel(gModel, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return vec;
    }


    /**
     * Given a list of scores, combine them into a single score
     */
    private static double combineScores(Entity entity, double score, double weight) {
        return (entity.getScore() + weight*score);
        //TODO train weights for scores?
    }

    public void trainWeights() {
        weights = new ArrayList<Double>();

        int n = 1; //keep track of doc number for printing purposes

        //Weka instructions: https://weka.wikispaces.com/Programmatic+Use
        //set up Weka feature vector
        int numScorers = scorersList.size();
        FastVector wekaAttributes = new FastVector(numScorers + 1);
        int scorerNum = 0;
        for (ScoreWeight scorer : scorersList) {
            String scorerName = "Scorer" + scorerNum;
            scorerNum++;
            Attribute attribute = new Attribute(scorerName);
            wekaAttributes.addElement(attribute);
        }
        //classification attribute
        FastVector classVal = new FastVector(2);
        classVal.addElement("positive");
        classVal.addElement("negative");
        Attribute classAttribute = new Attribute("classVal", classVal);
        wekaAttributes.addElement(classAttribute);


        //iterate over docs, create a list of scores (i.e. features) for each entity
        //convert features into Weka Instances

        // Create an empty training set
        trainingSet = new Instances("Rel", wekaAttributes, 10);
        // Set class index (the index of the feature you're trying to identify)
        trainingSet.setClassIndex(numScorers);

        for (Document doc : TrainDocumentList) {

            // PRINT :Scoring the document
            System.out.println("Training doc " + n + "/" + TrainDocumentList.size());
            n++;

            //identify ground truth
            Entity truth = doc.getAnswer();

            //initialize scorers based on the doc
            for (ScoreWeight current_scorer : scorersList) {
                //initialize scorer
                current_scorer.scorer.initializeScorer(doc);
            }

            //create an Instance for each entity
            Set<Entity> entities = doc.getEntities();
            for (Entity entity : entities) {
                Instance instance = new DenseInstance(numScorers + 1);

                //scorer features
                for (int i=0;i<scorersList.size();i++) {
                    ScoreWeight scorer = scorersList.get(i);
                    Double score = scorer.scorer.getScore(entity, doc);
                    instance.setValue((Attribute)wekaAttributes.elementAt(i), score);
                }

                //class feature
                boolean isTrue = truth.getCode().equals(entity.getCode());
                if (isTrue) {
                    instance.setValue((Attribute) wekaAttributes.elementAt(numScorers), "positive");
                } else {
                    instance.setValue((Attribute) wekaAttributes.elementAt(numScorers), "negative");
                }
                trainingSet.add(instance);
            }
        }

        //Now that we have all our training examples, train a model
        //model = new NaiveBayes();
        model = new Logistic();
        try {
            model.buildClassifier(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setScores() {
        int n = 1;

        //set up Weka feature vector
        int numScorers = scorersList.size();
        FastVector wekaAttributes = new FastVector(numScorers + 1);
        int scorerNum = 0;
        for (ScoreWeight scorer : scorersList) {
            String scorerName = "Scorer" + scorerNum;
            scorerNum++;
            Attribute attribute = new Attribute(scorerName);
            wekaAttributes.addElement(attribute);
        }
        //classification attribute
        FastVector classVal = new FastVector(2);
        classVal.addElement("positive");
        classVal.addElement("negative");
        Attribute classAttribute = new Attribute("classVal", classVal);
        wekaAttributes.addElement(classAttribute);

        for (Document doc : TestDocumentList) {

            // PRINT :Scoring the document
            System.out.println("Scoring doc " + n + "/" + TestDocumentList.size());
            n++;

            //initialize scorers based on the doc
            for (ScoreWeight current_scorer : scorersList) {
                //initialize scorer
                current_scorer.scorer.initializeScorer(doc);
            }

            //create an Instance for each entity
            Set<Entity> entities = doc.getEntities();
            for (Entity entity : entities) {
                Instance instance = new DenseInstance(numScorers + 1);

                //scorer features
                for (int i = 0; i < scorersList.size(); i++) {
                    ScoreWeight current_scorer = scorersList.get(i);
                    Double score = current_scorer.scorer.getScore(entity, doc);

                    instance.setValue(i, score);
                    //These two lines should work the same as the one above, but they don't for unknown reasons
                    //Attribute element = (Attribute) wekaAttributes.elementAt(i);
                    //instance.setValue(element, score);
                }

                try {
                    //fDistribution is the probability of the instance being in each category
                    //fDistribution[0] = P(positive)
                    //fDistribution[1] = P(negative)
                    instance.setDataset(trainingSet);
                    double[] fDistribution = model.distributionForInstance(instance);
                    entity.setScore(fDistribution[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Semafor initializeSemafor() {
        File modelsLocation = new File("src/main/resources/semafor_models");
        String modelsDir = modelsLocation.getAbsolutePath();
        try {
            Semafor semafor = Semafor.getSemaforInstance(modelsDir);
            return semafor;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GrammaticalStructureFactory initializeGSF(LexicalizedParser lp) {
        GrammaticalStructureFactory gsf = null;
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }
        return gsf;
    }


    private LexicalizedParser initializeLP() {
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        return lp;
    }
}

