package ca.ipredict.predictor;

import ca.ipredict.database.Item;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.lang.Math;



public class ConseqEntropy{

	private TreeMap<Integer, Integer> consequentsItemsFreq;
	private int length;
	private float minEntropy;

	public ConseqEntropy(ConseqEntropy other){ 
		this.minEntropy = other.minEntropy; 
		this.length = other.length;
		this.consequentsItemsFreq = new TreeMap<Integer, Integer>(other.consequentsItemsFreq);
	}

	public ConseqEntropy(){ 
		minEntropy = 100000; 
		length = 0;
		consequentsItemsFreq = new TreeMap<Integer, Integer>();
	}

	public void addConsequent(ArrayList<ArrayList<Integer>> conList){
		for (ArrayList<Integer> l : conList){
			Item[] items = new Item[l.size()];
			int counter = 0;
			for (Integer i : l){
				items[counter++] = new Item(i);
			}
			addConsequent(items);
		}
	}

	public void addConsequent(Item[] sequence){
		for (Item item : sequence){
			Integer oldVal = consequentsItemsFreq.get(item.val);
			if (oldVal == null) consequentsItemsFreq.put(item.val, 1);
			else consequentsItemsFreq.put(item.val, oldVal + 1);
		}
		length += sequence.length;
		updateEntropy();
		//System.out.println(minEntropy);
	}

	public boolean checkIfEntropyDrops(Item[] sequence){
		float newEntropy = calculateEntropy(consequentsItemsFreq, length, sequence);
		if (newEntropy <= minEntropy) return true;
		else return false;
	}

	public float getEntropy(){
		return minEntropy;
	}

	private void updateEntropy(){
		minEntropy = calculateEntropy(consequentsItemsFreq, length, null);
	}

	private float calculateEntropy(TreeMap<Integer, Integer> list, int l, Item[] seq){
		float  sum = 0;
		if (seq != null) {
			for (Item item : seq){
				Integer oldVal = list.get(item.val);
				if (oldVal == null) list.put(item.val, 1);
				else list.put(item.val, oldVal + 1);
			}
			l += seq.length;
		}
		for(Entry<Integer, Integer> it : list.entrySet()){
			sum += (it.getValue() / (float) l) * (Math.log((float)l / it.getValue()) / Math.log(2));
		}
		return sum;
	}

	public static void main(String[] args){

		Item[] items = new Item[5];
		ConseqEntropy cEn = new ConseqEntropy();
		//1 2 2 3 4;
		items[0] = new Item(new Integer(1));
		items[1] = new Item(new Integer(2));
		items[2] = new Item(new Integer(2));
		items[3] = new Item(new Integer(3));
		items[4] = new Item(new Integer(4));

		cEn.addConsequent(items);


		//2 2 2 2 4;
		items[0] = new Item(new Integer(2));
		items[1] = new Item(new Integer(2));
		items[2] = new Item(new Integer(2));
		items[3] = new Item(new Integer(2));
		items[4] = new Item(new Integer(4));

		cEn.addConsequent(items);

		//2 2 2 2 2;
		items[0] = new Item(new Integer(2));
		items[1] = new Item(new Integer(2));
		items[2] = new Item(new Integer(2));
		items[3] = new Item(new Integer(2));
		items[4] = new Item(new Integer(2));

		cEn.addConsequent(items);
	}
	
}