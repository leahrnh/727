import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.*;

/**
 * Created by shrimai on 4/1/16.
 */
public class SentenceToVector extends Scorer{
    public static Integer document_index = 0;
    private static ArrayList<ScorePair<Double, String>> all_score;

    class ScorePair<D, S> implements Comparable<ScorePair<D, S>> {
        Double score;
        String sentence;

        public ScorePair(Double score, String name) {
            this.score = score;
            this.sentence = name;
        }

        public int compareTo(ScorePair<D, S> o) {
            return score < o.score ? -1 : score > o.score ? 1 : 0;
        }
    }

    public SentenceToVector(List<Document> documentList) {
        for (Document doc : documentList) {
            String file_id = doc.getId();
            String questionSentence = doc.getQuestion().getText();
            List<Sentence> candidateSentences = doc.getPassage().getSentences();
            writeSentences(file_id, candidateSentences, questionSentence);
        }
        String command = "python /Users/shrimai/skipVector/skip-thoughts/semantic_score.py";
        /*try {
            // PRINT : Running the Python Module
            System.out.println("Running the python command");
            Process process = Runtime.getRuntime().exec(command);
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNext()) {
                // PRINT : Printing python output
                System.out.println(scanner.nextLine());
            }
            int exitvalue = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }


    public double getScore(Entity entity, Document doc) {
        String current_entity = entity.getCode().toLowerCase();

        for (int i = 0; i < all_score.size(); i++){
            if (all_score.get(i).sentence.contains(current_entity)) {
                System.out.println();
                return all_score.get(i).score/5.0;
            }
        }
        return 0;
    }
    
    public void initializeScorer(Document document){
        String filepath = document.getId();
        File file = new File(filepath);
        String path_to_scores = new File(file.getParent()).getParent() + "/skipVector/skip-thoughts/data/semanticData/" + FilenameUtils.removeExtension(file.getName()) + ".score";
        String line;
        all_score = new ArrayList<ScorePair<Double, String>>();
        List<Sentence> candidateSentences = document.getPassage().getSentences();
        Integer iterate = 0;
        try {
            FileReader fileReader = new FileReader(path_to_scores);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
                if (candidateSentences.get(iterate).getText().isEmpty() || candidateSentences.get(iterate).getText().equals(" ")){
                    //System.out.println("Here empty string");
                    continue;
                }
                all_score.add(new ScorePair<Double, String>(Double.parseDouble(line), candidateSentences.get(iterate).getText()));
                iterate++;
            }

            Collections.sort(all_score, Collections.reverseOrder());
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void writeSentences(String filepath, List<Sentence> candidate_sentences, String question) {
        File file = new File(filepath);
        String new_name = new File(file.getParent()).getParent() + "/skipVector/skip-thoughts/data/semanticData/" + FilenameUtils.removeExtension(file.getName()) + ".test";
        File sentenceFile = new File(new_name);

        if(sentenceFile.exists() && !sentenceFile.isDirectory()) {
            return;
        }
        FileWriter fw = null;
        try {
            System.out.println("Writing File " + new_name);
            fw = new FileWriter(sentenceFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Sentence sentence : candidate_sentences) {
                if (sentence.getText().isEmpty() || sentence.getText().equals(" ")){
                    //System.out.println("Here empty string");
                    continue;
                }
                else {
                    bw.write(sentence.getText() + "\t" + question);
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
