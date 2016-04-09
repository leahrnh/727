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

        @Override
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
        }*/

    }


    public double getScore(Entity entity, Document doc) {
        String current_entity = entity.getCode().toLowerCase();

        for (int i = 0; i < all_score.size(); i++){
            if (all_score.get(i).sentence.contains(current_entity)) {
                System.out.println();
                return all_score.get(i).score;
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
                all_score.add(new ScorePair<Double, String>(Double.parseDouble(line), candidateSentences.get(iterate).getText()));
                iterate++;
            }

            Collections.sort(all_score, Collections.reverseOrder());
            //System.out.println(all_scores.size() + " real");
            // Always close files.
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
        //System.out.println(new_name);
        File sentenceFile = new File(new_name);
        FileWriter fw = null;
        try {
            fw = new FileWriter(sentenceFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (Sentence sentence : candidate_sentences) {
                bw.write(sentence.getText() + "\t" + question);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
