package ca.ipredict.controllers;

import java.io.IOException;
import java.util.*;

import ca.ipredict.helpers.StatsLogger;
import ca.ipredict.predictor.Evaluator;
import ca.ipredict.predictor.CPT.CPT.CPTPredictor;
import ca.ipredict.predictor.CPT.CPTPlus.CPTPlusPredictor;
import ca.ipredict.predictor.DG.DGPredictor;
import ca.ipredict.predictor.LZ78.LZ78Predictor;
import ca.ipredict.predictor.Markov.MarkovAllKPredictor;
import ca.ipredict.predictor.Markov.MarkovFirstOrderPredictor;
import ca.ipredict.predictor.TDAG.TDAGPredictor;


import info.debatty.java.stringsimilarity.*;

/**
 * This controller demonstrates how to compare all the predictors.
 * The results are expressed with various performance measures:
 * 
 * Success: is the ratio of successful predictions against the number of
 * wrong predictions, it is defined as (Number of success) / (Number of success +
 * number of failure)
 * 
 * Failure: is the inverse of the local accuracy: 1 - (Success)
 * 
 * No Match: is the ratio of unsuccessful prediction against the total num-
 * ber of tested sequences: (Number of sequence without prediction) / (number of
 * sequence)
 * 
 * Too Small: is the ratio of sequences too small to be used in the experimentation, it counts 
 * any sequence with a length smaller than the parameter consequentSize.
 * 
 * Overall: is our main measure to evaluates the accuracy of a given
 * predictor. It is the number of successful prediction against the total number of
 * tested sequences. (Number of success) / ( number of sequence)
 */
public class MainController {

	public static void main(String[] args) throws IOException {

			//Example on how to use the NormalizedLevenshtein through the java.strinsimilarity Library
			Levenshtein l = new Levenshtein();

			ArrayList<Integer> s1 = new ArrayList<Integer>();
			ArrayList<Integer> s2 = new ArrayList<Integer>();

			s1.add(10);
			s1.add(20);
			s1.add(30);
			s1.add(40);

			s2.add(10);
			s2.add(20);
			s2.add(30);
			s2.add(40);


	        System.out.println(distance(s1, s2));
	        System.out.println(l.distance("My string", "My $tring"));
	        System.out.println(l.distance("My string", "My $tring"));

	        System.exit(0);

	        //End of Example

			if (args.length < 1) {
				System.out.println("Missing required argument with data directory.");
				System.exit(1);
			}

			//instantiate the evaluator
			Evaluator evaluator = new Evaluator(args[0]);
			
			//Loading datasets
			evaluator.addDataset("BMS", 		5000);
			evaluator.addDataset("SIGN", 		1000);
			evaluator.addDataset("MSNBC", 		5000);
			evaluator.addDataset("BIBLE_WORD", 	5000);
			evaluator.addDataset("BIBLE_CHAR", 	5000);
			evaluator.addDataset("KOSARAK", 	45000);
			evaluator.addDataset("FIFA", 		5000);

			// evaluator.addDataset("SPICE0", 		5000);
			// evaluator.addDataset("SPICE1", 		5000);
			// evaluator.addDataset("SPICE2", 		5000);
			// evaluator.addDataset("SPICE3", 		5000);
			// evaluator.addDataset("SPICE4", 		5000);
			// evaluator.addDataset("SPICE5", 		5000);
			// evaluator.addDataset("SPICE6", 		5000);
			// evaluator.addDataset("SPICE7", 		5000);
			// evaluator.addDataset("SPICE8", 		5000);
			// evaluator.addDataset("SPICE9", 		5000);
			// evaluator.addDataset("SPICE10", 		5000);
			// evaluator.addDataset("SPICE11", 		5000);
			// evaluator.addDataset("SPICE12", 		5000);
			// evaluator.addDataset("SPICE13", 		5000);
			// evaluator.addDataset("SPICE14", 		5000);
			// evaluator.addDataset("SPICE15", 		5000);
			
			//Loading predictors
			evaluator.addPredictor(new DGPredictor("DG", "lookahead:4"));
			evaluator.addPredictor(new TDAGPredictor());
			evaluator.addPredictor(new CPTPlusPredictor("CPT+",		"CCF:true CBS:true"));
			evaluator.addPredictor(new CPTPredictor());
			evaluator.addPredictor(new MarkovFirstOrderPredictor());
			evaluator.addPredictor(new MarkovAllKPredictor());
			evaluator.addPredictor(new LZ78Predictor());
			
			//Start the experiment
			StatsLogger results = evaluator.Start(Evaluator.KFOLD, 14 , true, true, true);
	}


	public static final double distance(final ArrayList<Integer> s1, final ArrayList<Integer> s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        boolean flag = true;
        for(int itemList1 : s1)
        {
            if(!s2.contains(itemList1)) flag = false;
        }
        if (flag) return 0;

        if (s1.size() == 0) {
            return s2.size();
        }

        if (s2.size() == 0) {
            return s1.size();
        }

        // create two work vectors of integer distances
        int[] v0 = new int[s2.size() + 1];
        int[] v1 = new int[s2.size() + 1];
        int[] vtemp;

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s1.size(); i++) {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            // use formula to fill in the rest of the row
            for (int j = 0; j < s2.size(); j++) {
                int cost = 1;
                if (s1.get(i) == s2.get(j)) {
                    cost = 0;
                }
                v1[j + 1] = Math.min(
                        v1[j] + 1,              // Cost of insertion
                        Math.min(
                                v0[j + 1] + 1,  // Cost of remove
                                v0[j] + cost)); // Cost of substitution
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);

            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }

        //normalise


        int m_len = Math.max(s1.size(), s2.size());

        if (m_len == 0) {
            return 0;
        }

        return v0[s2.size()] / m_len;

        //return v0[s2.size()];
    }

}
