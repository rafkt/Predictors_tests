package ca.ipredict.predictor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ca.ipredict.database.DatabaseHelper;
import ca.ipredict.database.DatabaseHelper.Format;
import ca.ipredict.database.Item;
import ca.ipredict.database.Sequence;
import ca.ipredict.database.SequenceStatsGenerator;
import ca.ipredict.helpers.MemoryLogger;
import ca.ipredict.helpers.StatsLogger;
import ca.ipredict.predictor.profile.Profile;
import ca.ipredict.predictor.profile.ProfileManager;

/**
 * Evaluation framework
 */
public class Evaluator {

	private List<Predictor> predictors; //list of predictors
	
	//Sampling type
	public final static int HOLDOUT = 0;
	public final static int KFOLD = 1;
	public final static int RANDOMSAMPLING = 2; 
	
	//statistics
	private long startTime;
	private long endTime;

	private int foldCount;
	
	//Database
	private DatabaseHelper database;
	
	//public Stats stats;
	public StatsLogger stats;
	public List<StatsLogger> experiments;
	
	public List<String> datasets;  
	public List<Integer> datasetsMaxCount;  

	private HashMap<Item, Integer> itemFrequencies;
	private int totalTrainingLength;
	private int maxFreq;
	private int minFreq;
	
	
	public Evaluator(String pathToDatasets) {
		predictors = new ArrayList<Predictor>();
		datasets = new ArrayList<String>();
		datasetsMaxCount = new ArrayList<Integer>();
		database = new DatabaseHelper(pathToDatasets);
	}
	
	/**
	 * Adds a Predictor to the list of predictors
	 * @param predictor
	 */
	public void addPredictor(Predictor predictor) {
		predictors.add(predictor);
	}
	
	/**
	 * Adds a dataset to the experiment
	 * @param format Format of the Dataset
	 * @param maxCount Maximum number of sequence to read in the dataset
	 */
	public void addDataset(String format, int maxCount) {
		datasets.add(format);
		datasetsMaxCount.add(maxCount);
	}

	/**
	 * Start the controller using the prefered SamplingRate on the list of predictor
	 * @param samplingType one of: HOLDOUT, RANDOMSAMPLING, KFOLD
	 * @param param The parameter associated with the sampling type
	 * @param showDatasetStats show statistics about the dataset
	 * @return 
	 */
	public StatsLogger Start(int samplingType, float param, boolean showResults, boolean showDatasetStats, boolean showExecutionStats) throws IOException {
	
		//Setting statsLogger
		List<String> statsColumns = new ArrayList<String>();
		statsColumns.add("Success");
		statsColumns.add("Failure");
		statsColumns.add("No Match");
		statsColumns.add("Too Small");
		statsColumns.add("Overall");
		statsColumns.add("Size (MB)");
		statsColumns.add("Train Time");
		statsColumns.add("Test Time");
		
		//Extracting the name of each predictor
		List<String> predictorNames = new ArrayList<String>();
		for(Predictor predictor : predictors) {
			predictorNames.add(predictor.getTAG());
		}
		
		for(int i = 0; i < datasets.size(); i++) {
			
			int maxCount = datasetsMaxCount.get(i);
			String format = datasets.get(i);
			
			//Loading the parameter profile
			ProfileManager.loadProfileByName(format.toString());
			
			//Loading the dataset
			database.loadDataset(format, maxCount);
			
			if(showDatasetStats) {
				System.out.println();
				SequenceStatsGenerator.prinStats(database.getDatabase(), format);
			}
			
			//Creating the statsLogger
			stats = new StatsLogger(statsColumns, predictorNames, false);
			
			//Saving current time for across time analysis
			startTime = System.currentTimeMillis();
			
			//For each predictor, do the sampling and do the training/testing
			for(int id = 0 ; id < predictors.size(); id++) {
				
				//Picking the sampling strategy
				switch(samplingType) {
					case HOLDOUT:
						Holdout(param, id);
						break;
				
					case KFOLD:
						KFold((int)param, id);
						break;
						
					case RANDOMSAMPLING:
						RandomSubSampling(param, id);
						break;
					
					default: 
						System.out.println("Unknown sampling type."); 
				}
			}
			//Saving end time
			endTime = System.currentTimeMillis();
			
			finalizeStats(showExecutionStats);
			
			if(showResults == true) {
				System.out.println(stats.toString());
			}
		}
		
		return stats;
	}

	/**
	 * Holdout method
	 * Data are randomly partitioned into two sets (a training set and a test set) using a ratio.
	 * The classifier is trained using the training set and evaluated using the test set.
	 * @param ratio to divide the training and test sets
	 */
	public void Holdout(double ratio, int classifierId) {
		
		List<Sequence> trainingSequences = getDatabaseCopy();
		List<Sequence> testSequences = splitList(trainingSequences, ratio);
		
		//DEBUG
		//System.out.println("Dataset size: "+ (trainingSequences.size() + testSequences.size()));
		//System.out.println("Training: " + trainingSequences.size() + " and Test set: "+ testSequences.size());
		
		PrepareClassifier(trainingSequences, classifierId); //training (preparing) classifier
		
		StartClassifier(testSequences, classifierId); //classification of the test sequence
	}
	
