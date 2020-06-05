package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.SequenceProduceListener;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class CompareARSandImproved {

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

    public static void main(String[] args) throws IOException {

//		String[] testFileNames = {"consisitency.Main","critical.Critical","datarace.Main",
//				"hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort"};org.apache.log4j.TestThrowableStrRep
        File file=new File("G:\\expProject\\ars-programs-ofGaoYi\\Coverage");
        //String [] testFileNames = file.list();//"account.Main","airline.Main",
         String[] testFileNames = {"airline.Main"};
        for(String testFileName : testFileNames){
            Run(testFileName);
            new File("G:\\iteration.txt").delete();
        }
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

    public static void Run(String testFileName) throws IOException {


        for (int ii = 0; ii <1; ii++) {

        Sequence correctSeq = null;
        int randomTime = 0;
        Set<Sequence> correctSeqs = null;
        //String testFileName = "SimpleTest.Main";//"CheckField"
        int iteration =5;
        int HEUNUM1 = 0;
        int HEUNUM2 = 0;
        double ars= 0;

        int firstworktime=0;
        int rule1worktime=0;
        int rule2worktime=0;
        int rule4worktime=0;
        int rule1randomtime=0;
        int rule2randomtime=0;
        int rule4randomtime=0;

        long StartTime1 = 0;
        long EndTime1 = 0;
        long runtime1 = 0;
        long StartTime2= 0;
        long EndTime2 = 0;
        long runtime2 = 0;
        long runtime1avg= 0;
        long runtime2avg= 0;


        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Number");
        HSSFRow row = null;

        String path="G:\\";
        String path3=path+"iteration.txt";
        File file3 = new File(path3);
        if(!file3.exists()){
            file3.getParentFile().mkdirs();
        }
        FileWriter fw3 = new FileWriter(file3, true);
        BufferedWriter bw3 = new BufferedWriter(fw3);
        bw3.write(testFileName);
        bw3.newLine();
        bw3.flush();


        String path4=path+"ruleworktime\\"+testFileName + "_" + iteration +"iteration.csv";
        File file4 = new File(path4);
        if(!file4.exists()){
            file4.getParentFile().mkdirs();
        }
        FileWriter fw4 = new FileWriter(file4, true);
        BufferedWriter bw4 = new BufferedWriter(fw4);
        bw4.write("PatternWork"+","+"rule1worktime"+","+"rule2worktime"+","+"rule4worktime"+","+"rule1randomtime"+","+"rule2randomtime"+","+"rule4randomtime");
        bw4.newLine();
        bw4.flush();

        row = sheet.createRow(0);
        row.createCell(0).setCellValue("Index");
        row.createCell(1).setCellValue("ARS1");
        row.createCell(2).setCellValue("ARS2");


            int QueueN1=5;
            int QueueN2=5;
            try {
                writeTxtFile(""+QueueN1,new File("G:\\QueueNumber1.txt"));
                writeTxtFile(""+QueueN2,new File("G:\\QueueNumber2.txt"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        for(int i = 1;i<=iteration;i++){

            row = sheet.createRow(i);
            long firststarttime=System.currentTimeMillis();
            correctSeq = getCorrectSequence(testFileName);
            long firstendtime=System.currentTimeMillis();
            long firstruntime=firstendtime-firststarttime;

            if(correctSeq.getResult() == true){
                ars+=1;
                correctSeqs = new HashSet<>();
                correctSeqs.add(correctSeq);

                StartTime1=System.currentTimeMillis();
                BasicPatternFindingListener listener1=CreateBaselines("PatternDistanceBasedSearch",testFileName,correctSeq);
                EndTime1=System.currentTimeMillis();
                runtime1=EndTime1-StartTime1;


                StartTime2=System.currentTimeMillis();
                BasicPatternFindingListener listener2=CreateBaselines("IntelligentBasedSearch",testFileName,correctSeq);
                EndTime2=System.currentTimeMillis();
                runtime2=EndTime2-StartTime2;


                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(listener1.getCorrectSeqs().size()+1);
                row.createCell(2).setCellValue(listener2.getCorrectSeqs().size()+1);
                row.createCell(3).setCellValue(firstruntime+runtime1);
                row.createCell(4).setCellValue(firstruntime+runtime2);

                bw4.write(listener2.getFirstworktime()
                            +","+listener2.getRule1worktime()
                            +","+listener2.getRule2worktime()
                            +","+listener2.getRule4worktime()
                            +","+listener2.getRule1randomtime()
                            +","+listener2.getRule2randomtime()
                            +","+listener2.getRule4randomtime());
                bw4.flush();
                bw4.newLine();

                HEUNUM1 += (listener1.getCorrectSeqs().size()+1);
                HEUNUM2 += (listener2.getCorrectSeqs().size()+1);
                runtime1avg+=firstruntime+runtime1;
                runtime2avg+=firstruntime+runtime2;

                firstworktime+=listener2.getFirstworktime();
                rule1worktime+=listener2.getRule1worktime();
                rule2worktime+=listener2.getRule2worktime();
     //           rule3worktime+=listener2.getRule3worktime();
                rule4worktime+=listener2.getRule4worktime();
                rule1worktime+=listener2.getRule1worktime();
                rule2worktime+=listener2.getRule2worktime();
                rule4worktime+=listener2.getRule4worktime();

                int P1=listener1.getCorrectSeqs().size()+1;
                int P2=listener2.getCorrectSeqs().size()+1;
                System.out.println("--------------current iteration: " + i+"   P1="+P1+" P2="+P2);
                bw3.write("--------------current iteration: " + i+"   P1="+P1+" P2="+P2);
                bw3.newLine();
                bw3.flush();

                System.gc();

            }else{
                row.createCell(0).setCellValue(i);
                row.createCell(1).setCellValue(1);
                row.createCell(2).setCellValue(1);
                row.createCell(3).setCellValue(firstruntime);
                row.createCell(4).setCellValue(firstruntime);
                HEUNUM1 += 1;
                HEUNUM2 += 1;
                runtime1avg+=firstruntime;
                runtime2avg+=firstruntime;

                System.out.println("--------------current iteration: " + i+"   P1="+1+" P2="+1);
                bw3.write("--------------current iteration: " + i+"   P1="+1+" P2="+1);
                bw3.newLine();
                bw3.flush();

                System.gc();
            }
        }


        bw3.close();
        fw3.close();
        row = sheet.createRow(101);
        row.createCell(0).setCellValue("average:");
        row.createCell(1).setCellValue(HEUNUM1*1.0/iteration);
		row.createCell(2).setCellValue(HEUNUM2*1.0/iteration);
        row.createCell(3).setCellValue(runtime1avg*1.0/iteration);
        row.createCell(4).setCellValue(runtime2avg*1.0/iteration);
        System.out.println("ARS1="+HEUNUM1/100.0);
        System.out.println("ARS2="+HEUNUM2/100.0);
        if(HEUNUM1>=HEUNUM2)
        {
            FileOutputStream fos = new FileOutputStream("G:\\" + testFileName + "_" + iteration + ".xls");
            workbook.write(fos);
            fos.close();
            break;
        }

//        System.out.println("arstime"+ars);
//        System.out.println("pattern"+firstworktime);
//        System.out.println("rule1="+rule1worktime);
//        System.out.println("rule2="+rule2worktime);
//        System.out.println("rule4="+rule4worktime);

        bw4.close();
        fw4.close();
        }
    }
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
                return deleteFile(fileName);
        }
    }
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }


}
