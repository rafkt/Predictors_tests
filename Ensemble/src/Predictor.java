import java.util.LinkedList;
import java.util.List;

public class Predictor {

    private String Tag;
    private List<Sequence> Answers;

    public Predictor(String tag, List<Sequence> answers){
        this.Answers = new LinkedList<Sequence>(answers);
        this.Tag = tag.equals(EnsemblePredictor.Predictors.CPTPlus.toString()) ? "CPT+" : tag;
    }

    public List<Sequence> GetAnswers() {
        return Answers;
    }

    public String GetTag() {
        return Tag;
    }

    public Sequence GetAnswerForIndex(int index){
        return Answers.get(index);
    }

}
