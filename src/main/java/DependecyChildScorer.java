import edu.cmu.cs.lti.ark.fn.data.prep.formats.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Instead of looking at the head of the placeholder/entities, look at their children
 */
public class DependecyChildScorer extends Scorer {

    private List<String> placeholderChildren;

    public DependecyChildScorer() {
    }

    public double getScore(Entity entity, Document doc) {
        String entityCode = entity.getCode();
        String entityParseCode = Sentence.convertCode(entityCode);

        double score = 0.0;

        //if placeholderHead is empty, we will never find a match, so we can just return 0 for everything
        if (placeholderChildren.isEmpty()) {
            return 0.0;
        }

        //looks for occurences of the entity sharing a child with the placeholder
        for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse();

            //identify entity in sentence matching target
            List<Token> tokens = dependencyParse.getTokens();
            Integer entityIndex = -1;

            findTargetEntity:
            for (Token token : tokens) {
                if (token.getForm().equals(entityParseCode)) {
                    //found our entity!
                    entityIndex = token.getId();
                    break findTargetEntity;
                }
            }

            if (entityIndex >= 0) {
                //look for children that match those of the placeholder
                for (Token token : tokens) {
                    if (token.getHead().equals(entityIndex) && placeholderChildren.contains(token.getForm().toLowerCase())) {
                        score += 1;
                    }
                }
            }
        }
        return score;
    }

    public void initializeScorer(Document document) {
        this.placeholderChildren = new ArrayList<String>();

        //extract placeholder position index
        Sentence questionSentence = document.getQuestion().getSentence();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse();
        List<Token> tokens = questionParse.getTokens();
        Integer placeholderIndex = 0;
        findPlaceholder:
        for (Token token : tokens) {
            String word = token.getForm();
            //System.out.println(token.getId() + "\t" + token.getForm()+ "\t" + token.getHead() + "\t" + token.getDeprel());
            if (word.equals("placeholder")) {
                placeholderIndex = token.getId();
                break findPlaceholder;
            }
        }

        //extract placeholder children
        for (Token token : tokens) {
            if (token.getHead() == placeholderIndex && !token.getDeprel().equals("punct") && !token.getDeprel().equals("erased")) {
                placeholderChildren.add(token.getForm().toLowerCase());
            }
        }
    }
}
