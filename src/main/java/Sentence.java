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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.cmu.cs.lti.ark.fn.data.prep.formats.SentenceCodec.ConllCodec;

public class Sentence {
    private String text;
    private List<Integer> entityNumbers;
    private edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dependencyParse;
    private SemaforParseResult semaforParse;

    public Sentence(String text) {
        this.text = text;
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
        String parseText = text;
        Pattern pattern = Pattern.compile("@entity([0-9]+)");
        Matcher m = pattern.matcher(parseText);
        entityNumbers = new ArrayList<Integer>();
        while (m.find()) {
            Integer entityNumber = Integer.parseInt(m.group(1));
            entityNumbers.add(entityNumber);
            String entityCode = m.group();
            parseText = parseText.replaceAll(entityCode, convertCode(entityCode));
        }
        parseText = parseText.replaceAll("@placeholder", "placeholder");

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

        for (List<HasWord> sentence : new DocumentPreprocessor(new StringReader(parseText))) {
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
            System.err.println(parseText);
            System.err.println("Warning: Sentence has no parse. \"" + text + "\"");
        }

        //At this point we should have parsed all the text
        if (sentenceIterator.hasNext()) {
            edu.cmu.cs.lti.ark.fn.data.prep.formats.Sentence dp2 = sentenceIterator.next();
            System.err.println("Warning: Sentence has multiple parsed sentences. \"" + text + "\"");
            //If this error comes up, it probably means that the semafor tokenization is different from the tokenization in Passage.java
            System.err.println("Part 2: ");
            for (Token token : dp2.getTokens()) {
                System.err.println(". " + token.toString());
            }
        }

        // Put output stream back the way it was before
        System.out.flush();
        System.setOut(old);

    }

    public List<Integer> getEntityNumbers() {
        return entityNumbers;
    }

    public static String convertCode(String code) {
        if (code.matches("@entity[0-9]+")) {
            code = code.replaceAll("entity", "Entity");
            code = code.replaceAll("@", "");
            return code;
        } else {
            System.err.println("Can't convert code " + code);
            return "";
        }
    }

    public void printParse() {
        for (Token token : dependencyParse.getTokens()) {
            System.out.println(token.getId()+"\t"+token.getForm()+"\t"+token.getHead());
        }
    }
}
