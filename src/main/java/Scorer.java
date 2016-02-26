/**
 * Calculate a score that the given entity is the "placeholder" for the question associated with the doc
 */
public abstract class Scorer {

    public abstract double getScore(Entity entity, Document doc);
}
