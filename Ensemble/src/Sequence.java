import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Sequence {
    private List<Integer> Items;

    public Sequence(){
        this.Items = new LinkedList<Integer>();
    }

    public Sequence (List<Integer> items){
        this.Items = new LinkedList<Integer>(items);
    }

    public List<Integer> GetItems() {
        return Items;
    }

    public List<Integer> GetItems(int n) {
        return new ArrayList(Items.subList(0, Items.size() > n ? n : Items.size()));
    }

    public boolean IsValid(){
        if (Items.size() > 0 && Items.get(0) != -1) return true;
        return false;
    }
}
