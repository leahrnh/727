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

        Integer entityCode = entity.getCodeNumber();


        //extract placeholder head and relation
        Sentence questionSentence = doc.getQuestion().getSentence();
        String question = doc.getQuestion().getText();
        edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence questionParse = questionSentence.getDependencyParse(lp, gsf, semafor);
        List<Token> tokens = questionParse.getTokens();
        Integer placeholderHead = 0;
        String relationToHead = "";
        findPlaceholder:
        for (Token token : tokens) {
            String word = token.getForm();
            //System.out.println(token.getId() + "\t" + token.getForm()+ "\t" + token.getHead() + "\t" + token.getDeprel());
            if (word.equals("placeholder")) {
                placeholderHead = token.getHead();
                relationToHead = token.getDeprel();
                break findPlaceholder;
            }
        }

        //what if the placeholder is the root?
        if (placeholderHead==0) {
            //Then we get no info from looking at what its head is, so return 0.5 indiscriminately
            return 0.5;
        }

        //extract entity head and relation in different sentences
        Token head = tokens.get(placeholderHead-1);
        String headForm = head.getForm();
        //if the head is "entity," that also gives us no useful information
        if (headForm.equals("entity")) {
            return 0.5;
        }

        // calculate score as (number of sentences in which entity has same head as placeholder) / (number of sentences including entity)
        int sentencesWithEntity = 0;
        int entityPlaceholderHeadMatches = 0;
        //check each sentence for whether it contains the target entity
        for (Sentence sentence : doc.getPassage().getSentences()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse = sentence.getDependencyParse(lp, gsf, semafor);
            List<Integer> sentenceEntities = sentence.getEntityNumbers();

            //if the sentence does contain the entity, find its head, and see if it matches the head of placeholder
            if (sentenceEntities.contains(entityCode)) {
                sentencesWithEntity++;

                //identify entity in sentence matching target
                int entityIndex = sentenceEntities.indexOf(entityCode);
                tokens = dependencyParse.getTokens();
                int numEntitiesSeen = 0;
                Integer entityHead = 0;
                String entityRelationToHead = "";

                findTargetEntity:
                for (Token token : tokens) {
                    if (token.getForm().equals("entity")) {
                        if (numEntitiesSeen == entityIndex) {
                            //found our entity!
                            entityHead = token.getHead();
                            entityRelationToHead = token.getDeprel();
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
                    if (entityHeadForm.equals(headForm)) {
                        entityPlaceholderHeadMatches++;
                        //System.out.println("Found match between placeholder and entity with head " + entityHeadForm);
                    }
                }

            }
        }

        return ((double) entityPlaceholderHeadMatches) / sentencesWithEntity;
    }

    public void initializeScorer(Document document){return;}
}
