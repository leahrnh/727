import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.cmu.cs.lti.ark.util.ds.Scored;
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
        System.out.println("******DOC******");
        Sentence questionSentence = doc.getQuestion().getSentence();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse(lp, gsf, semafor);
        SemaforParseResult questionSemafor = questionSentence.getSemaforParse(lp, gsf, semafor);

        for (SemaforParseResult.Frame frame : questionSemafor.frames) {
            SemaforParseResult.Frame.NamedSpanSet target = frame.target;
            String frameName = target.name;
            System.out.print("Frame: " + frameName);
            for (SemaforParseResult.Frame.Span span : target.spans) {
                System.out.println( "...from text " + span.text);
                if (span.text.equals("placeholder")) {
                    System.out.println("placeholder in frame " + frameName + " with role " + frameName);
                }
            }
            for (SemaforParseResult.Frame.ScoredRoleAssignment annotationSet : frame.annotationSets) {
                for (SemaforParseResult.Frame.NamedSpanSet spanSet : annotationSet.frameElements) {
                    String spanName = spanSet.name;
                    System.out.print("Span: " + spanName);
                    for (SemaforParseResult.Frame.Span span : spanSet.spans) {
                        System.out.println( "...from text " + span.text);
                        if (span.text.equals("placeholder")) {
                            System.out.println("placeholder in frame " + frameName + " with role " + spanName);
                        }
                    }
                }
            }

        }


        /*for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse(lp, gsf, semafor);
            SemaforParseResult semaforParse = sentence.getSemaforParse(lp, gsf, semafor);
        }
        */

        //Currently not returning a meaningful score. That would be nice, too.
        return 0;
    }

    public void initializeScorer(Document document){return;}
}
