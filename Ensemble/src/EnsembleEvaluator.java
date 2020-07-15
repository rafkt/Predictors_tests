import java.util.Arrays;
import java.util.List;

public class EnsembleEvaluator {

    public static enum Datasets{BMS, KOSARAK, FIFA, MSNBC, SIGN, BIBLE_CHAR, BIBLE_WORD};

    private static final int  FOLDS = 14;

    public static void main(String[] args) {

        Arrays.stream(Datasets.values()).forEach(dataset -> {
            double accuracy = 0.0;
            for(int fold = 0; fold < FOLDS; fold++){
                List<Sequence> consequents = EnsemblePredictor.ReadFromFile("../../outputs-ensemble/" + dataset.toString() + ".fold." + fold +
                        ".consequent.mapped.txt");

                EnsemblePredictor ensemblePredictor = new EnsemblePredictor(dataset.toString(), fold);

                ensemblePredictor.HasValidInit(consequents.size());

                int counter = 0;

                for (int i = 0; i < consequents.size(); i++){
                    for(Integer item : consequents.get(i).getItems()){
                        try {
                            Sequence predictions = ensemblePredictor.Predict(i, EnsemblePredictor.Mode.Recommendations);
                            if (IsGoodPrediction(predictions, item)){
                                counter++;
                                ensemblePredictor.ProvideFeedback();
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                accuracy = accuracy + ((float)counter / consequents.size());
                //System.out.println(((float)counter / consequents.size()));
            }
            System.out.println("" + dataset.toString() + ": " + ((float)accuracy / FOLDS));
        });
    }

    public static boolean IsGoodPrediction(Sequence predictions, Integer item){
        for (Integer prediction : predictions.getItems()){
            if (prediction.compareTo(item) == 0) {
                return true;
            }
        }
        return false;
    }
}
