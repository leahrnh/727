/**
 * The cloze sentence for a passage including a @placeholder entity
 */
public class Question {
    private String question;

    public Question(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                '}';
    }
}
