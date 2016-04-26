import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;

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
    private static List<Document> DocumentList;

    class ScoreWeight<S, W> {
        Scorer scorer;
        double weight;

        public ScoreWeight(Scorer scorer, double weight) {
            this.scorer = scorer;
            this.weight = weight;
        }
    }

    //create a list of all the scoring methods (represented by the abstract class Scorer), which will be applied to the data
    public ScoreCalculator(List<Document> docs){

        //initialize parsing models. This is done here so it can be used by different scorers without initializing multiple times.
        //comment this section out if not using relevant scorers
        /*LexicalizedParser lp = initializeLP(); //Stanford parser
        GrammaticalStructureFactory gsf = initializeGSF(lp); //Stanford Grammatical Structure Factory
        Semafor semafor = initializeSemafor(); //Semafor
        */

        this.DocumentList = docs;
        //scorersList.add(new ScoreWeight<Scorer, Double>(new WordcountScorer(), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new SemaforScorer(lp, gsf, semafor), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new PowerloomScorer(), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new DependecyScorer(lp, gsf, semafor), 1.0));
        //scorersList.add(new ScoreWeight<Scorer, Double>(new SentenceToVector(DocumentList), 0.3));
        scorersList.add(new ScoreWeight<Scorer, Double>(new wordCountandVector(), 1.0));
    }


    /**
     * Given a list of scores, combine them into a single score
     */
    private static double combineScores(Entity entity, double score, double weight) {
        return (entity.getScore() + weight*score);
        //TODO train weights for scores?
    }

    public static void setScores() {
        int n = 1;
        double score, current_weight;

        //iterate over the docs, finding a score for each entity
        for (Document doc : DocumentList) {
            // PRINT :Scoring the document
            System.out.println("Scoring doc " + n + "/" + DocumentList.size());
            n++;
            Set<Entity> entities = doc.getEntities();
            for (ScoreWeight current_scorer : scorersList) {
                current_scorer.scorer.initializeScorer(doc);
                current_weight = current_scorer.weight;
                for (Entity entity : entities) {
                    score = current_scorer.scorer.getScore(entity, doc);
                    entity.setScore(combineScores(entity, score, current_weight));
                }
                //the score is stored as part of the entity object
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

