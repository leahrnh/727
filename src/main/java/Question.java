/**
 * The cloze sentence for a passage including a @placeholder entity
 */
public class Question {
    private String text;
    private Sentence sentence;

    public Question(String question) {
        this.text = question;
        this.sentence = new Sentence(question);
    }

    public String getText() {
        return text;
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
