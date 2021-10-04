import java.util.*;

public class EnsembleConfiguration {

    //PredictorList, an expectedCommonScore and a currentRank

    private List<Predictor> PredictorList;
    private int ExpectedCommonScore;
    private int CurrentRank;

    public EnsembleConfiguration(List<Predictor> predictorList, int expectedCommonScore, int currentRank){
        CurrentRank = currentRank;
        ExpectedCommonScore = expectedCommonScore;
        PredictorList = new ArrayList<Predictor>(predictorList);
    }

    public Sequence Predict(int index){
        Sequence predictionFromAll = PredictorsHaveCommonPredictions(PredictorList, index, ExpectedCommonScore);
        return predictionFromAll;
    }

    static Sequence PredictorsHaveCommonPredictions(List<Predictor> predictorsList, int index, int expectedCommonScore){
        HashMap<Integer, Integer> countTableForAll = GetAnswerRanksFor(predictorsList, index);
        ScoreDistribution sdAll = new ScoreDistribution();
        for(Map.Entry<Integer, Integer> it : countTableForAll.entrySet()){

            sdAll.put(it.getKey(), it.getValue());
        }
        Integer predictionScore = sdAll.getBestScore();

        if (predictionScore != null && predictionScore >= expectedCommonScore) return new Sequence(sdAll.getBestValue());
        return new Sequence(Arrays.asList(-1));
    }

    static HashMap<Integer, Integer> GetAnswerRanksFor(List<Predictor> predictors, int index){
        HashMap countTable = new HashMap<Integer, Integer>();
        predictors.stream().forEach(predictor -> {
            List items = predictor.GetAnswerForIndex(index).GetItems();
            items.stream().forEach(item -> {
                if (countTable.containsKey(item)){
                    int freq = (int) countTable.get(item);
                    countTable.put(item, freq + 1);
                }
                else
                {
                    countTable.put(item, 1);
                }
            });
        });
        return countTable;
    }

    public int getCurrentRank() {
        return CurrentRank;
    }

    public void setCurrentRank(int currentRank) {
        CurrentRank = currentRank;
    }
}
