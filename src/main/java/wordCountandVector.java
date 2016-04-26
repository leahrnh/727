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
    private Word2Vec vec;

    public wordCountandVector(Word2Vec wordToVec) {
        this.vec = wordToVec;
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
        //System.out.println(score);
        return score;
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

