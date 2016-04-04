import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by shrimai on 4/1/16.
 */
public class ScoreCalculator {
    static ArrayList<Scorer> scorersList = new ArrayList<Scorer>();
    private static List<Document> DocumentList;

    public ScoreCalculator(List<Document> docs){
        this.DocumentList = docs;
    }
    //create a list of all the scoring methods (represented by the abstract class Scorer), which will be applied to the data
    //TODO create more complex/sophisticated scorers
    public static ArrayList<Scorer> makeList() {
        //scorersList.add(new WordcountScorer());
        //scorersList.add(new SemaforScorer());
        scorersList.add(new SentenceToVector(DocumentList));
        return scorersList;
    }

    /**
     * Given a list of scores, combine them into a single score
     */
    private static double combineScores(List<Double> scores) {
        //scores are weighted evenly here
        double sum = 0;
        for (Double score : scores) {
            sum += score;
        }
        return sum / scores.size();

        //TODO train weights for scores?
    }

    public static void setScores() {
        ArrayList<Scorer> scorers = ScoreCalculator.makeList();
        int n = 1;

        //iterate over the docs, finding a score for each entity
        for (Document doc : DocumentList) {
            System.out.println("Scoring doc " + n + "/" + DocumentList.size());
            n++;
            Set<Entity> entities = doc.getEntities();
            for (Entity entity : entities) {
                List<Double> scores = new ArrayList();
                for (Scorer scorer : scorers) {
                    scores.add(scorer.getScore(entity, doc));
                }
                //the score is stored as part of the entity object
                entity.setScore(combineScores(scores));
            }
        }
    }
}

