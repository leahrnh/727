/**
 * An anonymized entity from a passage
 */
public class Entity implements Comparable<Entity>{
    private String code;
    private String word;
    private Double score;

    public Entity(String line) {
        String[] pair = line.split(":");
        this.code = pair[0];
        this.word = pair[1];
        this.score = 0.0;
    }

    public String getCode() {
        return code;
    }

    public String getWord() {
        return word;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "code='" + code + '\'' +
                ", word='" + word + '\'' +
                ", score=" + score +
                '}';
    }

    public int compareTo(Entity e2) {
        double comp = score - e2.getScore();
        if (comp > 0) {
            return 1;
        } else if (comp==0) {
            return 0;
        } else {
            return -1;
        }
    }
}
