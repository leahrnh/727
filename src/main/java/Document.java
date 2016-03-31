import java.io.*;
import java.util.*;

/**
 * Structured object to hold a .question file
 */
public class Document {
    private Passage passage;
    private Question question;
    private Entity answer;
    private Set<Entity> entities;
    private String id;

    /**
     * Constructor that reads components directly from the file
     */
    public Document(String filepath) {
        this.id = filepath;
        String line;
        List<String> lines = new ArrayList<String>();
        try {
            FileReader fileReader = new FileReader(filepath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + filepath + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + filepath + "'");
        }

        this.passage = new Passage(lines.get(2));
        this.question = new Question(lines.get(4));
        this.answer = new Entity(lines.get(6) + ":UNKNOWN"); //this is kind of a hack because the answer code doesn't come with a word
        Set<Entity> es = new HashSet<Entity>();
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

    public String getId() {
        return id;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public List<Entity> rankEntities() {
        List<Entity> ranked = new ArrayList<Entity>();
        ranked.addAll(entities);
        Collections.sort(ranked);
        Collections.reverse(ranked);
        return ranked;
    }
}
