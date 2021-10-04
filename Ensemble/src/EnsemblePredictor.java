import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class EnsemblePredictor {

    private List<Predictor> PredictorsList;
    private List<EnsembleConfiguration> Pool;
    private int LastUsedPredictorIndex;

    public static enum Predictors{
        CPTPlus,
        SUBSEQPrime,
        DG,
        TDAG,
        Mark1,
        AKOM,
        LZ78
    };
    public static enum Mode{
        Naive,
        Enhanced,
        Intelligent,
        Recommendations
    }

    public EnsemblePredictor(String dataset, int fold){
        PredictorsList = new ArrayList<Predictor>();
        for (Predictors predictor : Predictors.values()) {
            List<Sequence> answersFromFile = ReadFromFile("../../outputs-ensemble/" + dataset + ".fold." + fold +
                    ".answers." + (predictor.equals(Predictors.CPTPlus) ? "CPT+" : predictor.toString()) + ".mapped.txt");
            PredictorsList.add(new Predictor(predictor.toString(), answersFromFile));
        }
        EnsembleIntelligentWithIsolationSetup();
    }

    public Sequence Predict(int index, Mode mode) throws Exception {
        switch (mode){
            case Naive:
                return Naive(index);
            case Enhanced:
                return Enhanced(index);
            case Intelligent:
                return Intelligent(index);
            case Recommendations:
                return Recommendations(index);
        }
        throw new Exception("Not Predict mode was defined");
    }

    public void ProvideFeedback(){
        int currentRank = Pool.get(LastUsedPredictorIndex).getCurrentRank();
        currentRank++;
        Pool.get(LastUsedPredictorIndex).setCurrentRank(currentRank);
        Collections.sort(Pool, comparing(EnsembleConfiguration::getCurrentRank).reversed());
    }

    private Sequence Naive(int index){

        HashMap<Integer, Integer> countTableForAll = EnsembleConfiguration.GetAnswerRanksFor(PredictorsList, index);
        ScoreDistribution sd = new ScoreDistribution();
        for(Map.Entry<Integer, Integer> it : countTableForAll.entrySet()){

            sd.put(it.getKey(), it.getValue());
        }
        List<Integer> prediction = sd.getBestValue();
        return new Sequence(prediction);
    }

    public boolean HasValidInit(int expectedNumberOfAnswersInit){
        PredictorsList.stream().forEach(predictor -> {
            if (predictor.GetAnswers().size() != expectedNumberOfAnswersInit){
                new Exception("Inconsistency detected");
            }
        });
        return true;
    }

    private Sequence Enhanced(int index){

        Sequence predictionFromAll = EnsembleConfiguration.PredictorsHaveCommonPredictions(PredictorsList, index, PredictorsList.size() - 2);
        if (predictionFromAll.IsValid()) return predictionFromAll;

        Sequence predictionFromLossy = EnsembleConfiguration.PredictorsHaveCommonPredictions(GetLossyPredictors(), index, GetLossyPredictors().size() - 1);
        if (predictionFromLossy.IsValid()) return predictionFromLossy;

        Sequence predictionFromLossless = EnsembleConfiguration.PredictorsHaveCommonPredictions(GetLosslessPredictors(), index, GetLosslessPredictors().size());
        if (predictionFromLossless.IsValid()) return predictionFromLossless;

        Sequence predictionFromTdagAkom = EnsembleConfiguration.PredictorsHaveCommonPredictions(GetTdagAkom(), index, 2);
        if (predictionFromTdagAkom.IsValid()) return predictionFromTdagAkom;

        Sequence predictionFromCPTPlus = EnsembleConfiguration.PredictorsHaveCommonPredictions(PredictorsList.stream().filter(predictor -> predictor.GetTag() == "CPT+").collect(Collectors.toList()), index, 1);
        if (predictionFromCPTPlus.IsValid()) return predictionFromCPTPlus;

        Sequence predictionFromSUBSEQ = EnsembleConfiguration.PredictorsHaveCommonPredictions(PredictorsList.stream().filter(predictor -> predictor.GetTag() == Predictors.SUBSEQPrime.toString()).collect(Collectors.toList()), index, 1);
        return predictionFromSUBSEQ;
    }

    private Sequence Intelligent(int index){
        LastUsedPredictorIndex = 0;

        for (EnsembleConfiguration configuration:
             Pool) {
            Sequence prediction = configuration.Predict(index);
            if (prediction.IsValid()) return prediction;
            LastUsedPredictorIndex++;
        }
        LastUsedPredictorIndex = Pool.size() - 1;
        return Pool.get(Pool.size() - 1).Predict(index);
    }

    private Sequence Recommendations(int index) {
        HashMap<Integer, Integer> predictionFromAll = EnsembleConfiguration.GetAnswerRanksFor(PredictorsList, index);
        ScoreDistribution sortedMap = new ScoreDistribution();
        for(Map.Entry<Integer, Integer> it : predictionFromAll.entrySet()){

            sortedMap.put(it.getKey(), it.getValue());
        }

        return sortedMap.ToSequence();
    }

    private List<Predictor> GetLosslessPredictors(){
        return PredictorsList.stream()
                .filter(predictor -> predictor.GetTag() != Predictors.DG.toString())
                .filter(predictor -> predictor.GetTag() != Predictors.TDAG.toString())
                .filter(predictor -> predictor.GetTag() != Predictors.AKOM.toString())
                .filter(predictor -> predictor.GetTag() != Predictors.LZ78.toString())
                .filter(predictor -> predictor.GetTag() != Predictors.Mark1.toString())
                .collect(Collectors.toList());
    }

    private List<Predictor> GetTdagAkom(){
        return PredictorsList.stream()
                .filter(predictor -> predictor.GetTag() != Predictors.DG.toString())
                .filter(predictor -> predictor.GetTag() != Predictors.LZ78.toString())
                .filter(predictor -> predictor.GetTag() != Predictors.Mark1.toString())
                .filter(predictor -> predictor.GetTag() != "CPT+")
                .filter(predictor -> predictor.GetTag() != Predictors.SUBSEQPrime.toString())
                .collect(Collectors.toList());
    }

    private List<Predictor> GetLossyPredictors(){
        return PredictorsList.stream()
                .filter(predictor -> predictor.GetTag() != "CPT+")
                .filter(predictor -> predictor.GetTag() != Predictors.SUBSEQPrime.toString())
                .collect(Collectors.toList());
    }



    private Predictor GetPredictor(Predictors tag){
        return PredictorsList.stream()
                .filter(predictor -> predictor.GetTag() == (tag.toString().equals("CPTPlus") ? "CPT+" : tag.toString()))
                .findFirst()
                .get();
    }

    private void EnsembleIntelligentSetup(){
        LastUsedPredictorIndex = 0;
        Pool = new ArrayList<EnsembleConfiguration>();
        Pool.add(new EnsembleConfiguration(PredictorsList, PredictorsList.size() - 2, 5));
        Pool.add(new EnsembleConfiguration(GetLossyPredictors(), GetLossyPredictors().size() - 1, 4));
        Pool.add(new EnsembleConfiguration(GetLosslessPredictors(), GetLosslessPredictors().size(), 3));
        Pool.add(new EnsembleConfiguration(GetTdagAkom(), 2, 2));
        Pool.add(new EnsembleConfiguration(PredictorsList.stream().filter(predictor -> predictor.GetTag() == "CPT+").collect(Collectors.toList()), 1, 1));
        Pool.add(new EnsembleConfiguration(PredictorsList.stream().filter(predictor -> predictor.GetTag() == Predictors.SUBSEQPrime.toString()).collect(Collectors.toList()), 1, 0));
    }

    private void EnsembleIntelligentSetupV2(){
        LastUsedPredictorIndex = 0;
        Pool = new ArrayList<EnsembleConfiguration>();
        Pool.add(new EnsembleConfiguration(PredictorsList.stream().filter(predictor -> predictor.GetTag() == Predictors.SUBSEQPrime.toString()).collect(Collectors.toList()), 1, 5));
        Pool.add(new EnsembleConfiguration(PredictorsList.stream().filter(predictor -> predictor.GetTag() == "CPT+").collect(Collectors.toList()), 1, 4));
        Pool.add(new EnsembleConfiguration(GetTdagAkom(), 2, 3));
        Pool.add(new EnsembleConfiguration(PredictorsList, PredictorsList.size() - 2, 2));
        Pool.add(new EnsembleConfiguration(GetLossyPredictors(), GetLossyPredictors().size() - 1, 1));

    }

    private void EnsembleIntelligentWithIsolationSetup(){
        LastUsedPredictorIndex = 0;
        Pool = new ArrayList<EnsembleConfiguration>();
        PredictorsList.stream().forEach(predictor -> Pool.add(new EnsembleConfiguration(Arrays.asList(predictor), 1, 0)));
    }

    static List<Sequence> ReadFromFile(String fileName){

        List<Sequence> fileContents = new ArrayList<Sequence>();

        String line = null;
        try {
            FileReader fileReader =
                    new FileReader(fileName);

            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                List<Integer> lineArray = new ArrayList<Integer>();
                String[] items = line.split(" ");
                Arrays.stream(items).forEach(i -> {
                    lineArray.add(Integer.parseInt(i));
                });
                fileContents.add(new Sequence(lineArray));
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }

        return fileContents;
    }

}
