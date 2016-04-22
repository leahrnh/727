import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Demo class to show how to incorporate parses into a scorer.
 * Note that sentences will only be parsed once.
 */
public class SemaforScorer extends Scorer {

    private LexicalizedParser lp; //Stanford parser
    private GrammaticalStructureFactory gsf; //Stanford Grammatical Structure Factory
    private Semafor semafor;

    public SemaforScorer(LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor) {
        this.lp = lp;
        this.gsf = gsf;
        this.semafor = semafor;
    }

    public double getScore(Entity entity, Document doc) {

        for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse(lp, gsf, semafor);
            SemaforParseResult semaforParse = sentence.getSemaforParse(lp, gsf, semafor);
        }

        //Currently not returning a meaningful score. That would be nice, too.
        return 0;
    }

    public void initializeScorer(Document document){return;}
}
