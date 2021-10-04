import org.omg.CORBA.INTERNAL;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreDistribution {


    private TreeMap<Integer, List<Integer>> dict;

    public Sequence ToSequence(){
        List<Integer> sequenceItems = new ArrayList<Integer>();
        TreeMap<Integer, List<Integer>> reversedDict = new TreeMap<Integer, List<Integer>>(Collections.reverseOrder());
        reversedDict.putAll(dict);
        for(Map.Entry<Integer, List<Integer>> it : reversedDict.entrySet()){
            sequenceItems.addAll((it.getValue()).stream().filter(v -> v != -1).collect(Collectors.toList()));
        }
        return new Sequence(sequenceItems);
    }

    public ScoreDistribution() {
        dict = new TreeMap<Integer, List<Integer>>();
    }

    public void put(Integer key, Integer value) {

        List<Integer> keys = dict.get(value);
        if(keys == null) {
            keys = new ArrayList<Integer>();
        }

        keys.add(key);

        dict.put(value, keys);
    }

    public void clear() {
        dict.clear();
    }

    public List<Integer> getBestValue() {

        if(dict.size() == 0) {
            return null;
        }
        else if(dict.size() == 1) {
            return dict.lastEntry().getValue();
        }

        Integer bestVal = dict.lastKey(); //best value in the dictionary

        return dict.get(bestVal);
    }

    public List<Integer> getNextBestValue(Integer best) {

        Integer nextBest = dict.lowerKey(best);
        if(nextBest == null) {
            return null;
        }

        return dict.get(nextBest);
    }

    public Integer getBestScore() {

        Integer nextBest = dict.lastEntry().getKey();
        if(nextBest == null) {
            return null;
        }

        return nextBest;
    }
}