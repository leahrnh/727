import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.Token;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Demo class to show how to incorporate parses into a scorer.
 * Note that sentences will only be parsed once.
 */
public class DependecyScorer extends Scorer {

    private LexicalizedParser lp; //Stanford parser
    private GrammaticalStructureFactory gsf; //Stanford Grammatical Structure Factory
    private Semafor semafor;

    /**
     * Initialize parsing models
     */
    private void init() {
        //set up Parser
        lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
        if (tlp.supportsGrammaticalStructures()) {
            gsf = tlp.grammaticalStructureFactory();
        }

        //set up Semafor
        File modelsLocation = new File("src/main/resources/semafor_models");
        String modelsDir = modelsLocation.getAbsolutePath();
        try {
            semafor = Semafor.getSemaforInstance(modelsDir);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public double getScore(Entity entity, Document doc) {
        if (lp==null || gsf==null) {
            init();
        }

        Sentence questionSentence = doc.getQuestion().getSentence();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse(lp, gsf, semafor);


        for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse(lp, gsf, semafor);
            List<Token> tokens = dependencyParse.getTokens();
            for (Token token : tokens) {
                String word = token.toString();
                String pos = token.getCpostag();
                Integer head = token.getHead();
            }
        }

        //Currently not returning a meaningful score. That would be nice, too.
        return 0;
    }

    public void initializeScorer(Document document){return;}
}
