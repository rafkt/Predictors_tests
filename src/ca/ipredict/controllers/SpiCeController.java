package ca.ipredict.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import ca.ipredict.database.Item;
import ca.ipredict.predictor.profile.ProfileManager;



import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.ipredict.helpers.StatsLogger;
import ca.ipredict.predictor.Evaluator;
import ca.ipredict.predictor.CPT.CPT.CPTPredictor;
import ca.ipredict.predictor.CPT.CPTPlus.CPTPlusPredictor;
import ca.ipredict.predictor.DG.DGPredictor;
import ca.ipredict.predictor.LZ78.LZ78Predictor;
import ca.ipredict.predictor.Markov.MarkovAllKPredictor;
import ca.ipredict.predictor.Markov.MarkovFirstOrderPredictor;
import ca.ipredict.predictor.TDAG.TDAGPredictor;
import ca.ipredict.database.DatabaseHelper;
import ca.ipredict.database.Sequence;



import ca.ipredict.clustering.distanceFunctions.*;
import ca.ipredict.clustering.distanceFunctions.DistanceFunction;
import ca.ipredict.clustering.kmeans.AlgoKMeans;
import ca.ipredict.cluster.ClusterWithMean;
import ca.ipredict.cluster.DoubleArray;

import ca.ipredict.database.SequenceVector;

/**
 * Main controller to compare all the predictors
 */
public class SpiCeController {

	Predictor spicePredictor;

	public static void main(String[] args) throws IOException {
			prepareCPTPlus("../Spice_python_script/train_test_sets", "5.spice.train", 10);
			//getPrediction("18 18 14 2 6 10 13 15 8 15 13 18 11 15 6 18 13 9 12");
			return;
// 			if (args.length < 1) {
// 				System.out.println("Missing required argument with data directory.");
// 				System.exit(1);
// 			}

// 			//instantiate the evaluator
// 			Evaluator evaluator = new Evaluator(args[0]);
			
// 			//Loading data set
// 			evaluator.addDataset("BMS", 		5000);
// 			evaluator.addDataset("SIGN", 		1000);
// 			evaluator.addDataset("MSNBC", 		5000);
// 			evaluator.addDataset("BIBLE_WORD", 	5000);
// 			evaluator.addDataset("BIBLE_CHAR", 	5000);
// 			evaluator.addDataset("KOSARAK", 	45000);
// 			evaluator.addDataset("FIFA", 		5000);
			
// 			//Loading predictors
// 			evaluator.addPredictor(new DGPredictor("DG", "lookahead:4"));
// 			evaluator.addPredictor(new TDAGPredictor());
// 			evaluator.addPredictor(new CPTPlusPredictor("CPT+",		"CCF:true CBS:true"));
// //			evaluator.addPredictor(new CPTPlusPredictor("CPT++",		"CCF:false CBS:true"));
// 			evaluator.addPredictor(new CPTPredictor());
// 			evaluator.addPredictor(new MarkovFirstOrderPredictor());
// 			evaluator.addPredictor(new MarkovAllKPredictor());
// 			evaluator.addPredictor(new LZ78Predictor());
			
// 			//Start the experiment
// 			StatsLogger results = evaluator.Start(Evaluator.KFOLD, 14 , true, true, true);
	}

	public static void prepareCPTPlus(String pathToDataset, String DatasetName, int numClusterers)throws IOException {
			DatabaseHelper database = new DatabaseHelper(pathToDataset, DatasetName);
			ProfileManager.loadProfileByName(DatasetName.toString());
			database.loadDataset("SPICE", -1); //change the max value of 5000 // this should be changed to switch
			List<Sequence> dataset = new ArrayList<Sequence>(database.getDatabase().getSequences().subList(0, database.getDatabase().size()));

			//we should add a switch where different spredictors can be chosen.
			spicePredictor = new CPTPlusPredictor("CPT+","CCF:true CBS:true");
			spicePredictor.Train(dataset);
	}

	public static String getPrediction(String queryStr){
		//System.out.println(queryStr);
		Sequence query = new Sequence(0);
		String[] split = queryStr.split(" ");
		int count = Integer.valueOf(split[0].trim());
		//what if we have the empty query ?
		//think something about this

		//For large queries CPT+ cannot be effective.
		//I will try only the last 10 items of the query.

		if (count == 0) {
			Item item = new Item(-2);
			query.addItem(item);
			Sequence predicted = spicePredictorList.get(index).Predict(query);
			//System.out.println(predicted);
			return predicted.toString();
		}
		int start_index = count - 6 > 1 ? count - 6: 1;
		count = start_index > 1 ? 6: count;

		for (int i = start_index; i < split.length && count > 0; i++, count--){
			Item item = new Item(Integer.valueOf(split[i].trim()));
			query.addItem(item);
		}

		//System.out.println(query);
		Sequence predicted = spicePredictorList.get(index).Predict(query);
		//System.out.println(predicted);
		return predicted.toString();
	}

}
