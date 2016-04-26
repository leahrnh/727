import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;

import java.util.List;

/**
 * Demo class to show how to incorporate parses into a scorer.
 * Note that sentences will only be parsed once.
 */
public class SemaforScorer extends Scorer {

    private LexicalizedParser lp; //Stanford parser
    private GrammaticalStructureFactory gsf; //Stanford Grammatical Structure Factory
    private Semafor semafor;
    private String placeholderFrame;
    private String placeholderRole;

    public SemaforScorer(LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor) {
        this.lp = lp;
        this.gsf = gsf;
        this.semafor = semafor;
    }

    public double getScore(Entity entity, Document doc) {
        //Currently not returning a meaningful score. That would be nice, too.
        return 0;
    }

    /*private boolean containsWord(String text, String target) {
        String[] words = text.split("\\s");
        for (String word : words) {
            if (word.equals(target)) {
                return true;
            }
        }
        return false;
    }*/

    public void initializeScorer(Document document){
        this.placeholderFrame = "";
        this.placeholderRole = "";
        Sentence questionSentence = document.getQuestion().getSentence();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse(lp, gsf, semafor);
        SemaforParseResult questionSemafor = questionSentence.getSemaforParse(lp, gsf, semafor);

        for (SemaforParseResult.Frame frame : questionSemafor.frames) {
            SemaforParseResult.Frame.NamedSpanSet target = frame.target;
            //System.out.println("Target name: " + target.name);
            List<SemaforParseResult.Frame.Span> spans = target.spans;
            for (SemaforParseResult.Frame.Span span : spans) {
                //System.out.println("\tText: " + span.text);
                //System.out.println("\tStart: " + span.start);
                //System.out.println("\tEnd: " + span.end);
                if (span.text.equals("placeholder")) {
                    this.placeholderFrame = target.name;
                    this.placeholderRole = target.name;
                }
            }
            List<SemaforParseResult.Frame.ScoredRoleAssignment> scoredRoleAssignments = frame.annotationSets;
            for (SemaforParseResult.Frame.ScoredRoleAssignment scoredRoleAssignment : scoredRoleAssignments) {
                List<SemaforParseResult.Frame.NamedSpanSet> namedSpanSets = scoredRoleAssignment.frameElements;
                for (SemaforParseResult.Frame.NamedSpanSet namedSpanSet : namedSpanSets) {
                    //System.out.println("\tNamed span set: " + namedSpanSet.name);
                    List<SemaforParseResult.Frame.Span> subSpans = namedSpanSet.spans;
                    for (SemaforParseResult.Frame.Span span : subSpans) {
                        //System.out.println("\t\tText: " + span.text);
                        //System.out.println("\t\tStart: " + span.start);
                        //System.out.println("\t\tEnd: " + span.end);
                        if (span.text.equals("placeholder")) {
                            this.placeholderFrame = target.name;
                            this.placeholderRole = namedSpanSet.name;
                        }
                    }
                }
            }
        }
        /*if (this.placeholderFrame.equals("")) {
            System.out.println("No role for placeholder");
        } else {
            System.out.println("Placeholder has role " + this.placeholderRole + " in frame " + this.placeholderFrame);
        }*/
    }
}
