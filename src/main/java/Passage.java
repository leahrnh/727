/**
 * Stores the text component of the document
 * TODO consider making this class more complex (ex. splitting passages into sentences, associating different entities with different sentences, etc)
 */
public class Passage {
    private String text;
    private String[] sentences;

    public static String[] makeSentences(String fullPassage) {
        return fullPassage.split("\\.");
    }

    public Passage(String fullPassage) {
        this.text = fullPassage;
        this.sentences = makeSentences(fullPassage);
    }

    public String[] getSentences() {
        return sentences;
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