	/**
	 * Random subsampling
	 * Holdout method repeated 10 times
	 * @param ratio to use for the holdout method
	 */
	public void RandomSubSampling(double ratio, int classifierId) {
		
		int k = 10;
		for(int i = 0 ; i < k; i++) {
			Holdout(ratio, classifierId);
			
			//Logging memory usage
			MemoryLogger.addUpdate();
		}
		
	}

	private void setItemsFrequencies (List<Sequence> trainingSet){
		itemFrequencies = new HashMap<Item, Integer>();
		totalTrainingLength = 0;
		maxFreq = 0;
		minFreq = Integer.MAX_VALUE;;

		//task of the this function is to set the above variables

		for (Sequence seq : trainingSet){
			for (Item it : seq.getItems()){
				totalTrainingLength++;

				Integer value = itemFrequencies.get(it);
				if (value == null) value = 0;
				itemFrequencies.put(it, ++value);
			}
		}

		for (Map.Entry<Item, Integer> es : itemFrequencies.entrySet()) {
			int value2check = es.getValue();
			if (value2check > maxFreq) maxFreq = value2check;
			if (value2check < minFreq) minFreq = value2check;
    	}

    	//System.out.println(maxFreq + " " + minFreq + " " + totalTrainingLength);

	}

	private double calculateScore (Sequence Predicted){

		// fetch the frequency of Predicted and normalise it according to maxFreq/minFreq for log_2(1/p)

		double min_log = (Math.log(totalTrainingLength / minFreq)/Math.log(2));
		double max_log = (Math.log(totalTrainingLength / maxFreq)/Math.log(2));
		double predicted_log = (Math.log(totalTrainingLength / itemFrequencies.get(Predicted.getItems().get(0)))/Math.log(2));

		// System.out.println(min_log + " " + max_log + " " + predicted_log);

		//calculating log_2 is done by
		// (Math.log(----)/Math.log(2))

		//return the result
		return (predicted_log - min_log) / (double)(max_log - min_log);
	}
	
	/**
	 * k-fold cross-validation
	 * Data are partitioned in k exclusive subsets (folds) of same size.
	 * Training and testing is done k times. For each time; a fold is used for testing 
	 * and the k-1 other folds for training
	 */
	public void KFold(int k, int classifierId) {
		
		//k has to be at least 2
		if(k < 2) {
			throw new RuntimeException("K needs to be 2 or more");
		}

		List<Sequence> dataSet = getDatabaseCopy();
		
		//calculating absolute ratio
		double relativeRatio = 1/(double)k;
		int absoluteRatio = (int) (dataSet.size() * relativeRatio);
		
		foldCount = 0;
		//For each fold, it does training and testing
		for(int i = 0 ; i < k ; i++) {

			//Partitioning database 
			//
			int posStart = i * absoluteRatio; //start position of testing set
			int posEnd = posStart + absoluteRatio; //end position of testing set
			if(i == (k-1)) { //if last fold we adjust the size to include all the left-over sequences
				posEnd = dataSet.size(); //special case
			}
			
			//declaring the sets
			List<Sequence> trainingSequences = new LinkedList<Sequence>();
			List<Sequence> testSequences = new LinkedList<Sequence>();
			
			//actual partitioning
			for(int j = 0 ; j < dataSet.size(); j++) {
				
				Sequence toAdd = dataSet.get(j);
				
				//is in testing set
				if(j >= posStart && j < posEnd) {
					testSequences.add(toAdd);
				}
				else {
					trainingSequences.add(toAdd);
				}
			}
			//
			//End of Partitioning
			
			setItemsFrequencies(trainingSequences);
			PrepareClassifier(trainingSequences, classifierId); //training (preparing) classifier	
			StartClassifier(testSequences, classifierId); //classification of the test sequence
			
			//Logging memory usage
			MemoryLogger.addUpdate();
		}
		
	}
	
