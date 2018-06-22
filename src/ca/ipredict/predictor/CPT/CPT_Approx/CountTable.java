package ca.ipredict.predictor.CPT.CPT_Approx;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.*;

import ca.ipredict.database.Item;
import ca.ipredict.database.Sequence;
import ca.ipredict.helpers.ScoreDistribution;

import ca.ipredict.predictor.CPT.CPT_Approx.LevenshteinDistance;



import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.*;
import java.util.Locale;
import java.util.Set;

import org.simmetrics.SetMetric;
import org.simmetrics.StringDistance;
import org.simmetrics.StringMetric;
import org.simmetrics.builders.StringDistanceBuilder;
import org.simmetrics.builders.StringMetricBuilder;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.EuclideanDistance;
import org.simmetrics.metrics.OverlapCoefficient;
import org.simmetrics.metrics.StringMetrics;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;

import org.simmetrics.metrics.SmithWaterman;
import org.simmetrics.metrics.SmithWatermanSetMetric;



/**
 * Represents a CountTable for the CPT Predictor
 */
public class CountTable {

	/**
	 * Internal representation of the CountTable
	 */
	private TreeMap<Integer, Float> table;
	private HashSet<Integer> branchVisited;
	private CPTHelper helper;
	private SmithWatermanSetMetric<Integer> sw;
	
	/**
	 * Basic controller
	 */
	public CountTable(CPTHelper helper) {
		table = new TreeMap<Integer, Float>();
		branchVisited = new HashSet<Integer>();
		this.helper = helper;
		sw = new SmithWatermanSetMetric<>();
	}

