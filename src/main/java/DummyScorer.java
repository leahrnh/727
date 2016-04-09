/**
 * An example implementation of the Scorer class
 */
public class DummyScorer extends Scorer{

    public double getScore(Entity entity, Document doc) {
        return 1;
    }
    public void initializeScorer(Document document){return;}
}
