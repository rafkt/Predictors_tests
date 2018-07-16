package ca.ipredict.helpers;

import java.util.ArrayList;
import java.util.List;
import ca.ipredict.database.Item;

public class SequenceVariations{


	public static ArrayList<List<Item>> getPairPermutations(List<Item> target){

		ArrayList<List<Item>> permuted_sequences = new ArrayList<List<Item>>();	

		//permuted_sequences.add(target);	

		for (int i = 0; i < target.size() - 1; i++){
			ArrayList<Item> seq = new ArrayList<Item>();

			for (int j = 0; j < i; j++)	seq.add(target.get(j));

			seq.add(target.get(i + 1));
			seq.add(target.get(i));
			for (int j = i + 2; j < target.size(); j++)seq.add(target.get(j));


			permuted_sequences.add(seq);			
			//System.out.println(seq);
		}

		return permuted_sequences;
	}

	public static ArrayList<List<Item>> getOneDeletionAnywhere(List<Item> target){
		ArrayList<List<Item>> deletion_in_sequences = new ArrayList<List<Item>>();	

		//deletion_in_sequences.add(target);	

		for (int i = 1; i < target.size(); i++){
			ArrayList<Item> seq = new ArrayList<Item>();

			for (int j = 0; j < i; j++)	seq.add(target.get(j));

			for (int j = i + 1; j < target.size(); j++)seq.add(target.get(j));


			deletion_in_sequences.add(seq);			
			//System.out.println(seq);
		}

		return deletion_in_sequences;
	}

	public static void main(String...args){

		//SequenceVariations sv = new SequenceVariations();

		ArrayList<Item> seq = new ArrayList<Item>();

		seq.add(new Item(1));
		seq.add(new Item(2));
		seq.add(new Item(3));
		seq.add(new Item(4));
		seq.add(new Item(5));
		seq.add(new Item(6));


		for (List<Item> i : SequenceVariations.getOneDeletionAnywhere(seq)) System.out.println(i);
	}
}