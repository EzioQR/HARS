package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.SequenceProduceListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompareTogetherCalculateRunNum {
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

    public static String CreateBaselines(String SearchStrategy,String testFileName,Sequence correctsequence)
    {
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

        int sorttime=listener.getSortedtime();
        //int ruleworktime=listener.getRuleworktime();
        int p=listener.getCorrectSeqs().size()+1;

        return 0+","+sorttime+","+p;
    }

    public static void main(String[] args) throws IOException, IOException {

        String testFileName="replicatedcasestudies.Generic";
        String path="G:\\";

        Sequence correctSeq = null;
        int randomTime = 0;
        Set<Sequence> correctSeqs = null;
        int iteration = 500;
        int HEUNUM1 = 0,HEUNUM2 = 0,HEUNUM3 = 0,HEUNUM4=0,HEUNUM5 = 0;
        long HEUNUM1time = 0,HEUNUM2time = 0,HEUNUM3time = 0,HEUNUM4time=0,HEUNUM5time = 0;
        int RANDOM = 0;

        long StartTime = 0;
        long EndTime = 0;

        String path1=path+testFileName + "_" + iteration + ".csv";
        String path2=path+testFileName + "_" + iteration + "_workrate.csv";
        String path3=path+"iteration.txt";
        String path4=path+testFileName + "_" + iteration + "_time.csv";

        File file1 = new File(path1);
        if(!file1.exists()){
            file1.getParentFile().mkdirs();
        }
        file1.createNewFile();

        File file2 = new File(path2);
        if(!file2.exists()){
            file2.getParentFile().mkdirs();
        }
        file2.createNewFile();

        File file3 = new File(path3);
        if(!file3.exists()){
            file3.getParentFile().mkdirs();
        }
        file3.createNewFile();

        File file4 = new File(path4);
        if(!file4.exists()){
            file4.getParentFile().mkdirs();
        }
        file4.createNewFile();


        String columns="ARS,rule1,rule2,rule3,rule4";

        FileWriter fw1 = new FileWriter(file1, true);
        BufferedWriter bw1 = new BufferedWriter(fw1);
        bw1.write(columns+","+"Random");
        bw1.newLine();
        bw1.flush();

        FileWriter fw2 = new FileWriter(file2, true);
        BufferedWriter bw2 = new BufferedWriter(fw2);
        bw2.write(columns);
        bw2.newLine();
        bw2.flush();

        FileWriter fw4 = new FileWriter(file4, true);
        BufferedWriter bw4 = new BufferedWriter(fw4);
        bw4.write(columns+","+"Random");
        bw4.newLine();
        bw4.flush();

        int rule1worktime=0,rule2worktime=0,rule3worktime=0,rule4worktime=0,rule5worktime=0;
        int sorttime1=0,sorttime2=0,sorttime3=0,sorttime4=0,sorttime5=0;

        List<Double> arstime=new ArrayList<>();
        List<Double> rule1time=new ArrayList<>();
        List<Double> rule2time=new ArrayList<>();
        List<Double> rule3time=new ArrayList<>();
        List<Double> rule4time=new ArrayList<>();

        List<Double> arsreuslt=new ArrayList<>();
        List<Double> rule1reuslt=new ArrayList<>();
        List<Double> rule2reuslt=new ArrayList<>();
        List<Double> rule3reuslt=new ArrayList<>();
        List<Double> rule4reuslt=new ArrayList<>();

        List<Double> workrate1=new ArrayList<>();
        List<Double> workrate2=new ArrayList<>();
        List<Double> workrate3=new ArrayList<>();
        List<Double> workrate4=new ArrayList<>();
        List<Double> workrate5=new ArrayList<>();

        FileWriter fw3 = new FileWriter(file3, true);
        BufferedWriter bw3 = new BufferedWriter(fw3);

        for(int i = 1;i<=iteration;i++){

            System.out.println("--------------current iteration: " + i);

            bw3.write("--------------current iteration: " + i);
            bw3.newLine();
            bw3.flush();

            StartTime=System.currentTimeMillis();
            correctSeq = getCorrectSequence(testFileName);
            EndTime=System.currentTimeMillis();
            long firsttime=EndTime-StartTime;

            if(correctSeq.getResult() == true){
                StartTime=System.currentTimeMillis();
                String []RunResult1=CreateBaselines("PatternDistanceBasedSearch",testFileName,correctSeq).split(",");
                EndTime=System.currentTimeMillis();
                long baseline1time=EndTime-StartTime;

                StartTime=System.currentTimeMillis();
                String []RunResult2=CreateBaselines("ThreadSwitchTimesBasedSearch",testFileName,correctSeq).split(",");
                EndTime=System.currentTimeMillis();
                long baseline2time=EndTime-StartTime;

                StartTime=System.currentTimeMillis();
                String []RunResult3=CreateBaselines("WriteTimeBasedSearch",testFileName,correctSeq).split(",");
                EndTime=System.currentTimeMillis();
                long baseline3time=EndTime-StartTime;

                StartTime=System.currentTimeMillis();
                String []RunResult4=CreateBaselines("RWSwitchedBasedSearch",testFileName,correctSeq).split(",");
                EndTime=System.currentTimeMillis();
                long baseline4time=EndTime-StartTime;

                StartTime=System.currentTimeMillis();
                String []RunResult5=CreateBaselines("SharedVariableNumberBasedSearch",testFileName,correctSeq).split(",");
                EndTime=System.currentTimeMillis();
                long baseline5time=EndTime-StartTime;


                HEUNUM1time=HEUNUM1time+baseline1time+firsttime;
                HEUNUM2time=HEUNUM2time+baseline2time+firsttime;
                HEUNUM3time=HEUNUM3time+baseline3time+firsttime;
                HEUNUM4time=HEUNUM4time+baseline4time+firsttime;
                HEUNUM5time=HEUNUM5time+baseline5time+firsttime;

                HEUNUM1 +=Integer.parseInt(RunResult1[2]);
                HEUNUM2 +=Integer.parseInt(RunResult2[2]);
                HEUNUM3 +=Integer.parseInt(RunResult3[2]);
                HEUNUM4 +=Integer.parseInt(RunResult4[2]);
                HEUNUM5 +=Integer.parseInt(RunResult5[2]);

                rule1worktime+=Integer.parseInt(RunResult1[0]);
                rule2worktime+=Integer.parseInt(RunResult2[0]);
                rule3worktime+=Integer.parseInt(RunResult3[0]);
                rule4worktime+=Integer.parseInt(RunResult4[0]);
                rule5worktime+=Integer.parseInt(RunResult5[0]);


                sorttime1+=Integer.parseInt(RunResult1[1]);
                sorttime2+=Integer.parseInt(RunResult2[1]);
                sorttime3+=Integer.parseInt(RunResult3[1]);
                sorttime4+=Integer.parseInt(RunResult4[1]);
                sorttime5+=Integer.parseInt(RunResult5[1]);

                System.gc();


            }else{
                HEUNUM1time=HEUNUM1time+firsttime;
                HEUNUM2time=HEUNUM2time+firsttime;
                HEUNUM3time=HEUNUM3time+firsttime;
                HEUNUM4time=HEUNUM4time+firsttime;
                HEUNUM5time=HEUNUM5time+firsttime;

                HEUNUM1 += 1;
                HEUNUM2 += 1;
                HEUNUM3 += 1;
                HEUNUM4 += 1;
                HEUNUM5 += 1;

                System.gc();
            }
            if (i%100==0)
            {
                arsreuslt.add((HEUNUM1/100.0));
                rule1reuslt.add((HEUNUM2/100.0));
                rule2reuslt.add((HEUNUM3/100.0));
                rule3reuslt.add((HEUNUM4/100.0));
                rule4reuslt.add((HEUNUM5/100.0));

                arstime.add(HEUNUM1time/100.0);
                rule1time.add(HEUNUM2time/100.0);
                rule2time.add(HEUNUM3time/100.0);
                rule3time.add(HEUNUM4time/100.0);
                rule4time.add(HEUNUM5time/100.0);

                workrate1.add((rule1worktime*1.0/sorttime1));
                workrate2.add((rule2worktime*1.0/sorttime2));
                workrate3.add((rule3worktime*1.0/sorttime3));
                workrate4.add((rule4worktime*1.0/sorttime4));
                workrate5.add((rule5worktime*1.0/sorttime5));

                HEUNUM1=0;HEUNUM2=0;HEUNUM3=0;HEUNUM4=0;HEUNUM5=0;
                rule1worktime=0;rule2worktime=0;rule3worktime=0;rule4worktime=0;rule5worktime=0;
                sorttime1=0;sorttime2=0;sorttime3=0;sorttime4=0;sorttime5=0;
                HEUNUM1time= 0;HEUNUM2time = 0;HEUNUM3time = 0;HEUNUM4time=0;HEUNUM5time = 0;
            }

        }

        bw3.write("开始执行随机算法");
        bw3.newLine();
        bw3.flush();

        List<Double> randomresult=new ArrayList<>();
        List<Double> randomAveragetime=new ArrayList<>();
        long randomcounttime=0;
        for(int i = 1;i<=iteration;i++){
            bw3.write("--------------current iteration: " + i);
            bw3.newLine();
            bw3.flush();
            randomTime = 1;
            StartTime = System.currentTimeMillis(); //get start time
            while(getCorrectSequence(testFileName).getResult()){
                randomTime++;
            }
            EndTime = System.currentTimeMillis();
            randomcounttime=randomcounttime+EndTime-StartTime;

            RANDOM += randomTime;
            if (i%100==0)
            {
                randomAveragetime.add(randomcounttime/100.0);
                randomresult.add(RANDOM*1.0/100);
                RANDOM=0;
                randomcounttime=0;
            }
        }

        for (int i = 0; i <arsreuslt.size(); i++) {
            System.out.println(arsreuslt.get(i)+","+rule1reuslt.get(i)+","+rule2reuslt.get(i)+","+rule3reuslt.get(i)+","+rule4reuslt.get(i)+","+randomresult.get(i));
            bw1.write(arsreuslt.get(i)+","+rule1reuslt.get(i)+","+rule2reuslt.get(i)+","+rule3reuslt.get(i)+","+rule4reuslt.get(i)+","+randomresult.get(i));
            bw1.newLine();
            bw1.flush();

            System.out.println(workrate1.get(i)+","+workrate2.get(i)+","+workrate3.get(i)+","+workrate4.get(i)+","+workrate5.get(i));
            bw2.write(workrate1.get(i)+","+workrate2.get(i)+","+workrate3.get(i)+","+workrate4.get(i)+","+workrate5.get(i));
            bw2.newLine();
            bw2.flush();

            bw4.write(arstime.get(i)+","+rule1time.get(i)+","+rule2time.get(i)+","+rule3time.get(i)+","+rule4time.get(i)+","+randomAveragetime.get(i));
            bw4.newLine();
            bw4.flush();

        }



        fw1.close();
        bw1.close();
        fw2.close();
        bw2.close();
        fw3.close();
        bw3.close();
        fw4.close();
        bw4.close();

    }
}
