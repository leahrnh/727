/**
 * The cloze sentence for a passage including a @placeholder entity
 */
public class Question {
    private String text;
    private Sentence sentence;

    public Question(String question) {
        this.text = question;
    }

    public String getText() {
        return text;
    }

    public void setSentence(Sentence sentence) {
        this.sentence = sentence;
    }

    public Sentence getSentence() {
        return sentence;
    }

    @Override
    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                '}';
    }
}
