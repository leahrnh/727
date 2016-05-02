import edu.cmu.cs.lti.ark.fn.data.prep.formats.*;

import java.util.List;

/**
 * Score based word immediately before and immediately after @placeholder
 */
public class BigramScorer extends Scorer {

    private String prevWord;
    private String nextWord;

    @Override
    public double getScore(Entity entity, Document doc) {
        String entityCode = Sentence.convertCode(entity.getCode());
        double score=0.0;
        List<Sentence> sentences = doc.getPassage().getSentences();
        for (Sentence sentence : sentences) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence parseSentence = sentence.getDependencyParse();
            if (parseSentence != null) {
                List<Token> tokens = parseSentence.getTokens();
                String entityPrevWord = "";
                String entityNextWord = "";
                findEntity:
                for (int i = 0; i < tokens.size(); i++) {
                    if (tokens.get(i).getForm().equals(entityCode)) {
                        if (i > 0) {
                            entityPrevWord = tokens.get(i - 1).getForm().toLowerCase();
                        } else {
                            entityPrevWord = "";
                        }
                        if (i < tokens.size() - 1) {
                            entityNextWord = tokens.get(i + 1).getForm().toLowerCase();
                        } else {
                            entityNextWord = "";
                        }
                        break findEntity;
                    }
                }

                if (entityPrevWord.equals(this.prevWord)) {
                    //score += entityPrevWord.length();
                    score += 1;
                    //System.out.println("found match with prev word " + entityPrevWord);
                }
                if (entityNextWord.equals(this.nextWord)) {
                    //score += entityNextWord.length();
                    score += 1;
                    //System.out.println("found match with next word " + entityNextWord);
                }
            }

        }

        //This doesn't need to be normalized because the training will take care of that be setting weights
        return score;
    }

    public void initializeScorer(Document document) {
        this.prevWord = "NONE";
        this.nextWord = "NONE";
        Sentence questionSentence = document.getQuestion().getSentence();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence parseSentence = questionSentence.getDependencyParse();
        List<Token> tokens = parseSentence.getTokens();
        findPlaceholder:
        for (int i=0;i<tokens.size();i++) {
            if (tokens.get(i).getForm().equals("placeholder")) {
                if (i>0) {
                    this.prevWord = tokens.get(i-1).getForm().toLowerCase();
                }
                if (i<tokens.size()-1) {
                    this.nextWord = tokens.get(i+1).getForm().toLowerCase();
                }
                break findPlaceholder;
            }
        }
    }
}
