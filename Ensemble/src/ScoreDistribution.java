import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ScoreDistribution<K> {


    private TreeMap<Integer, List<K>> dict;


    public ScoreDistribution() {
        dict = new TreeMap<Integer, List<K>>();
    }

    public void put(K key, Integer value) {

        List<K> keys = dict.get(value);
        if(keys == null) {
            keys = new ArrayList<K>();
        }

        keys.add(key);

        dict.put(value, keys);
    }

    public void clear() {
        dict.clear();
    }

    public List<K> getBestValue() {

        if(dict.size() == 0) {
            return null;
        }
        else if(dict.size() == 1) {
            return dict.lastEntry().getValue();
        }

        Integer bestVal = dict.lastKey(); //best value in the dictionary

        return dict.get(bestVal);
    }

    public List<K> getNextBestValue(Integer best) {

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