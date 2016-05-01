import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse sentences using Semafor and compare frames and roles
 */
public class SemaforScorer extends Scorer {

    private String placeholderFrame;
    private String placeholderRole;

    public SemaforScorer() {
        this.placeholderFrame = "";
        this.placeholderRole = "";
    }

    public double getScore(Entity entity, Document doc) {
        double score = 0.0;
        if (this.placeholderFrame.equals("")) {
            return score;
        }
        Passage passage = doc.getPassage();
        String entityCode = Sentence.convertCode(entity.getCode());
        Integer entityCodeNumber = entity.getCodeNumber();
        for (Sentence sentence : passage.getSentences()) {
            //if the sentence does contain the entity, find its head, and see if it matches the head of placeholder
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse(); //using this as a setter method even though it really isn't
            List<String> frameAndRole = findFrameAndRole(sentence, entityCode);
            String entityFrame = frameAndRole.get(0);
            String entityRole = frameAndRole.get(1);

            if (this.placeholderRole.equals(entityRole)) {
                score += 1;
                //System.out.println(doc.getId());
                //System.out.println("**Found match between placeholder and " + entityCode + " role " + entityRole);
                if (this.placeholderFrame.equals(entityFrame)) {
                    score += 1;
                    //System.out.println(doc.getId());
                    //System.out.println("****Found match between placeholder and " + entityCode + " frame " + entityFrame);
                }
            }
        }
        return score;
    }

    public void initializeScorer(Document document){
        Sentence questionSentence = document.getQuestion().getSentence();
        List<String> frameAndRole = findFrameAndRole(questionSentence, "placeholder");
        this.placeholderFrame = frameAndRole.get(0);
        this.placeholderRole = frameAndRole.get(1);


        /*if (this.placeholderFrame.equals("")) {
            System.out.println("No role for placeholder");
        } else {
            System.out.println("placeholder has role " + this.placeholderRole + " in frame " + this.placeholderFrame);
        }*/
    }

    private List<String> findFrameAndRole(Sentence sentence, String targetWord) {
        SemaforParseResult questionSemafor = sentence.getSemaforParse();
        String foundFrame = "";
        String foundRole = "";

        for (SemaforParseResult.Frame frame : questionSemafor.frames) {
            SemaforParseResult.Frame.NamedSpanSet target = frame.target;
            //System.out.println("Target name: " + target.name);
            List<SemaforParseResult.Frame.Span> spans = target.spans;
            for (SemaforParseResult.Frame.Span span : spans) {
                //System.out.println("\tText: " + span.text);
                //System.out.println("\tStart: " + span.start);
                //System.out.println("\tEnd: " + span.end);
                if (span.text.equals(targetWord)) {
                    foundFrame = target.name;
                    foundRole = target.name;
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
                        if (span.text.equals(targetWord)) {
                            foundFrame = target.name;
                            foundRole = namedSpanSet.name;
                        }
                    }
                }
            }
        }

        List<String> answer = new ArrayList<String>();
        answer.add(foundFrame);
        answer.add(foundRole);

        return answer;
    }
}
