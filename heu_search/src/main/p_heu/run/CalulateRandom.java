package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.SequenceProduceListener;

import java.io.*;
import java.util.List;

public class CalulateRandom {

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

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Number");
        HSSFRow row = null;

        long randStartTime = 0;
        long randEndTime = 0;
        long RANDOMTIME = 0;

        int RANDOM=0;
        int randomTime = 0;

        int iteration =1;

        row = sheet.createRow(0);
        row.createCell(0).setCellValue("Index");
        row.createCell(1).setCellValue("P=");
        row.createCell(2).setCellValue("Time=");

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

        for(int i = 1;i<=iteration;i++){

            row = sheet.createRow(i);
            randomTime = 1;

            randStartTime = System.currentTimeMillis(); //get start time
            while(getCorrectSequence(testFileName).getResult()){
                System.out.println("aaaaRandomTime="+randomTime);
                List<Node> nodes=getCorrectSequence(testFileName).getNodes();
//                for (int j = 0; j <nodes.size(); j++) {
//                    Node node =nodes.get(i);
//                    System.out.println("****************");
//                    if (node instanceof ScheduleNode)
//                    {
//                        ScheduleNode tempnode=(ScheduleNode) node;
//                        System.out.println(tempnode.toString());
//                    }
//                    else if (node instanceof ReadWriteNode)
//                    {
//                        ReadWriteNode tempnode=(ReadWriteNode) node;
//                        System.out.println(tempnode.toString());
//                    }
//                    System.out.println("****************");
//                }
                randomTime++;
            }
            randEndTime = System.currentTimeMillis();

            RANDOM += randomTime;
            row.createCell(0).setCellValue(i);
            row.createCell(1).setCellValue(randomTime);
            row.createCell(2).setCellValue((randEndTime - randStartTime));

            long runtime=(randEndTime - randStartTime);
            RANDOMTIME += runtime;
            bw3.write("--------------current iteration: " + i);
            bw3.newLine();
            bw3.flush();
        }

        row = sheet.createRow(iteration+1);
        row.createCell(0).setCellValue("average:");
        row.createCell(1).setCellValue(RANDOM*1.0/iteration);
        row.createCell(2).setCellValue(RANDOMTIME*1.0/iteration);
        FileOutputStream fos = new FileOutputStream("G:\\random\\" + testFileName + "_" + iteration + ".xls");
        workbook.write(fos);
        fos.close();
        bw3.close();
        fw3.close();

    }


    public static void main(String[] args) throws IOException {

//		String[] testFileNames = {"consisitency.Main","critical.Critical","datarace.Main",
//				"hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort"};

        File file=new File("G:\\expProject\\ars-programs-ofGaoYi\\Coverage");
// 		String [] testFileNames= file.list();
       String[] testFileNames = {"SimpleTest.Main"};
        for(String testFileName : testFileNames){
            {
                Run(testFileName);
                new File("G:\\iteration.txt").delete();
            }
        }
    }

}
