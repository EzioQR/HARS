package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.SearchState;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.SequenceProduceListener;

import java.util.HashSet;
import java.util.Set;

public class RunOnce {

    public static void main(String[] args) {
        String testFileName = "store.StoreTest";
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.PatternDistanceBasedSearch",
                testFileName};
        Config config = new Config(str);
        Filter filter = Filter.createFilePathFilter();

        Sequence correctSeq = getCorrectSequence(testFileName);
        Set<Sequence> correctSeqs = new HashSet<>();
        correctSeqs.add(correctSeq);
        BasicPatternFindingListener listener = new BasicPatternFindingListener(correctSeqs);
        listener.setPositionFilter(filter);
        JPF jpf = new JPF(config);
        jpf.addListener(listener);
        jpf.run();
        System.out.println("searched seq: " + (listener.getCorrectSeqs().size() + 1));
        printTrace(listener);
    }

    public static Sequence getCorrectSequence(String testFileName){

        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.SingleExecutionSearch",
                testFileName};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener listener = new SequenceProduceListener();
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        jpf = null;
        System.gc();
        return listener.getSequence();
    }

    public static void printTrace(BasicPatternFindingListener listener) {
        for (Sequence seq : listener.getCorrectSeqs()) {
            System.out.print("pass\t[");
            for(SearchState state : seq.getStates()) {
                System.out.print(state.getStateId() + ", ");
            }
            System.out.println("]");
        }
        System.out.print("fail\t[");
        for (SearchState state : listener.getErrorSequence().getStates()) {
            System.out.print(state.getStateId() + ", ");
        }
        System.out.println("]");
    }
}
