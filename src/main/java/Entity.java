/**
 * Created by leah on 2/25/16.
 */
public class Entity {
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
}