	/**
	 * Display the stats for the experiment
	 * @param showExecutionStats
	 */
	public void finalizeStats(boolean showExecutionStats) {
		
		//For each predictor, updates the stats
		for(Predictor predictor : predictors) {
			
			int success = (int)(stats.get("Success", predictor.getTAG()));
			int failure = (int)(stats.get("Failure", predictor.getTAG()));
			int noMatch = (int)(stats.get("No Match", predictor.getTAG()));
			int tooSmall =(int)(stats.get("Too Small", predictor.getTAG()));
			
			
			long matchingSize = success + failure; //For relative success (success / (success + failure))
			long testingSize = matchingSize + noMatch + tooSmall; //For global success (success / All_the_testing)
			
			stats.divide("Success", predictor.getTAG(), matchingSize);
			stats.divide("Failure", predictor.getTAG(), matchingSize);
			stats.divide("No Match", predictor.getTAG(), testingSize);
			stats.divide("Too Small", predictor.getTAG(), testingSize);
			
			stats.divide("Train Time", predictor.getTAG(), 100);
			stats.divide("Test Time", predictor.getTAG(), 100);
			
			//Adding overall success
			stats.set("Overall", predictor.getTAG(), success);
			stats.divide("Overall", predictor.getTAG(), testingSize);
			
			//Size of the predictor
			stats.set("Size (MB)", predictor.getTAG(), predictor.memoryUsage());
			stats.divide("Size (MB)", predictor.getTAG(), (100 * 1000 * 1000));
			
			
		}
		
		if(showExecutionStats) {
	        //memory usage
	  		MemoryLogger.addUpdate();
	        MemoryLogger.displayUsage();
	        
	        //Displaying the execution time
	        System.out.println("Execution time: "+ (endTime - startTime) / 1000 + " seconds");
		}
	}
	
	/**
	 * Tell whether the predicted sequence match the consequent sequence
	 */
	public static float isGoodPrediction(Sequence consequent, Sequence predicted) {
		
		Boolean hasError = false;
		float score = 0f;
		
		for(Item it : predicted.getItems()) {
			
			Boolean isFound = false;
			int counter = 0;
			for(Item re : consequent.getItems()) {
				if(re.val.equals(it.val)){
					if (counter == 0 && score == 0.f) score = 1f;
					else if (counter == 1 && score == 0.f) score = 0.5f;
					//isFound = true;
				}
				counter++;
			}
			//if(isFound == false)
			//	hasError = true;
			
		}
		
		
		return score;//(hasError == false);
	}
	

	
	private void PrepareClassifier(List<Sequence> trainingSequences, int classifierId) {
		long start = System.currentTimeMillis(); //Training starting time
		
		predictors.get(classifierId).Train(trainingSequences); //actual training
		
		long end = System.currentTimeMillis(); //Training ending time
		double duration = (double)(end - start) / 1000;
		stats.set("Train Time", predictors.get(classifierId).getTAG(), duration);
	}
	
	private void StartClassifier(List<Sequence> testSequences, int classifierId) {	
		
		long start = System.currentTimeMillis(); //Testing starting time

		int printCounter = 10;
		foldCount++;
		
		//for each sequence; it classifies it and evaluates it
		for(Sequence target : testSequences) {

			//System.out.println(database.mapSequenceToSetence.get(target));
			
			//if sequence is long enough
			if(target.size() > (Profile.paramInt("consequentSize"))) {
				
				Sequence consequent = target.getLastItems(2/**Profile.paramInt("consequentSize")*/,0); //the lasts actual items in target
				Sequence finalTarget = target.getLastItems(Profile.paramInt("windowSize"),Profile.paramInt("consequentSize"));
				
				Sequence predicted = predictors.get(classifierId).Predict(finalTarget);

				float weight = isGoodPrediction(consequent, predicted);
				//if no sequence is returned, it means that they is no match for this sequence
				int result = -1;
				if(predicted.size() == 0) {
					stats.inc("No Match", predictors.get(classifierId).getTAG(), 1);
				}
				//evaluates the prediction
				else if( weight > 0) {
					double score = calculateScore(predicted);
					//System.out.println(score);
					stats.inc("Success", predictors.get(classifierId).getTAG(), score * weight);
					result = 1;
				}
				else {
					stats.inc("Failure", predictors.get(classifierId).getTAG(), 1);
					result = 0;
				}
				// if (printCounter > 0 && foldCount == 1){
				// 	printCounter--;
				// 	System.out.println(predictors.get(classifierId).getTAG());
				// 	if(predicted.size() != 0) System.out.println(database.mapSequenceToSetence.get(target) + " --> prediction: " + database.mapItemToString.get(predicted.get(0).val) + " " + result);
				// }
			}
			//sequence is too small
			else {
				stats.inc("Too Small", predictors.get(classifierId).getTAG(), 1);
			}
		}
		
		long end = System.currentTimeMillis(); //Training ending time
		double duration = (double)(end - start) / 1000;
		stats.set("Test Time", predictors.get(classifierId).getTAG(), duration);
	}

	private List<Sequence> splitList(List<Sequence> toSplit, double absoluteRatio){
		
		int relativeRatio = (int) (toSplit.size() * absoluteRatio); //absolute ratio: [0.0-1.0]
		
		List<Sequence> sub=toSplit.subList(relativeRatio , toSplit.size());
		List<Sequence> two= new ArrayList<Sequence>(sub);
		sub.clear();
		
		return two;
	}
	
	private List<Sequence> getDatabaseCopy() {
		return new ArrayList<Sequence>(database.getDatabase().getSequences().subList(0, database.getDatabase().size()));
	}
	
}