	/**
	 * Calculate the score for an item and push the score to the CountTable, 
	 * if a key already exists then the given value is added to the old one
	 * @param curSeqLength Size of the sequence that contains the item
	 * @param fullSeqLength Size of the sequence before calling recursive divider
	 * @param numberOfSeqSameLength Number of similar sequence
	 */
	public void push(Integer key, int curSeqLength, int fullSeqLength, int dist) {
				
		//Declare the various weights
		//float weightLevel = 1f /numberOfSeqSameLength; //from [1.0,0[  -> higher is better
		float weightDistance = 1f / dist; //from [1.0,0[ -> higher is better
		//float distLevel = level;//1f / (level /*+ 1*/); //from [1.0,0[ -> higher is better
//		float weightLength = (float)curSeqLength / fullSeqLength; //from [1.0,0[ -> higher is better
//		float weightLength = (float)fullSeqLength / curSeqLength; //from [1.0,0[ -> higher is better
	
		//calculate the value for the current key 
//		float curValue = (weightLevel * 0.5f) + (weightLength * 5.0f) + (weightDistance * 1.8f);
//		float curValue = (weightLevel * 1f) + (weightLength * 1f) + (weightDistance * 0.0001f);
		//float curValue = (weightLevel * 1f) + (1f) + (weightDistance * 0.0001f);
		//int boost = (curSeqLength / (float)fullSeqLength) >= 0.9f ? 1 : 1;
		//if (boost == 2) System.out.println("Boosted");
		float curValue = /*(/*Maybe I need 1 - weightLevel*//* distLevel * 1f) +*/ ((curSeqLength / (float)fullSeqLength)) + (1f) + (weightDistance * 0.0001f);
		
		//Update the count table
		Float oldVal = table.get(key);
		if(oldVal == null) {
			table.put(key, curValue);
		}
		else {
			table.put(key, oldVal * curValue);
		}		
	}

	
	/**
	 * Update this CountTable with a sequence S, it finds the similar sequence SS of S
	 * All the selected items from SS are used to update the CountTable
	 * @param predictor Predictor used to access its data structures
	 * @param sequence Sequence to use to update the CountTable
	 * @param initialSequenceSize The initial size of the sequence to predict (used for weighting)
	 */
	public int update(Item[] sequence, int initialSequenceSize, int upperLever) {

		int branchesUsed = 0;

		//skipping a query item starting from the 1st
		//for (int i = 0; i < sequence.length - 1; i ++){

			//Item[] subseq = Arrays.copyOfRange(sequence, i, sequence.length);

			//Bitvector ids = helper.getSimilarSequencesIds(subseq);

			//for each level of distance (levenshtein)
			//int level = 1; // starting from exact matches
			//for (; level <= 10 ; level++){
				//if (sequence.length / level < 2) break;
			//For each sequence similar of the given sequence
				//for(int id = ids.nextSetBit(0); id >= 0 ; id = ids.nextSetBit(id + 1)) {
					
					// if(branchVisited.contains(id)) {
					// 	continue;
					// }

				Map<Integer, PredictionTree> map = helper.predictor.LT;
				for (Map.Entry<Integer, PredictionTree> entry : map.entrySet()){
					//System.out.println(entry.getKey() + "/" + entry.getValue());
					
					
					//extracting the sequence from the PredictionTree
					Item[] retrieved_seq = helper.getSequenceFromId(entry.getKey()/*id*/);

					List<Integer> ret_seqList = new ArrayList<Integer>();
					for (Item item : retrieved_seq) ret_seqList.add(item.val);

					List<Integer> sequenceList = new ArrayList<Integer>();
					for (Item item : sequence) sequenceList.add(item.val);

					//Levenshtein distance - if the distance does not meet our criteria then we abort.

					float dist = sw.compare(ret_seqList, sequenceList);//LevenshteinDistance.distance(seqList, sequenceList); //not ready yet
					if (dist == 0) continue;
					/*
						Now I can get the indices from the alignment. getFirstLocalIndex() for the index in ret_seqList and getSecondLocalIndex() for the index in sequenceList
						.. Remember we want to see if we have an exact representation of the sequence in the retrieved_seq. We can always substitute one or two items.
						The idea to go is: Get the indices. 
							For the getFirstLocalIndex() getSecondLocalIndex() you go foward for both sequences and see if they match.
							Increase sub counter if they not.
							If sub-counter gets more than we want abort and continue with next retrieved sequence
							Otherwise, as soon as we finish going foward, start again from the getFirstLocalIndex() getSecondLocalIndex() indicies and go backwards.
							Repeat as before.
							Add to the count table the items from the retrieved sequence, starting after the "sequence" items.
					*/

					int subs = 2;
					int consequent_index = -1;

					int seq_i = sw.getSecondLocalIndex() + 1;
					int ret_i = sw.getFirstLocalIndex() + 1;
					while(seq_i < sequenceList.size() && ret_i < ret_seqList.size()){
						if (!sequenceList.get(seq_i).equals(ret_seqList.get(ret_i))) subs--;
						if (subs < 0) break;
						seq_i++;
						ret_i++;
					}
					if (subs >= 0 && seq_i == sequenceList.size()){
						consequent_index = ret_i;
						seq_i = sw.getSecondLocalIndex() - 1;
						ret_i = sw.getFirstLocalIndex() - 1;
						while(seq_i >= 0 && ret_i >= 0){
							if (!sequenceList.get(seq_i).equals(ret_seqList.get(ret_i))) subs--;
							if (subs < 0) break;
							seq_i--;
							ret_i--;
						}
					}else continue;

					if (subs < 0 || seq_i >= 0) continue; //update smith-water jar.. after this line you can add the consequent of ret_sequence to the cout table.

					//System.out.print(ret_seqList + " <--- " + sequenceList + " <--- ");

					//if (dist < 1.0) continue;

					//if I continue then add it to branchVisited

					//branchVisited.add(id);
					
					//Generating a set of all the items from sequence
					// HashSet<Item> toAvoid = new HashSet<Item>();
					// for (int local_j = sw.getSecondLocalIndex() + 1; local_j < subseq.length - 1; local_j++){//(Item item : subseq) {
					// 	toAvoid.add(subseq[local_j]);
					// }
					

					//Updating this CountTable with the items {S}
					//Where {S} contains only the items that are in seq after
					//all the items from sequence have appeared at least once
					//Ex:	
					//	sequence: 	A B C
					//  seq: 		X A Y B C E A F
					//	{S}: 		E F
					int max = 3;//99; //used to limit the number of items to push in the count table
					int count = 1; //current number of items already pushed
					for (int local_i = consequent_index; local_i < retrieved_seq.length; local_i++){//(Item item : seq) {
						//only enters this if toAvoid is empty
						//it means that all the items of toAvoid have been seen
						if(/*toAvoid.size() == 0 &&*/ count < max) {
							//System.out.print(" " + retrieved_seq[local_i].val + " ");
							//calculating the score for this item
							push(retrieved_seq[local_i].val, sequence.length, initialSequenceSize, count);
							count++;
						} else break;
						//else if(toAvoid.contains(seq[local_i])) {
						//	toAvoid.remove(seq[local_i]);
						//}
					}
					//System.out.println();


					// int matchedSequenceLocalIndex = sw.getFirstLocalIndex();

					// if (toAvoid.size() > 0){
					// 	for (int local_i = sw.getFirstLocalIndex() + 1; local_i < seq.length - 1; local_i++){
					// 		if (count < max){
					// 			push(seq[local_i].val, seq.length, initialSequenceSize, ids.cardinality(), count, dist/*level*/);
					// 			count++;
					// 		}else{break;}
					// 	}
					// }
					

					//meaning that the count table has been really updated
					if(count > 1 ) {
						branchesUsed++;
					}//else {System.out.println("NOPE");}
				}

			//}
		//}
		//if (branchesUsed == 0){System.out.println("NOPE");}
		return branchesUsed;
	}
	
	/**
	 * Return a sequence containing the highest scored items from
	 * the counts table
	 * @param count Number of items to put in the sequence
	 * @param II The inverted index corresponding
	 * @return The sequence containing the |count| best items sorted from the CountTable
	 */
	public Sequence getBestSequence(int count) {
		
		//Iterating through the CountTable to sort the items by score
		ScoreDistribution<Integer> sd = new ScoreDistribution<Integer>();
		for(Entry<Integer, Float> it : table.entrySet()) {
			
			//the following measure of confidence and lift are "simplified" but are exactly the same as in the literature.
			//CONFIDENCE : |X -> Y|
			//LIFT: CONFIDENCE(X -> Y) / (|Y|)
			double confidence = it.getValue();
//			double support = helper.predictor.II.get(it.getKey()).cardinality();
//			double lift = it.getValue() * support;
			
			//Calculate score based on lift or confidence
			double score =  confidence; //Use confidence or lift, depending on Parameter.firstVote
						
			sd.put(it.getKey(), score);
		}
		
		//Filling a sequence with the best |count| items
		Sequence seq = new Sequence(-1);
		//List<Integer> bestItems = sd.getBest(1.002);
		List<Integer> bestItems = sd.getBest(0);
		if(bestItems != null && bestItems.size() > 0) {
			for(int i = 0; i < count && i < bestItems.size(); i++) {
				seq.addItem(new Item(bestItems.get(i)));
			}
		}

		//if (seq.size() == 0 && table.size() > 0) System.out.println("WHY??????");

		return seq;
	}

}
