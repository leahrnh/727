/**
 * The cloze sentence for a passage including a @placeholder entity
 */
public class Question {
    private String text;

    public Question(String question) {
        this.text = question;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                '}';
    }
}
