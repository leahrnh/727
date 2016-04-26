import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shrimai on 4/25/16.
 */
public class wordCountandVector extends Scorer{

    private static ArrayList<String> vocabulary = new ArrayList<String>();
    File gModel = new File("/Users/shrimai/Documents/word2vec/GoogleNews-vectors-negative300.bin.gz");
    Word2Vec vec;
    {
        try {
            vec = (Word2Vec) WordVectorSerializer.loadGoogleModel(gModel, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public double getScore(Entity entity, Document doc) {
        double score=0.0;
        Integer sentenceLength = 0;
        //String[] sentences = doc.getPassage().getText().split("\\.");
        List<Sentence> sentences = doc.getPassage().getSentences();
        String[] questionWords = doc.getQuestion().getText().split("\\s");
        for (Sentence sentence : sentences) {
            String sentText = sentence.getText();
            List<String> sentenceWords = Arrays.asList(sentText.split("\\s"));
            //Only look at sentences that contain the target entity
            if (sentenceWords.contains(entity.getCode())) {
                sentenceLength += sentenceWords.size();
                for (String word : questionWords) {
                    //If the target entity occurs in the question, give it a score of 0
                    if (word.equals(entity.getCode())) {
                        return 0.0;
                    }
                        //count words that are in both the question and a sentence containing the target entity
                    for (String wordTocompare: sentenceWords) {
                        score += vec.similarity(wordTocompare, word);

                    }
                }
            }
        }
        System.out.println(score);
        return score;
    }

    private void writeWords(String wordToWrite) {

        //CHANGE FILEPATH
        File file = new File("/Users/shrimai/Documents/Sem2/11727/727/src/main/resources/wordVector/");
        String new_name = file + "wordList.txt";
        System.out.println(new_name);
        File sentenceFile = new File(new_name);
        FileWriter fw = null;

        try {
            fw = new FileWriter(sentenceFile.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(wordToWrite);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    public void initializeScorer(Document document) {
        /*
        // PRINTS all the words in a file
        List<Sentence> sentences = document.getPassage().getSentences();
        String[] questionWords = document.getQuestion().getText().split("\\s");
        for (Sentence sentence : sentences) {
            String sentText = sentence.getText();
            List<String> sentenceWords = Arrays.asList(sentText.split("\\s"));

            for (String word: sentenceWords){

                if (vocabulary.isEmpty()){
                    vocabulary.add(word.toLowerCase());
                    writeWords(word.toLowerCase());
                }
                else if (vocabulary.contains(word.toLowerCase())) {
                    continue;
                }
                else if (word.toLowerCase().matches("@entity[0-9]+") || word.toLowerCase().matches("@placeholder")) {
                    continue;
                }
                else {
                    vocabulary.add(word.toLowerCase());
                    writeWords(word.toLowerCase());
                }
                //System.out.println(word);
            }
        }

        for (String word: questionWords) {
            if (vocabulary.contains(word.toLowerCase())) {
                continue;
            }
            else {
                vocabulary.add(word.toLowerCase());
                writeWords(word.toLowerCase());
            }
        }*/
        return;
    }
}

