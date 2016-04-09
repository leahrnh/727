import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException {

        //convert input documents to data structures
        String inputdir = args[0];
        List<Document> docs = getData(inputdir);

        ScoreCalculator scoreCalculate = new ScoreCalculator(docs);
        scoreCalculate.setScores();
        //perform all the evaluation in this method
        evaluate(docs);
    }

    /**
     * Go through the given directory, and convert files into Documents
     */
    private static List<Document> getData(String inputdir) throws URISyntaxException, IOException, ClassNotFoundException {
        List<Document> docs = new ArrayList();
        File folder = new File(inputdir);
        System.out.println("Reading files from " + inputdir);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            String filename = file.toString();
            String ext = FilenameUtils.getExtension(filename);
            if (file.isFile() && ext.equals("question")) {
                System.out.println("Filename: " + file.getName());
                Document d = new Document(file.getAbsolutePath());
                docs.add(d);
            }
        }
        return docs;
    }



    /**
     * Based on how scored entities compare to the correct answer, print out evaluation metrics
     */
    private static void evaluate(List<Document> docs) {
        int numCorrect = 0;
        double sumReciprocalRank = 0;
        for (Document doc : docs) {
            boolean correct = false;
            double reciprocalRank = 0.0;
            List<Entity> rankedEntities = doc.rankEntities();
            Entity answer = doc.getAnswer();
            //count number where top-ranked entity is correct
            if (rankedEntities.get(0).getCode().equals(answer.getCode())) {
                numCorrect++;
                correct = true;
            }
            //calculate reciprocal rank for each entity
            //for more info on mean reciprocal rank, see https://en.wikipedia.org/wiki/Mean_reciprocal_rank
            for(int i=0;i<rankedEntities.size();i++) {
                if (rankedEntities.get(i).getCode().equals(answer.getCode())) {
                    reciprocalRank = 1.0 / (i+1);
                    sumReciprocalRank += reciprocalRank;
                }
            }
            System.out.println("\nFile: " + doc.getId());
            System.out.println("Correct? " + correct);
            System.out.println("Reciprocal rank: " + reciprocalRank);
            for (Entity entity : rankedEntities) {
                System.out.println(entity.getScore() + "\t" + entity.getCode() + "\t" + entity.getWord());
            }

        }
        //take and report means
        double percentCorrect = (double)numCorrect / docs.size();
        double meanReciprocalRank = sumReciprocalRank / docs.size();

        System.out.println("Percent correct: " + percentCorrect);
        System.out.println("Mean reciprocal rank: " + meanReciprocalRank);
    }


}
