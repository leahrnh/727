/**
 * Stores the passage component of the document
 * TODO consider making this class more complex (ex. splitting passages into sentences, associating different entities with different sentences, etc)
 */
public class Passage {
    private String passage;

    public Passage(String fullPassage) {
        this.passage = fullPassage;
    }

    public String getPassage() {
        return passage;
    }
}
