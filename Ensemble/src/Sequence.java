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

    public List<Integer> getItems() {
        return Items;
    }

    public boolean IsValid(){
        if (Items.size() > 0 && Items.get(0) != -1) return true;
        return false;
    }
}
