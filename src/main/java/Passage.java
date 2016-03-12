import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec.ConllCodec;

/**
 * Stores the text component of the document
 * TODO consider making this class more complex (ex. splitting passages into sentences, associating different entities with different sentences, etc)
 */
public class Passage {
    private String text;
    private List<Sentence> sentences;

    public static List<Sentence> makeSentences(String fullPassage, String filepath, Semafor semafor) {
        List<Sentence> sentenceList = new ArrayList<Sentence>();
        String[] sentenceTexts =  fullPassage.split("\\.");

        //comment section below OUT when creating .sentence files
        File questionFile = new File(filepath);
        String parseFileName = questionFile.getParent() + "/" + FilenameUtils.removeExtension(questionFile.getName()) + ".parse";
        File parseFile = new File(parseFileName);
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
                } else {
                    System.err.println("Warning: iterator and sentenceList are different lengths");
                }
                sentenceList.add(s);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //comment section below IN if creating new .sentence files
        /*for (String sentence : sentenceTexts) {
            Sentence s = new Sentence(sentence);
            sentenceList.add(s);
        }*/

        return sentenceList;
    }

    public Passage(String fullPassage, String filepath, Semafor semafor) {
        this.text = fullPassage;
        this.sentences = makeSentences(fullPassage, filepath, semafor);
    }

    public List<Sentence> getSentences() {
        return sentences;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Passage{" +
                "text='" + text + '\'' +
                '}';
    }

}
