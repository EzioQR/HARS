package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.entity.pattern.Pattern;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.SequenceProduceListener;

import java.util.Set;

public class ProduceSequenceRandomly {
    public static void main(String[] args) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
				"+search.class=p_heu.search.SingleExecutionSearch",
                "CheckField"};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        Sequence seq = listener.getSequence();
        System.out.println(seq.getStates().size());
        Set<Pattern> patterns = seq.getPatterns();
//        if (seq.getResult() == false) {
//            System.out.println(patterns);
//        }
    }
}