package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.filter.Filter;
import p_heu.entity.pattern.Pattern;
import p_heu.entity.sequence.Sequence;
import p_heu.entity.sequence.SimpleSequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.BasicPatternFindingListener2;
import p_heu.listener.SequenceProduceListener;
import p_heu.listener.SequenceProduceListener2;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CaculateCoverage {

    public static BasicPatternFindingListener CreateBaselines(String SearchStrategy,String testFileName,Sequence correctsequence,int traceLimit)
    {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search."+SearchStrategy,
                testFileName};
        try {
            writeTxtFile(SearchStrategy,new File("G:\\expProject\\ars-programs-ofGaoYi\\methodname.txt"));
            writeTxtFile(testFileName,new File("G:\\expProject\\ars-programs-ofGaoYi\\program.txt"));
            writeTxtFile(""+traceLimit,new File("G:\\expProject\\ars-programs-ofGaoYi\\trace.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public static Set<Pattern> mergeNewPatterns(Set<Pattern> newPatterns,Set<Pattern>RandomCoverPatterns)
    {

        if (RandomCoverPatterns.isEmpty())
        {
            for(Pattern pattern:newPatterns)
            {
                Pattern temp=pattern.copy();
                RandomCoverPatterns.add(temp);
            }
            return RandomCoverPatterns;
        }

        for (Pattern newpattern:newPatterns)
        {
            boolean flag=false;
            for (Pattern alreadypattern:RandomCoverPatterns)
            {
                if (newpattern.isSameExecptThread(alreadypattern))
                {
                    flag=true;
                    break;
                }
            }
            if (flag)
                continue;
            else
            {
                Pattern temp=newpattern.copy();
                RandomCoverPatterns.add(temp);
            }
        }
        return RandomCoverPatterns;
    }

    public static String readTxtFile(File file){
        String result = "";
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"gbk");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
            while((s=br.readLine())!=null){
                result = result  + s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Sequence getCorrectSequence(String testFileName) {

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

    public static void randomSearch(Sequence correctsequence, String testFileName) {
        Sequence corsequence = correctsequence.copy();
        HashSet<Sequence> corseqs = new HashSet<>();
        corseqs.add(corsequence);

        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search.SingleExecutionSearch2",
                testFileName};
        Config config = new Config(str);
        JPF jpf = new JPF(config);
        SequenceProduceListener2 listener = new SequenceProduceListener2(corseqs);
        Filter filter = Filter.createFilePathFilter();
        listener.setPositionFilter(filter);

        jpf.addListener(listener);
        jpf.run();
        jpf = null;
        System.gc();
    }

    public static BasicPatternFindingListener2 CreateBaselines(String SearchStrategy, String testFileName, Sequence correctsequence, String filepath) {
        String[] str = new String[]{
                "+classpath=out/production/heu_search",
                "+search.class=p_heu.search." + SearchStrategy,
                testFileName};
        Config config = new Config(str);
        Filter filter = Filter.createFilePathFilter();

        Sequence corsequence = correctsequence.copy();
        HashSet<Sequence> corseqs = new HashSet<>();
        corseqs.add(corsequence);

        BasicPatternFindingListener2 listener = new BasicPatternFindingListener2(corseqs, filepath);
        listener.setPositionFilter(filter);
        JPF jpf = new JPF(config);
        jpf.addListener(listener);
        jpf.run();
        jpf = null;

        System.gc();
        
        return listener;
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


    public static boolean ChecknewSequence(List<SimpleSequence> lists, SimpleSequence newseq) {
        for (int i = 0; i <lists.size(); i++) {
            if (newseq.isSame(lists.get(i)))
            {
//                try {
//                    System.out.println("列表中第"+i+"条trace已经有了");
//                    SimpleSequence seq1=lists.get(i);
//                    for (String s1:seq1.getThreadlocation()) {
//                        System.out.println(s1);
//                    }
//                    System.out.println("*************************");
//                    SimpleSequence seq2=newseq;
//                    for (String s2:seq2.getThreadlocation()) {
//                        System.out.println(s2);
//                    }
//
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                return true;
            }
        }
        return false;
    }

    public static String run(String testFileName)throws IOException {

        String home_path="G:\\expProject\\ars-programs-ofGaoYi\\ErrorCountsBasedOnTrace\\random\\";

        Set patterns=new HashSet();

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Number");
        HSSFRow row = null;

        List<SimpleSequence> totalSeqs=new ArrayList<>();
        List<Integer> patternnumcounts=new ArrayList<>();

        int tracesnumber=0;
        int threshold=50;

        String path3="G:\\iteration.txt";
        File file3 = new File(path3);
        if(!file3.exists()){
            file3.getParentFile().mkdirs();
        }
        file3.createNewFile();

        FileWriter fw3 = new FileWriter(file3, true);
        BufferedWriter bw3 = new BufferedWriter(fw3);

        bw3.write(testFileName);
        bw3.newLine();
        bw3.flush();

        int errors=0;
        int corrects=0;

        long currenttime1=System.currentTimeMillis();
        long currenttime2=System.currentTimeMillis();
        long runtime=currenttime2-currenttime1;

        List<Long> times=new ArrayList<>();

        while(tracesnumber<=threshold) {

            Sequence resultTrace = getCorrectSequence(testFileName);
            tracesnumber++;

            Set<Pattern> newpatterns=resultTrace.getPatterns();
            patterns=mergeNewPatterns(newpatterns,patterns);
            patternnumcounts.add(patterns.size());

            if (resultTrace.getResult()) {
                SimpleSequence seq1=new SimpleSequence();
                seq1.ConvertSequenceToSimpleSequence(resultTrace);
                corrects++;
                totalSeqs.add(seq1.copy());
                currenttime2=System.currentTimeMillis();
                runtime=currenttime2-currenttime1;
                times.add(runtime);
            }
            else {
                SimpleSequence seq1=new SimpleSequence();
                seq1.ConvertSequenceToSimpleSequence(resultTrace);
                errors++;
//                if (!ChecknewSequence(totalSeqs,seq1))
//                {
                    totalSeqs.add(seq1.copy());
                    currenttime2=System.currentTimeMillis();
                    runtime=currenttime2-currenttime1;
                    times.add(runtime);
 //               }
            }

            bw3.write("--------------current iteration: " + tracesnumber);
            bw3.newLine();
            bw3.flush();

            System.gc();

        }


        corrects=0;errors=0;
        for (int i = 0; i < totalSeqs.size(); i++) {

            if (totalSeqs.get(i).getResult())
                corrects++;
            else
                errors++;
            row = sheet.createRow(i);
            row.createCell(0).setCellValue(testFileName);
            row.createCell(1).setCellValue(corrects);
            row.createCell(2).setCellValue(errors);
            row.createCell(3).setCellValue(totalSeqs.get(i).getResult());
            row.createCell(4).setCellValue(patternnumcounts.get(i));
        }

        FileOutputStream fos = new FileOutputStream(home_path+testFileName+ ".xls");
        workbook.write(fos);
        fos.close();
        fw3.close();
        bw3.close();
        file3.delete();

//        String path1="G:\\patterns.csv";
//        File file1 = new File(path1);
//        if(!file1.exists()){
//            file1.getParentFile().mkdirs();
//        }
//        file1.createNewFile();
//
//        FileWriter fw1 = new FileWriter(file1, true);
//        BufferedWriter bw1 = new BufferedWriter(fw1);
//        bw1.write("Random"+","+testFileName+","+patterns.size());
//        bw1.newLine();
//        bw1.flush();
//
//        fw1.close();
//        bw1.close();

        return corrects+","+errors;
    }

    public static String run1(String testFileName,String searchStrategy)throws IOException {

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Number");
        HSSFRow row = null;

        Set<Pattern> patterns=new HashSet<>();
        List<Integer> patternnumcounts=new ArrayList<>();

        String home_path="G:\\expProject\\ars-programs-ofGaoYi\\ErrorCountsBasedOnTrace\\"+searchStrategy+"\\";
        int traceLimit=50;
        int traces=0;
        List<Long> times=new ArrayList<>();
        List<SimpleSequence> totalsequences=new ArrayList<>();

        long StartTime;
        long EndTime;
        long runtime;


//        String path3="G:\\iteration.txt";
//        File file3 = new File(path3);
//        if(!file3.exists()){
//            file3.getParentFile().mkdirs();
//        }
//        file3.createNewFile();
//
//        FileWriter fw3 = new FileWriter(file3, true);
//        BufferedWriter bw3 = new BufferedWriter(fw3);
//
//        bw3.write(testFileName);
//        bw3.newLine();
//        bw3.flush();

        int count=0;
//        while(traces<=traceLimit)
//        {
            StartTime = System.currentTimeMillis();
            Sequence correctSeq = getCorrectSequence(testFileName);


 //           if(correctSeq.getResult() == true) {

                StartTime = System.currentTimeMillis();
                //BasicPatternFindingListener listener1 = CreateBaselines(searchStrategy, testFileName, correctSeq);//="PatternDistanceBasedSearch"
                BasicPatternFindingListener listener1 = CreateBaselines(searchStrategy, testFileName, correctSeq,traceLimit);
                EndTime = System.currentTimeMillis();
                runtime= EndTime - StartTime;

//                Set<Sequence> resultcorrectseqs=listener1.getCorrectSeqs();
//                Sequence resulterrorseq=listener1.getErrorSequence();

//                for (Sequence seq:resultcorrectseqs)
//                {
//                    SimpleSequence seqnew=new SimpleSequence();
//                    seqnew.ConvertSequenceToSimpleSequence(seq.copy());
//                    totalsequences.add(seqnew.copy());
//                    times.add(runtime);

//                    //记录pattern
//                    if (count<=traceLimit)
//                    {
//                        Set newpatterns=seq.getPatterns();
//                        patterns=mergeNewPatterns(newpatterns,patterns);
//                        patternnumcounts.add(patterns.size());
//                        count++;
//                    }
//                }

//                //记录pattern
//                if (count<=traceLimit)
//                {
//                    Set newpatterns=resulterrorseq.getPatterns();
//                    patterns=mergeNewPatterns(newpatterns,patterns);
//                    patternnumcounts.add(patterns.size());
//                    count++;
//                }

//                SimpleSequence seqnew=new SimpleSequence();
//                seqnew.ConvertSequenceToSimpleSequence(resulterrorseq.copy());
//
////                if (!ChecknewSequence(totalsequences,seqnew))
////                {
//                    totalsequences.add(seqnew.copy());
//                    times.add(runtime);
////                }
//
//
//                traces+=(resultcorrectseqs.size()+1);


//                StartTime = System.currentTimeMillis();
//                BasicPatternFindingListener listener2 = CreateBaselines("ThreadSwitchTimesBasedSearch", testFileName, correctSeq);
//                EndTime = System.currentTimeMillis();
//                long baseline2time = EndTime - StartTime;
//
//                StartTime = System.currentTimeMillis();
//                BasicPatternFindingListener listener3 = CreateBaselines("WriteTimeBasedSearch", testFileName, correctSeq);
//                EndTime = System.currentTimeMillis();
//                long baseline3time = EndTime - StartTime;
//
//                StartTime = System.currentTimeMillis();
//                BasicPatternFindingListener listener4 = CreateBaselines("RWSwitchedBasedSearch", testFileName, correctSeq);
//                EndTime = System.currentTimeMillis();
//                long baseline4time = EndTime - StartTime;
//
//                StartTime = System.currentTimeMillis();
//                BasicPatternFindingListener listener5 = CreateBaselines("SharedVariableNumberBasedSearch", testFileName, correctSeq);
//                EndTime = System.currentTimeMillis();
//                long baseline5time = EndTime - StartTime;
//            }
//            else{
//
//                //记录pattern
//                if (count<=traceLimit)
//                {
//                    Set newpatterns=correctSeq.getPatterns();
//                    patterns=mergeNewPatterns(newpatterns,patterns);
//                    patternnumcounts.add(patterns.size());
//                    count++;
//                }
//
//                SimpleSequence seqnew=new SimpleSequence();
//                seqnew.ConvertSequenceToSimpleSequence(correctSeq);
//
////                if (!ChecknewSequence(totalsequences,seqnew))
////                {
//                    totalsequences.add(seqnew.copy());
////                }
//
//                totalsequences.add(seqnew.copy());
//                traces++;


                System.gc();
//            }

//            bw3.write("--------------current iteration: " + traces);
//            bw3.newLine();
//            bw3.flush();
//        }

//        while(totalsequences.size()>traceLimit){
//            totalsequences.remove(totalsequences.size()-1);
//        }

//        int corrects=0;int errors=0;
//        for (int i = 0; i < totalsequences.size(); i++) {
//
//            if (totalsequences.get(i).getResult())
//                corrects++;
//            else
//                errors++;
//            row = sheet.createRow(i);
//            row.createCell(0).setCellValue(testFileName);
//            row.createCell(1).setCellValue(corrects);
//            row.createCell(2).setCellValue(errors);
//            row.createCell(3).setCellValue(totalsequences.get(i).getResult());
//            row.createCell(4).setCellValue(patternnumcounts.get(i));
//
//        }
//
//        FileOutputStream fos = new FileOutputStream(home_path+testFileName+ ".xls");
//        workbook.write(fos);
//        fos.close();
//        fw3.close();
//        bw3.close();
//        file3.delete();

        new File("G:\\expProject\\ars-programs-ofGaoYi\\methodname.txt").delete();
        new File("G:\\expProject\\ars-programs-ofGaoYi\\program.txt").delete();
        new File("G:\\expProject\\ars-programs-ofGaoYi\\trace.txt").delete();

//        String path1="G:\\patterns.csv";
//        File file1 = new File(path1);
//        if(!file1.exists()){
//            file1.getParentFile().mkdirs();
//        }
//        file1.createNewFile();
//
//        FileWriter fw1 = new FileWriter(file1, true);
//        BufferedWriter bw1 = new BufferedWriter(fw1);
//        bw1.write(searchStrategy+","+testFileName+","+patterns.size());
//        bw1.newLine();
//        bw1.flush();
//
//        fw1.close();
//        bw1.close();


        return "";

    }

    public static void main(String[] args) throws IOException {
//        随机算法
////        HSSFWorkbook workbook = new HSSFWorkbook();
////        HSSFSheet sheet = workbook.createSheet("total");
////        HSSFRow row = null;
//
//        File file=new File("G:\\expProject\\ars-programs-ofGaoYi\\Coverage");
//        String [] fileName = file.list();
//        for (int i = 0; i <fileName.length ; i++) {
//            String testFileName=fileName[i];
//            System.out.println(testFileName);
//            String result=run(testFileName);
//            String []results=result.split(",");
//
////            row = sheet.createRow(i);
////            row.createCell(0).setCellValue(testFileName);
////            row.createCell(1).setCellValue(results[0]);
////            row.createCell(2).setCellValue(results[1]);
//        }

//        FileOutputStream fos = new FileOutputStream("G:\\expProject\\ars-programs-ofGaoYi\\total.xls");
//        workbook.write(fos);

//        HSSFWorkbook workbook = new HSSFWorkbook();
//        HSSFSheet sheet = workbook.createSheet("totalARS");
//        HSSFRow row = null;

        File file = new File("G:\\expProject\\ars-programs-ofGaoYi\\Coverage");
        String searchstrategy = "ThreadSwitchTimesBasedSearch";
        String[] fileName = file.list();

        for (int i = 0; i < fileName.length; i++) {
            String testFileName = fileName[i];
            System.out.println(testFileName);
            run1(testFileName, searchstrategy);
            System.gc();
//            row = sheet.createRow(i);
//            row.createCell(0).setCellValue(testFileName);
//            row.createCell(1).setCellValue(results[0]);
//            row.createCell(2).setCellValue(results[1]);
        }

//        FileOutputStream fos = new FileOutputStream("G:\\expProject\\ars-programs-ofGaoYi\\"+searchstrategy+"count.xls");
//        workbook.write(fos);
    }
}
