/**
 * Stores the text component of the document
 * TODO consider making this class more complex (ex. splitting passages into sentences, associating different entities with different sentences, etc)
 */
public class Passage {
    private String text;

    public Passage(String fullPassage) {
        this.text = fullPassage;
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
