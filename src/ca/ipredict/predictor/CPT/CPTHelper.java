package ca.ipredict.predictor.CPT;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.ipredict.database.Item;
import ca.ipredict.database.Sequence;

/**
 * Best friend of CPT
 */
public class CPTHelper {

	
	public static CPTPredictor predictor;
	
	/**
	 * Return the last Length items
	 * @param sequence the sequence to slice
	 * @param length the size of the subsequences
	 */
	public static Sequence keepLastItems(Sequence sequence, int length) { 

		if(sequence.size() <= length){ 
			return sequence;
		}
		
		//slicing the seqence
		Sequence result = new Sequence(sequence.getId(), sequence.getItems().subList(sequence.size() - length, sequence.size()));
		return result;
	}

	/**
	 * Return a bit vector representing the set of similar sequence of the specified sequence
	 * @param sequence The sequence to used to find similar sequences
	 * @param II The inverted index containing the bit vectors
	 * @return The similar sequences as a bit vector, where each bit indicate whether a sequence is similar or not
	 */
	public static Bitvector getSimilarSequencesIds(Item[] sequence) {
		if(sequence.length == 0) {
			return new Bitvector();
		}

		//for each item in the sequence; do the intersection of their bitset
		Bitvector intersection = null;
		for(int i = 0 ; i < sequence.length ; i++) {
			if(intersection == null) {

				intersection = (Bitvector) predictor.II.get(sequence[i].val).clone();
			}
			else {
				Bitvector other = predictor.II.get(sequence[i].val);
				if(other != null) {
					intersection.and(predictor.II.get(sequence[i].val));
				}
			}
		}
		
		return intersection;
	}
	
	/**
	 * Return a sequence in sequential order from the Prediction Tree given its unique id
	 * @param id Id of the sequence to extract
	 * @return The full sequence matching the id
	 */
	public static Item[] getSequenceFromId(Integer id) {
		
		List<Item> sequence = new ArrayList<Item>();
		PredictionTree curNode = predictor.LT.get(id);
		
		//Reading the whole branch from bottom to top
		sequence.add(curNode.Item);
		while(curNode.Parent != null && curNode.Parent != predictor.Root) {
			curNode = curNode.Parent;
			sequence.add(curNode.Item);
		}
		
		//Reversing the sequence so that the leaf item is last and 
		//the item closer to the root be first
		Collections.reverse(sequence);
		
		//Returning the sequence as an array
		return sequence.toArray(new Item[0]);
	}
	
	/**
	 * The method return all possible subsequences of size S where the order is not relevant. It guarantee
	 * not duplicates in the results
	 * @param prefix Should be set to empty, eg: new Item[0]
	 * @param alphabet All the possible items
	 * @param offset Should be set to 0
	 * @param S size of the subsequences to return
	 * @return A list of subsequences of size S wihtout duplicates
	 */
	public static List<Item[]> noiseRemover(Item[] prefix, Item[] alphabet, int offset, int S) {
	
		List<Item[]> results = new ArrayList<Item[]>();

		
		//for each possible value in the alphabet from alphabet[offset] to alphabet[alphabet.length - 1]
		while(offset < alphabet.length) {
			
			//creating a sequence of "size" containing the suffix
			List<Item> cur = new ArrayList<Item>();
			for(Item item : prefix) {
				cur.add(item);
			}
			
			//adding the current offset element in the alphabet
			cur.add(alphabet[offset]);
			
			//if the cur sequence is not big enough
			//do a recursive call to add more items
			if(cur.size() < S) {
				results.addAll(noiseRemover(cur.toArray(new Item[cur.size()]), alphabet, offset + 1, S));
			}
			//if the sequence is the right size, we add it to the result list
			else {
				results.add(cur.toArray(new Item[S]));
			}

			//shifting the offset by one
			offset++;
		}
		
		return results;
	}
	
	
	public static Sequence removeUnseenItems(Sequence seq) {
		
		Sequence target = new Sequence(seq);
		
		//Min support for items in the target sequence
		int treshold = 0;
		
		List<Item> selectedItems = new ArrayList<Item>();
		for(Item item : target.getItems()) {
			
			//Keep only the item that we have seen during training and that have a support 
			//above the specified threshold
			if(predictor.II.get(item.val) != null && predictor.II.get(item.val).cardinality() >= treshold) {
				selectedItems.add(item);
			}	
		}
		target.getItems().clear();
		target.getItems().addAll(selectedItems);
		
		return target;
	}
	
	public static void main(String...args) {
		
		
		Item[] alphabet = new Item[5];
		alphabet[0] = new Item(1);
		alphabet[1] = new Item(2);
		alphabet[2] = new Item(3);
		alphabet[3] = new Item(4);
		alphabet[4] = new Item(5);
		
		
		List<Item[]> results = noiseRemover(new Item[0], alphabet, 0, 3);
	
		
		for(Item[] seq : results) {
			for(Item item : seq) {
				System.out.print(item.val + ", ");
			}
			System.out.println();
		}
	}
}