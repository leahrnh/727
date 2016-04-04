import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by shrimai on 4/1/16.
 */
public class SentenceToVector extends Scorer{

    public SentenceToVector(List<Document> documentList) {
        int n = 1;
        for (Document doc : documentList) {
            String questionSentence = doc.getQuestion().getText();
            List<Sentence> candidateSentences = doc.getPassage().getSentences();
            try {
                String filename = "Document" + Integer.toString(n) + ".txt";
                PrintWriter writer = new PrintWriter("/Users/shrimai/skipVector/skip-thoughts/data/semanticData/" + filename, "UTF-8");
                System.out.println("Writing Files..");
                for (Sentence candSent : candidateSentences){
                    writer.println(candSent.getText() + "\t" + questionSentence);
                }
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            n++;
        }
        String command = "python /Users/shrimai/skipVector/skip-thoughts/semantic_score.py";
        try {
            System.out.println("Running the python command");
            Process process = Runtime.getRuntime().exec(command);
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNext()) {
                System.out.println(scanner.nextLine());
            }
            int exitvalue = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public double getScore(Entity entity, Document doc) {
        return 0;
    }
}
