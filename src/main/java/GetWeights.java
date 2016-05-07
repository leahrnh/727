import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;

/**
 * Created by leah on 5/6/16.
 */
public class GetWeights {

    public static void main(String[] args) {
        try {
            Classifier classifier = ScoreCalculator.loadModel();
            double[][] weights = ((Logistic) classifier).coefficients();
            for (int i=0;i<weights.length;i++) {
                for (int j=0;j<weights[i].length;j++) {
                    System.out.println("["+i+"]["+j+"]"+weights[i][j]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
