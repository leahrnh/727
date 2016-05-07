import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {

    private static LexicalizedParser lp;
    private static GrammaticalStructureFactory gsf;
    private static Semafor semafor;

    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException {

        Timestamp startTime = new Timestamp(new java.util.Date().getTime());

        String trainDir = args[1];
        String testDir = args[2];


        lp = initializeLP(); //Stanford parser
        gsf = initializeGSF(lp); //Stanford Grammatical Structure Factory
        semafor = initializeSemafor(); //Semafor
        boolean useSemafor = true;

        //convert input documents to data structures
        List<Document> trainDocs = getData(trainDir, useSemafor);
        List<Document> testDocs = getData(testDir, useSemafor);

        ScoreCalculator scoreCalculate = new ScoreCalculator(trainDocs, testDocs);

        if (args[0].equals("train")) {
            scoreCalculate.trainWeights();
        } else if (args[0].equals("test")) {
            //name output
            String outputName = "name_me";
            if (args.length > 3) {
                outputName = args[3];
            }
            scoreCalculate.setScores();
            System.out.println("\n\nTESTING EVALUATION");
            evaluate(testDocs, outputName);
        } else if (args[0].equals("traintest")) {
            scoreCalculate.trainWeights();
            //name output
            String outputName = "name_me";
            if (args.length > 3) {
                outputName = args[3];
            }
            scoreCalculate.setScores();
            System.out.println("\n\nTESTING EVALUATION");
            evaluate(testDocs, outputName);
        }
        else {
            System.err.println("Cannot deal with argument " + args[0]);
        }


        System.out.println("Started at " + startTime);
        System.out.println("Finishing at " + new Timestamp(new java.util.Date().getTime()));
    }

    /**
     * Go through the given directory, and convert files into Documents
     */
    private static List<Document> getData(String inputdir, boolean useSemafor) throws URISyntaxException, IOException, ClassNotFoundException {
        List<Document> docs = new ArrayList();
        File folder = new File(inputdir);
        // PRINT :Reading File Directory
        System.out.println("Reading files from " + inputdir);
        File[] listOfFiles = folder.listFiles();
        int n = 1;
        for (File file : listOfFiles) {
            String filename = file.toString();
            String ext = FilenameUtils.getExtension(filename);
            if (file.isFile() && ext.equals("question")) {
                // PRINT :File names Read
                System.out.println("Processing file " + n);
                n++;
                System.out.println("Filename: " + file.getName());
                Document d = new Document(file.getAbsolutePath(), lp, gsf, semafor, useSemafor);
                docs.add(d);
            }
        }
        return docs;
    }



    /**
     * Based on how scored entities compare to the correct answer, print out evaluation metrics
     */
    private static void evaluate(List<Document> docs, String outputName) {

        //output file
        File outputFile = new File("src/main/resources/results/" + outputName + ".csv");
        FileWriter fw = null;
        try {
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            fw = new FileWriter(outputFile.getAbsoluteFile());

            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("filename, correct?, chosen entity, MRR");

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
                for (int i = 0; i < rankedEntities.size(); i++) {
                    if (rankedEntities.get(i).getCode().equals(answer.getCode())) {
                        reciprocalRank = 1.0 / (i + 1);
                        sumReciprocalRank += reciprocalRank;
                    }
                }
                System.out.println("\nFile: " + doc.getId());
                System.out.println("Correct? " + correct);
                System.out.println("Reciprocal rank: " + reciprocalRank);
                bw.write("\n" + doc.getId() + "," + correct + "," + rankedEntities.get(0).getCode() + "," + reciprocalRank);
                for (Entity entity : rankedEntities) {
                    System.out.println(entity.getScore() + "\t" + entity.getCode() + "\t" + entity.getWord());
                }

            }
            //take and report means
            double percentCorrect = (double) numCorrect / docs.size();
            double meanReciprocalRank = sumReciprocalRank / docs.size();

            System.out.println("\n\nPercent correct: " + percentCorrect);
            System.out.println("Mean reciprocal rank: " + meanReciprocalRank);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static GrammaticalStructureFactory initializeGSF(LexicalizedParser lp) {
        GrammaticalStructureFactory gsf = null;
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }
        return gsf;
    }


    private static LexicalizedParser initializeLP() {
        LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        return lp;
    }

    private static Semafor initializeSemafor() {
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


}
