import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.*;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private LexicalizedParser lp;
    private GrammaticalStructureFactory gsf;

    /**
     * Constructor that reads components directly from the file
     */
    public Document(String filepath, LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor, boolean useSemafor) {
        this.id = filepath;
        this.lp = lp;
        this.gsf = gsf;
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
        File f = new File(filepath);
        String parseFileName = f.getParent() + "/" + FilenameUtils.removeExtension(f.getName()) + ".parse";
        String questionParseFileName = f.getParent() + "/" + FilenameUtils.removeExtension(f.getName()) + ".questionparse";
        File parseFile = new File(parseFileName);
        File questionParseFile = new File(questionParseFileName);
        //if the parse file does not already exist, then create it
        if (!parseFile.exists()) {
            createParseFile(parseFileName, passage, useSemafor);
        }

        if (!questionParseFile.exists()) {
            createQuestionParseFile(questionParseFile, question);
        }

        readParseFile(passage, parseFileName, semafor, useSemafor);
        readQuestionParseFile(question, questionParseFileName, semafor);

        this.answer = new Entity(lines.get(6) + ":UNKNOWN"); //this is kind of a hack because the answer code doesn't come with a word
        Set<Entity> es = new HashSet<Entity>();
        for (int i=8;i<lines.size();i++) {
            es.add(new Entity(lines.get(i)));
        }
        this.entities = es;

    }

    private void readQuestionParseFile(Question question, String questionParseFileName, Semafor semafor) {
        SentenceCodec.SentenceIterator sentenceIterator = null;
        try {
            sentenceIterator = ConllCodec.readInput(new FileReader(questionParseFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Sentence questionSentence = question.getSentence();
        if (sentenceIterator.hasNext()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentenceIterator.next();
            questionSentence.setDependencyParse(dependencyParse);
            try {
                questionSentence.setSemaforParse(semafor.parseSentence(dependencyParse));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void createQuestionParseFile(File questionParseFile, Question question) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(questionParseFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        Sentence sentence = question.getSentence();
        String parseText = sentence.getText();
        Pattern pattern = Pattern.compile("@entity([0-9]+)");
        Matcher m = pattern.matcher(parseText);
        List<Integer> entityNumbers = new ArrayList<Integer>();
        while (m.find()) {
            Integer entityNumber = Integer.parseInt(m.group(1));
            entityNumbers.add(entityNumber);
            String entityCode = m.group();
            parseText = parseText.replaceAll(entityCode, Sentence.convertCode(entityCode));
        }
        //sentence.setEntityNumbers(entityNumbers);
        //parseText = parseText.replaceAll("@placeholder", "placeholder");

        //Dependency parse
        String conllDependencyParse;

        //Have to redirect terminal output to variable http://stackoverflow.com/questions/8708342/redirect-console-output-to-string-in-java
        // Create a stream to hold the output
        ByteArrayOutputStream tmpOutputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(tmpOutputStream);
        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);

        for (List<HasWord> hasWords : new DocumentPreprocessor(new StringReader(parseText))) {
            Tree parse = lp.apply(hasWords);
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            GrammaticalStructure.printDependencies(gs, gs.typedDependencies(), parse, true, false);
        }
        // Put output stream back the way it was before
        System.out.flush();
        System.setOut(old);

        conllDependencyParse = tmpOutputStream.toString();

        try {
            bufferedWriter.write(conllDependencyParse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readParseFile(Passage passage, String parseFileName, Semafor semafor, boolean useSemafor) {
        SentenceCodec.SentenceIterator sentenceIterator = null;
        try {
            sentenceIterator = ConllCodec.readInput(new FileReader(parseFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int i = 0;
        List<Sentence> sentences = passage.getSentences();
        while (sentenceIterator.hasNext()) {
            Sentence targetSentence = sentences.get(i);
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentenceIterator.next();
            targetSentence.setDependencyParse(dependencyParse);
            if (useSemafor) {
                try {
                    targetSentence.setSemaforParse(semafor.parseSentence(dependencyParse));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            i++;

        }
    }

    private void createParseFile(String parseFileName, Passage passage, boolean useSemafor) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(parseFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        List<Sentence> sentences = passage.getSentences();
        for (Sentence sentence : sentences) {
            String parseText = sentence.getText();
            Pattern pattern = Pattern.compile("@entity([0-9]+)");
            Matcher m = pattern.matcher(parseText);
            while (m.find()) {
                String entityCode = m.group();
                parseText = parseText.replaceAll(entityCode, Sentence.convertCode(entityCode));
            }
            //parseText = parseText.replaceAll("@placeholder", "placeholder");

            //Dependency parse
            String conllDependencyParse;

            //Have to redirect terminal output to variable http://stackoverflow.com/questions/8708342/redirect-console-output-to-string-in-java
            // Create a stream to hold the output
            ByteArrayOutputStream tmpOutputStream = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(tmpOutputStream);
            // IMPORTANT: Save the old System.out!
            PrintStream old = System.out;
            // Tell Java to use your special stream
            System.setOut(ps);

            for (List<HasWord> hasWords : new DocumentPreprocessor(new StringReader(parseText))) {
                Tree parse = lp.apply(hasWords);

                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                GrammaticalStructure.printDependencies(gs, gs.typedDependencies(), parse, true, false);
            }
            // Put output stream back the way it was before
            System.out.flush();
            System.setOut(old);

            conllDependencyParse = tmpOutputStream.toString();

            if (useSemafor) {
                try {
                    bufferedWriter.write(conllDependencyParse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            bufferedWriter.close();
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
        List<Entity> ranked = new ArrayList<Entity>();
        ranked.addAll(entities);
        Collections.sort(ranked);
        Collections.reverse(ranked);
        return ranked;
    }
}
