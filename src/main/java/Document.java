import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.*;

import static edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec.ConllCodec;

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
    public Document(String filepath, Semafor semafor) {
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

        //comment line below IN when creating .sentence files
        //writeSentences(filepath, passage, question);

        //comment line below OUT when creating .sentence files
        parseSentences(semafor, filepath);

    }

    /**
     * Create a new file, filename.sentences, that just contains lines made of individual sentences (1 sentence per line)
     * Used once per data set, to create input to turbo parser
     */
    private void writeSentences(String filepath, Passage passage, Question question) {
        String[] sentenceTexts =  passage.getText().split("\\.");
        List<Sentence> sentenceList = new ArrayList<Sentence>();
        File file = new File(filepath);
        String newname = file.getParent() + "/" + FilenameUtils.removeExtension(file.getName()) + ".sentences";
        File sentenceFile = new File(newname);
        FileWriter fw;
        try {
            fw = new FileWriter(sentenceFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (String sentence : sentenceTexts) {
                Sentence s = new Sentence(sentence);
                sentenceList.add(s);
                bw.write(sentence);
                bw.newLine();
            }
            passage.setSentences(sentenceList);
            bw.write(question.getText());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use Semafor to read the turbo-parsed .parse file.
     * Create Sentence objects associated with Passage and Question
     * @param semafor Semafor object
     * @param filepath absolute path to .question file
     */
    private void parseSentences(Semafor semafor, String filepath) {
        List<Sentence> sentenceList = new ArrayList<Sentence>();
        String[] sentenceTexts =  passage.getText().split("\\.");
        File questionFile = new File(filepath);
        String parseFileName = questionFile.getParent() + "/" + FilenameUtils.removeExtension(questionFile.getName()) + ".parse";
        final SentenceCodec.SentenceIterator sentenceIterator;
        try {
            sentenceIterator = ConllCodec.readInput(new FileReader(parseFileName));
            for (String sentence : sentenceTexts) {
                Sentence s = new Sentence(sentence);
                if (sentenceIterator.hasNext()) {
                    edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence turboSentence = sentenceIterator.next();
                    s.setParsedSentence(turboSentence);
                    SemaforParseResult result = semafor.parseSentence(turboSentence);
                    s.setSemaforParse(result);
                    sentenceList.add(s);
                } else {
                    System.err.println("Warning: iterator and sentenceList are different lengths");
                }
            }

            //the final sentence is the question sentence
            if (sentenceIterator.hasNext()) {
                Sentence s = new Sentence(question.getText());
                edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence turboSentence = sentenceIterator.next();
                s.setParsedSentence(turboSentence);
                SemaforParseResult result = semafor.parseSentence(turboSentence);
                s.setSemaforParse(result);
                question.setSentence(s);
            } else {
                System.err.println("No question parse for " + filepath);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        passage.setSentences(sentenceList);
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
