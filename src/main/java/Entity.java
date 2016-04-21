import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An anonymized entity from a passage
 */
public class Entity implements Comparable<Entity>{
    private String code;
    private String word;
    private Double score;
    private Integer codeNumber;


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

    public Integer getCodeNumber() {
        if (codeNumber != null) {
            return codeNumber;
        } else {
            //extract code number
            Pattern pattern = Pattern.compile("@entity([0-9]+)");
            Matcher matcher = pattern.matcher(code);
            matcher.find();
            codeNumber = Integer.parseInt(matcher.group(1));
            return codeNumber;
        }
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
