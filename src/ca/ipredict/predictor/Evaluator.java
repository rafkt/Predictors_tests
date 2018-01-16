package ca.ipredict.predictor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.io.IOException;

import java.util.Collections;
import java.lang.Math;

import ca.ipredict.database.DatabaseHelper;
import ca.ipredict.database.DatabaseHelper.Format;
import ca.ipredict.database.Item;
import ca.ipredict.database.Sequence;
import ca.ipredict.database.SequenceStatsGenerator;
import ca.ipredict.database.SequenceDatabase;
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
	
	//Database
	private DatabaseHelper database;
	
	//public Stats stats;
	public StatsLogger stats;
	public List<StatsLogger> experiments;
	
	public List<String> datasets;  
	public List<Integer> datasetsMaxCount;  

	private HashMap<Item, HashMap<Item, Integer>> comesBefore; //needs initialisation
	private HashMap<Item, HashMap<Item, Integer>> comesAfter; //needs initialisation
	private HashMap<Item, Float> comesBeforeItemEntropy;
	private HashMap<Item, Float> comesAfterItemEntropy;
	
	
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

			comesBefore = new HashMap<Item, HashMap<Item, Integer>>();
			comesAfter = new HashMap<Item, HashMap<Item, Integer>>();
			comesBeforeItemEntropy = new HashMap<Item, Float>();
			comesAfterItemEntropy = new HashMap<Item, Float>();


			// // --do some debugging here - then comment this section

			// 			ArrayList<Sequence> training = new ArrayList<Sequence>();
			// 	//		//1 2 3 4
			// 			Sequence seq1 = new Sequence(-1);
			// 			seq1.addItem(new Item(1));
			// 			seq1.addItem(new Item(2));
			// 			seq1.addItem(new Item(3));
			// 			seq1.addItem(new Item(4));
			// 			training.add(seq1);
						
			// 			//1 2 3 4
			// 			Sequence seq2 = new Sequence(-1);
			// 			seq2.addItem(new Item(1));
			// 			seq2.addItem(new Item(2));
			// 			seq2.addItem(new Item(3));
			// 			seq2.addItem(new Item(4));
			// 			training.add(seq2);
						
			// 			//1 2 3 4
			// 			Sequence seq3 = new Sequence(-1);
			// 			seq3.addItem(new Item(1));
			// 			seq3.addItem(new Item(2));
			// 			seq3.addItem(new Item(3));
			// 			seq3.addItem(new Item(4));
			// 			training.add(seq3);
						
			// 	//		//0 1 2 4
			// 			Sequence seq4 = new Sequence(-1);
			// 			seq4.addItem(new Item(0));
			// 			seq4.addItem(new Item(1));
			// 			seq4.addItem(new Item(2));
			// 			seq4.addItem(new Item(4));
			// 			training.add(seq4);

			// 			setBeforeMatrix(training);
			// 			setAfterMatrix(training);

			// 			for (HashMap.Entry<Item, HashMap<Item, Integer>> entry : comesBefore.entrySet()) {
			// 				System.out.println(entry.getKey()+" :");
			// 			   	for (HashMap.Entry<Item, Integer> innerEntry : entry.getValue().entrySet()) {
			// 			   		System.out.println(innerEntry.getKey()+" : "+innerEntry.getValue());
			// 			   	}
						   
			// 			}
			// 			System.out.println("---------------------");
			// 			for (HashMap.Entry<Item, HashMap<Item, Integer>> entry : comesAfter.entrySet()) {
			// 				System.out.println(entry.getKey()+" :");
			// 			   for (HashMap.Entry<Item, Integer> innerEntry : entry.getValue().entrySet()) {
			// 			   		System.out.println(innerEntry.getKey()+" : "+innerEntry.getValue());
			// 			   }
						   
			// 			}

			// 			for (HashMap.Entry<Item, Float> entry : comesBeforeItemEntropy.entrySet()) {
			// 			   	System.out.println(entry.getKey()+" : "+entry.getValue()); 
			// 			}

			// 			for (HashMap.Entry<Item, Float> entry : comesAfterItemEntropy.entrySet()) {
			// 			   	System.out.println(entry.getKey()+" : "+entry.getValue()); 
			// 			}

			// 			System.exit(0);

			// // -- end of debugging section - comment it when not in need.

			setBeforeMatrix(getDatabaseCopy());
			setAfterMatrix(getDatabaseCopy());
			
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

	//constracts an ala-hankel matrix which will be used by CPT+ for doing predictions.
	public void setBeforeMatrix(List<Sequence> database){ //needs to be called - same function can be adopted form comesAfter Hashmap and pass as a parameter a reversed sequence database (or reverse on the fly every sequence)
		for (Sequence sequence : database) { // for every sequence
			List<Item> items = sequence.getItems();
			List<Item> met = new ArrayList<Item>();
			
			for (int i = 0; i < items.size(); i++){//goes through every item of the sequence
				Item item = items.get(i);
				//System.out.print(item + " : ");
				if (met.contains(item))continue;
				met.add(item);

				List<Item> seen = new ArrayList<Item>();
				for (int j = i + 1; j < items.size(); j++) {//counts what comes after every item
					Item itemAfter = items.get(j);
					//System.out.print(itemAfter + " ");
					if (seen.contains(itemAfter)) continue;
					seen.add(itemAfter);
					if (comesBefore.containsKey(item)){
						HashMap<Item, Integer> items_coming_before = comesBefore.get(item);
						if (items_coming_before.containsKey(itemAfter)){
							int counter = items_coming_before.get(itemAfter);
							items_coming_before.put(itemAfter, counter + 1);
						}else{
							items_coming_before.put(itemAfter, 1);
						}
					}else{
						HashMap<Item, Integer> items_coming_before = new HashMap<Item, Integer>();
						items_coming_before.put(itemAfter, 1);
						comesBefore.put(item,items_coming_before);
					}
				}
				//System.out.println("");
			}
		}

		//set the entropy Map for each map

		for (HashMap.Entry<Item, HashMap<Item, Integer>> entry : comesBefore.entrySet()) {
			int total = 0;
			//System.out.println(entry.getKey()+" :");
		   	for (HashMap.Entry<Item, Integer> innerEntry : entry.getValue().entrySet()) {
		   		//System.out.println(innerEntry.getKey()+" : "+innerEntry.getValue());
		   		total += innerEntry.getValue();
			}
			float h_i_sum = 0f;
			for (HashMap.Entry<Item, Integer> innerEntry : entry.getValue().entrySet()) {
		   		//System.out.println(innerEntry.getKey()+" : "+innerEntry.getValue());
		   		//System.out.println(innerEntry.getValue() / (float)total);
		   		h_i_sum += (innerEntry.getValue() / (float) total) * (Math.log(total / (float) innerEntry.getValue())/Math.log(2));
			}
			//System.out.println(h_i_sum);
			comesBeforeItemEntropy.put(entry.getKey(), h_i_sum);
		}
	}

	public void setAfterMatrix(List<Sequence> database){ //needs to be called - same function can be adopted form comesAfter Hashmap and pass as a parameter a reversed sequence database (or reverse on the fly every sequence)
		for (Sequence sequence : database) { // for every sequence

			//I should reverse the sequence here - hence I can use the setBeforeMatrix function to get the setAfter results
			List<Item> items = new ArrayList<Item>(sequence.getItems());
			Collections.reverse(items);

			List<Item> met = new ArrayList<Item>();
			
			for (int i = 0; i < items.size(); i++){//goes through every item of the sequence
				Item item = items.get(i);
				if (met.contains(item))continue;
				met.add(item);

				List<Item> seen = new ArrayList<Item>();
				for (int j = i + 1; j < items.size(); j++) {//counts what comes after every item
					Item itemAfter = items.get(j);
					if (seen.contains(itemAfter)) continue;
					seen.add(itemAfter);
					if (comesAfter.containsKey(item)){
						HashMap<Item, Integer> items_coming_before = comesAfter.get(item);
						if (items_coming_before.containsKey(itemAfter)){
							int counter = items_coming_before.get(itemAfter);
							items_coming_before.put(itemAfter, counter + 1);
						}else{
							items_coming_before.put(itemAfter, 1);
						}
					}else{
						HashMap<Item, Integer> items_coming_before = new HashMap<Item, Integer>();
						items_coming_before.put(itemAfter, 1);
						comesAfter.put(item,items_coming_before);
					}
				}
			}
		}

		//set the entropy Map for each map

		for (HashMap.Entry<Item, HashMap<Item, Integer>> entry : comesAfter.entrySet()) {
			int total = 0;
			//System.out.println(entry.getKey()+" :");
		   	for (HashMap.Entry<Item, Integer> innerEntry : entry.getValue().entrySet()) {
		   		//System.out.println(innerEntry.getKey()+" : "+innerEntry.getValue());
		   		total += innerEntry.getValue();
			}
			float h_i_sum = 0f;
			for (HashMap.Entry<Item, Integer> innerEntry : entry.getValue().entrySet()) {
		   		//System.out.println(innerEntry.getKey()+" : "+innerEntry.getValue());
		   		h_i_sum += (innerEntry.getValue() / (float) total) * (Math.log(total / (float) innerEntry.getValue())/Math.log(2));
			}
			comesAfterItemEntropy.put(entry.getKey(), h_i_sum);
		}
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
	public static Boolean isGoodPrediction(Sequence consequent, Sequence predicted) {
		
		Boolean hasError = false;
		
		for(Item it : predicted.getItems()) {
			
			Boolean isFound = false;
			for(Item re : consequent.getItems()) {
				if( re.val.equals(it.val) )
					isFound = true;
			}
			if(isFound == false)
				hasError = true;
			
		}
		
		
		return (hasError == false);
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
		
		//for each sequence; it classifies it and evaluates it
		for(Sequence target : testSequences) {
			
			//if sequence is long enough
			if(target.size() > (Profile.paramInt("consequentSize"))) {
				
				Sequence consequent = target.getLastItems(Profile.paramInt("consequentSize"),0); //the lasts actual items in target
				Sequence finalTarget = target.getLastItems(Profile.paramInt("windowSize"),Profile.paramInt("consequentSize"));
				
				Sequence predicted;

				if (predictors.get(classifierId).getTAG() == "CPT+") predicted = predictors.get(classifierId).Predict(finalTarget,  comesBeforeItemEntropy);
				else predicted = predictors.get(classifierId).Predict(finalTarget);
				
				//if no sequence is returned, it means that they is no match for this sequence
				if(predicted.size() == 0) {
					stats.inc("No Match", predictors.get(classifierId).getTAG());
				}
				//evaluates the prediction
				else if(isGoodPrediction(consequent, predicted)) {
					stats.inc("Success", predictors.get(classifierId).getTAG());
				}
				else {
					stats.inc("Failure", predictors.get(classifierId).getTAG());
				}
				
			}
			//sequence is too small
			else {
				stats.inc("Too Small", predictors.get(classifierId).getTAG());
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
