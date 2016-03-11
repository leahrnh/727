import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.*;

/**
 * Structured object to hold a .question file
 */
public class
Document {
    private Passage passage;
    private Question question;
    private Entity answer;
    private Set<Entity> entities;
    private String id;

    /**
     * Constructor taking pre-created components
     */
    public Document(Passage passage, Question question, Entity answer, Set<Entity> entities) {
        this.id = "unknown";
        this.passage = passage;
        this.question = question;
        this.answer = answer;
        this.entities = entities;
    }

    /**
     * Constructor that reads components directly from the file
     */
    public Document(String filepath) {
        this.id = filepath;
        String line = null;
        List<String> lines = new ArrayList();
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
        Set<Entity> es = new HashSet();
        for (int i=8;i<lines.size();i++) {
            es.add(new Entity(lines.get(i)));
        }
        this.entities = es;

        //use this when you get new data to convert it to a parse-able format
        //writeSentences(filepath, passage, question);
    }

    /**
     * Create a new file, filename.sentences, that just contains lines made of individual sentences (1 sentence per line)
     */
    private void writeSentences(String filepath, Passage passage, Question question) {
        File file = new File(filepath);
        String newname = file.getParent() + "/" + FilenameUtils.removeExtension(file.getName()) + ".sentences";
        File sentenceFile = new File(newname);
        FileWriter fw = null;
        try {
            fw = new FileWriter(sentenceFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (String sentence : passage.getSentences()) {
                bw.write(sentence);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        List<Entity> ranked = new ArrayList();
        ranked.addAll(entities);
        Collections.sort(ranked);
        Collections.reverse(ranked);
        return ranked;
    }
}
