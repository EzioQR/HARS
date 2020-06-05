package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.filter.Filter;
import p_heu.entity.pattern.Pattern;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.SequenceProduceListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test {

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
    public static BasicPatternFindingListener CreateBaselines(String SearchStrategy,String testFileName,Sequence correctsequence)
    {
        try {
            writeTxtFile(SearchStrategy,new File("G:\\methodname.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search."+SearchStrategy,
                testFileName};
        Config config = new Config(str);
        Filter filter = Filter.createFilePathFilter();

        Sequence corsequence=correctsequence.copy();
        HashSet<Sequence> corseqs=new HashSet<>();
        corseqs.add(corsequence);

        BasicPatternFindingListener listener = new BasicPatternFindingListener(corseqs);
        listener.setPositionFilter(filter);
        JPF jpf = new JPF(config);
        jpf.addListener(listener);
        jpf.run();
        jpf = null;

        System.gc();
        return listener;
    }
    public static void main(String[] args) {

        String testFileName="lamport.lamport";
        List<Double> prunetimes=new ArrayList<>();
        int iteration=100;
        for (int i = 0; i <iteration; i++) {

            Sequence correctSeq=getCorrectSequence(testFileName);
            Set<Sequence> correctSeqs = new HashSet<>();
            correctSeqs.add(correctSeq);
            if (correctSeq.getResult()==true) {
                BasicPatternFindingListener listener1 = CreateBaselines("PatternDistanceBasedSearch", testFileName, correctSeq);
                prunetimes.add(listener1.getPrunetimes());
            }
        }
        for (int i = 0; i < prunetimes.size(); i++) {
            System.out.println(prunetimes.get(i));
        }
        //        Sequence failTrace=listener1.getErrorSequence();
//        Set<Pattern> errorpatterns=failTrace.getPatterns();
//        System.out.println("errorpatternSize="+errorpatterns.size());
//        for (Pattern p:errorpatterns)
//        {
//            System.out.println(p.toString());
//        }
    }

    public static void writeTxtFile(String content,File fileName)throws Exception{

        FileOutputStream fileOutputStream=null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(content.getBytes("gbk"));
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
