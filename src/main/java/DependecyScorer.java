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
    private String placeholderHead;

    public DependecyScorer(LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor) {
        this.lp = lp;
        this.gsf = gsf;
        this.semafor = semafor;
    }

    public double getScore(Entity entity, Document doc) {
        Integer entityCode = entity.getCodeNumber();

        // calculate score as (number of sentences in which entity has same head as placeholder) / (number of sentences including entity)
        //check each sentence for whether it contains the target entity
        for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse(lp, gsf, semafor);
            List<Integer> sentenceEntities = sentence.getEntityNumbers();

            //if the sentence does contain the entity, find its head, and see if it matches the head of placeholder
            if (sentenceEntities.contains(entityCode)) {

                //identify entity in sentence matching target
                int entityIndex = sentenceEntities.indexOf(entityCode);
                List<Token> tokens = dependencyParse.getTokens();
                int numEntitiesSeen = 0;
                Integer entityHead = 0;

                findTargetEntity:
                for (Token token : tokens) {
                    if (token.getForm().equals("entity")) {
                        if (numEntitiesSeen == entityIndex) {
                            //found our entity!
                            entityHead = token.getHead();
                            break findTargetEntity;
                        }
                        else {
                            numEntitiesSeen += 1;
                        }
                    }
                }

                //compare entity head to placeholder head, and give a point if they match
                if (entityHead!=0) {
                    Token entityHeadToken = tokens.get(entityHead - 1);
                    String entityHeadForm = entityHeadToken.getForm();
                    if (entityHeadForm.equals(this.placeholderHead)) {
                        //TODO compare heads semantically
                        return 1.0;
                    }
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
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse(lp, gsf, semafor);
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
        Token head = tokens.get(placeholderHead-1);
        String placeholderHeadForm = head.getForm();
        //if the head is "entity," that also gives us no useful information
        if (placeholderHeadForm.equals("entity")) {
            this.placeholderHead = "";
            return;
        }

        //remember placeholder for the document
        this.placeholderHead = placeholderHeadForm;
    }
}
