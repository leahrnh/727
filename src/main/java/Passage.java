import java.util.ArrayList;
import java.util.List;

/**
 * Stores the passage component of the document
*/
public class Passage {
    private String text;
    private List<Sentence> sentences; //set in Document

    public Passage(String fullPassage) {
        this.text = fullPassage;
        List<Sentence> sentenceList = new ArrayList<Sentence>();
        String[] sentenceTexts =  fullPassage.split("(\\.|\\?|!)");
        for (String sentence : sentenceTexts) {
            Sentence s = new Sentence(sentence);
            sentenceList.add(s);
        }
        this.sentences = sentenceList;
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Passage{" +
                "text='" + text + '\'' +
                '}';
    }

}
