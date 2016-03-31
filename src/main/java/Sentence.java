import edu.cmu.cs.lti.ark.fn.Semafor;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec;
import edu.cmu.cs.lti.ark.fn.data.prep.formats.Token;
import edu.cmu.cs.lti.ark.fn.parsing.SemaforParseResult;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;

import java.io.*;
import java.util.List;

import static edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec.ConllCodec;

public class Sentence {
    private String text;
    private List<String> entityNames;
    private edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse;
    private SemaforParseResult semaforParse;

    public Sentence(String text) {
        this.text = text;
        //TODO extract entity names from text
    }

    public String getText() {
        return text;
    }

    /**
     * Get the Turbo-parsed version of the sentence. This includes tokens, parts of speech, dependency tags
     */
    public edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence getDependencyParse(LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor) {
        if (dependencyParse ==null) {
            parseSentence(lp, gsf, semafor);
        }
        return dependencyParse;
    }

    public SemaforParseResult getSemaforParse(LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor) {
        if (semaforParse==null) {
            parseSentence(lp, gsf, semafor);
        }
        return semaforParse;
    }

    private void parseSentence(LexicalizedParser lp, GrammaticalStructureFactory gsf, Semafor semafor) {
        //Dependency parse
        String conllDependencyParse;

        //Have to redirect terminal output to variable http://stackoverflow.com/questions/8708342/redirect-console-output-to-string-in-java
        // Create a stream to hold the output
        ByteArrayOutputStream tmpOutputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(tmpOutputStream);
        // IMPORTANT: Save the old System.out!
        PrintStream old = System.out;
        // Tell Java to use your special stream
        System.setOut(ps);

        for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(text))) {
            Tree parse = lp.apply(sentence);

            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            GrammaticalStructure.printDependencies(gs, gs.typedDependencies(), parse, true, false);
        }

        conllDependencyParse = tmpOutputStream.toString();

        final SentenceCodec.SentenceIterator sentenceIterator = ConllCodec.readInput(new StringReader(conllDependencyParse));
        if (sentenceIterator.hasNext()) {
            dependencyParse = sentenceIterator.next();
            try {
                semaforParse = semafor.parseSentence(dependencyParse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Warning: Sentence has no parse. \"" + text + "\"");
        }

        //At this point we should have parsed all the text
        if (sentenceIterator.hasNext()) {
            System.err.println("Warning: Sentence has multiple parsed sentences. \"" + text + "\"");
            //TODO figure out the deal with one sentence not being broken up correctly
            System.err.println("Part 2: ");
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dp2 = sentenceIterator.next();
            for (Token token : dp2.getTokens()) {
                System.out.print(" " + token.toString());
            }
        }

        // Put output stream back the way it was before
        System.out.flush();
        System.setOut(old);

    }
}
