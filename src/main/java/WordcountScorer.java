import java.util.Arrays;
import java.util.List;

/**
 * Score based on overlap between words in the question and words in sentences containing that entity.
 * Exclude entities in the question itself.
 */
public class WordcountScorer extends Scorer {

    @Override
    public double getScore(Entity entity, Document doc) {
        double score=0.0;
        //String[] sentences = doc.getPassage().getText().split("\\.");
        List<Sentence> sentences = doc.getPassage().getSentences();
        String[] questionWords = doc.getQuestion().getText().split("\\s");
        for (Sentence sentence : sentences) {
            String sentText = sentence.getText();
            List<String> sentenceWords = Arrays.asList(sentText.split("\\s"));
            //Only look at sentences that contain the target entity
            if (sentenceWords.contains(entity.getCode())) {
                for (String word : questionWords) {
                    //If the target entity occurs in the question, give it a score of 0
                    if (word.equals(entity.getCode())) {
                        return 0.0;
                    }
                    if (sentenceWords.contains(word)) {
                        //count words that are in both the question and a sentence containing the target entity
                        score += 1.0;
                    }
                }
            }
        }
        return score/100;
    }

    public void initializeScorer(Document document) {return;}
}
