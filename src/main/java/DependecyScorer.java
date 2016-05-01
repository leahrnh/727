import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.Token;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;

import java.util.List;

/**
 * Demo class to show how to incorporate parses into a scorer.
 * Note that sentences will only be parsed once.
 */
public class DependecyScorer extends Scorer {

    private String placeholderHead;

    public DependecyScorer() {
    }

    public double getScore(Entity entity, Document doc) {
        Integer entityCodeNumber = entity.getCodeNumber();
        String entityCode = entity.getCode();
        String entityParseCode = Sentence.convertCode(entityCode);

        //if placeholderHead is empty, we will never find a match, so we can just return 0 for everything
        if (placeholderHead.equals("")) {
            return 0.0;
        }

        // calculate score as (number of sentences in which entity has same head as placeholder) / (number of sentences including entity)
        //check each sentence for whether it contains the target entity
        for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse();

            //identify entity in sentence matching target
            List<Token> tokens = dependencyParse.getTokens();
            Integer entityHead = 0;

            findTargetEntity:
            for (Token token : tokens) {
                if (token.getForm().equals(entityParseCode)) {
                    //found our entity!
                    entityHead = token.getHead();
                    break findTargetEntity;
                }
            }

            //compare entity head to placeholder head, and give a point if they match
            if (entityHead != 0) {
                Token entityHeadToken = tokens.get(entityHead - 1);
                String entityHeadForm = entityHeadToken.getForm();
                //System.out.println("Entity " + entityCode + " has head " + entityHeadForm);
                //return semanticComparison(entityHeadForm, this.placeholderHead);
                if (entityHeadForm.equals(this.placeholderHead)) {
                    //TODO compare heads semantically
                    return 1.0;
                }
            }

        }

        //if we never found a match...
        return 0.0;
    }

    public void initializeScorer(Document document){
        this.placeholderHead = "";

        //extract placeholder head and relation
        Sentence questionSentence = document.getQuestion().getSentence();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse();
        List<Token> tokens = questionParse.getTokens();
        Integer placeholderHead = 0;
        findPlaceholder:
        for (Token token : tokens) {
            String word = token.getForm();
            //System.out.println(token.getId() + "\t" + token.getForm()+ "\t" + token.getHead() + "\t" + token.getDeprel());
            if (word.equals("placeholder")) {
                placeholderHead = token.getHead();
                break findPlaceholder;
            }
        }

        //what if the placeholder is the root?
        if (placeholderHead==0) {
            //Then we get no info from looking at what its head is, so return 0 indiscriminately
            this.placeholderHead = "";
            return;
        }

        //extract entity head and relation in different sentences
        Token head = tokens.get(placeholderHead - 1);
        String placeholderHeadForm = head.getForm();
        //if the head is "entity," that also gives us no useful information
        if (placeholderHeadForm.matches("entity[A-Z]+")) {
            this.placeholderHead = "";
            return;
        }

        //remember placeholder for the document
        this.placeholderHead = placeholderHeadForm;
        //questionSentence.printParse();
        //System.out.println("Placeholder has head " + placeholderHeadForm);
    }
}
