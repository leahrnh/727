import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;

import java.util.List;

public class Sentence {
    private String text;
    private List<String> entitiyNames;
    private edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence parsedSentence;
    private SemaforParseResult semaforParse;

    public Sentence(String text) {
        this.text = text;
        //TODO extract entity names from text
    }

    public void setParsedSentence(edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence parsedSentence) {
        this.parsedSentence = parsedSentence;
    }

    public void setSemaforParse(SemaforParseResult semaforParse) {
        this.semaforParse = semaforParse;
    }

    public String getText() {
        return text;
    }

    /**
     * Get the Turbo-parsed version of the sentence. This includes tokens, parts of speech, dependency tags
     */
    public edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence getParsedSentence() {
        return parsedSentence;
    }

    public SemaforParseResult getSemaforParse() {
        return semaforParse;
    }


}
