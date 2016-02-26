import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        //convert input documents to data structures
        String inputdir = args[0];
        List<Document> docs = getData(inputdir);

        //create a list of all the scoring methods (represented by the abstract class Scorer), which will be applied to the data
        List<Scorer> scorers = new ArrayList();
        scorers.add(new DummyScorer());
        //TODO create more complex/sophisticated scorers

        //iterate over the docs, finding a score for each entity
        for (Document doc : docs) {
            Set<Entity> entities = doc.getEntities();
            for (Entity entity : entities) {
                List<Double> scores = new ArrayList();
                for (Scorer scorer: scorers) {
                    scores.add(scorer.getScore(entity, doc));
                }
                //the score is stored as part of the entity object
                entity.setScore(combineScores(scores));
            }
        }
        //perform all the evaluation in this method
        evaluate(docs);
    }

    /**
     * Go through the given directory, and convert files into Documents
     */
    private static List<Document> getData(String inputdir) {
        List<Document> docs = new ArrayList();
        File folder = new File(inputdir);
        System.out.println("Reading files from " + inputdir);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            String filename = file.toString();
            if (file.isFile() && !file.getName().equals(".DS_Store")) {
                System.out.println("Filename: " + file.getName());
                Document d = new Document(file.getAbsolutePath());
                docs.add(d);
            }
        }
        return docs;
    }

    /**
     * Given a list of scores, combine them into a single score
     */
    private static double combineScores(List<Double> scores) {
        //TODO combine scores intelligently
        return scores.get(0);
    }

    /**
     * Based on how scored entities compare to the correct answer, print out evaluation metrics
     */
    private static void evaluate(List<Document> docs) {
        int numCorrect = 0;
        double sumReciprocalRank = 0;
        for (Document doc : docs) {
            List<Entity> rankedEntities = doc.rankEntities();
            Entity answer = doc.getAnswer();
            //count number where top-ranked entity is correct
            if (rankedEntities.get(0).getCode().equals(answer.getCode())) {
                numCorrect++;
            }
            //calculate reciprocal rank for each entity
            //for more info on mean reciprocal rank, see https://en.wikipedia.org/wiki/Mean_reciprocal_rank
            for(int i=0;i<rankedEntities.size();i++) {
                if (rankedEntities.get(i).getCode().equals(answer.getCode())) {
                    sumReciprocalRank += 1.0 / i;
                }
            }
        }
        //take and report means
        double percentCorrect = (double)numCorrect / docs.size();
        double meanReciprocalRank = sumReciprocalRank / docs.size();

        System.out.println("Percent correct: " + percentCorrect);
        System.out.println("Mean reciprocal rank: " + meanReciprocalRank);
    }


}
