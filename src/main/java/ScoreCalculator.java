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
        this.DocumentList = docs;
        //scorersList.add(new ScoreWeight<Scorer, Double>(new WordcountScorer(), 1.0));
        //scorersList.add(new SemaforScorer());
        scorersList.add(new ScoreWeight<Scorer, Double>(new SentenceToVector(DocumentList), 0.3));
    }

    //TODO create more complex/sophisticated scorers


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
}

