import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Document {
    private Passage passage;
    private Question question;
    private Entity answer;
    private Set<Entity> entities;

    /**
     * Constructor taking pre-created components
     */
    public Document(Passage passage, Question question, Entity answer, Set<Entity> entities) {
        this.passage = passage;
        this.question = question;
        this.answer = answer;
        this.entities = entities;
    }

    /**
     * Constructor that reads components directly from the file
     */
    public Document(String filename) {
        String line = null;
        List<String> lines = new ArrayList();
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filename + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + filename + "'");
        }

        this.passage = new Passage(lines.get(2));
        this.question = new Question(lines.get(4));
        this.answer = new Entity(lines.get(6) + ":blank"); //TODO this is kind of a hack because the answer code doesn't come with a word
        Set<Entity> es = new HashSet();
        for (int i=8;i<lines.size();i++) {
            es.add(new Entity(lines.get(i)));
        }
        this.entities = es;
    }

    public Passage getPassage() {
        return passage;
    }

    public Question getQuestion() {
        return question;
    }

    public Entity getAnswer() {
        return answer;
    }

    public Set<Entity> getEntities() {
        return entities;
    }
}
