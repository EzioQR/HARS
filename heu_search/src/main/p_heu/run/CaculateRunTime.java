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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CaculateRunTime {

	public static void main(String[] args) throws IOException {

//		String[] testFileNames = {"consisitency.Main","critical.Critical","datarace.Main",
//				"hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort"};
//		String[] testFileNames = {"account.Main","airline.Main","alarmclock.AlarmClock","allocationvector.TestCase","atmoerror.Main",
//									"bubblesort.BubbleSort","CheckField","consisitency.Main","critical.Critical","datarace.Main",
//									"even.Main","hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort","producerConsumer.ProducerConsumer",
//									"reorder.ReorderTest","SimpleTest.Main"};
//		String[] testFileNames = {"bubblesort.BubbleSort","CheckField","consisitency.Main","critical.Critical","datarace.Main",
//				"even.Main","hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort","producerConsumer.ProducerConsumer",
//				"reorder.ReorderTest","SimpleTest.Main"};
		String[] testFileNames = {"account.Main"};
		for(String testFileName : testFileNames){
			Run(testFileName);
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

		Sequence correctSeq = null;
		int randomTime = 0;
		Set<Sequence> correctSeqs = null;
		//String testFileName = "SimpleTest.Main";//"CheckField"
		int iteration = 10;
		long heuStartTime = 0;
		long heuEndTime = 0;
		long HEUNUMTIME = 0;

		long randStartTime = 0;
		long randEndTime = 0;
		long RANDOMTIME = 0;


		String[] str = new String[]{
				"+classpath=out/production/heu_search",
				"+search.class=p_heu.search.PatternDistanceBasedSearch",
				testFileName};
		Config config = new Config(str);
		Filter filter = Filter.createFilePathFilter();

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Number");
		HSSFRow row = null;

		for(int i = 0;i<iteration;i++){

			heuStartTime = System.currentTimeMillis();//get start time
			row = sheet.createRow(i);
			correctSeq = getCorrectSequence(testFileName);
			if(correctSeq.getResult() == true){

				correctSeqs = new HashSet<>();
				correctSeqs.add(correctSeq);
				BasicPatternFindingListener listener = new BasicPatternFindingListener(correctSeqs);
				listener.setPositionFilter(filter);
				JPF jpf = new JPF(config);
				jpf.addListener(listener);
				jpf.run();

				heuEndTime = System.currentTimeMillis(); //get end time

				row.createCell(0).setCellValue(i);
				row.createCell(1).setCellValue((heuEndTime - heuStartTime));
				System.gc();

			}else{
				heuEndTime = System.currentTimeMillis(); //get end time
				row.createCell(0).setCellValue(i);
				row.createCell(1).setCellValue((heuEndTime - heuStartTime));
				System.gc();
			}
			HEUNUMTIME += (heuEndTime - heuStartTime);
		}

		System.out.println(" ============= heuend ============= ");


		for(int i = 0;i<iteration;i++){
			randStartTime = System.currentTimeMillis(); //get start time
			row = sheet.getRow(i);
			randomTime = 1;
			while(getCorrectSequence(testFileName).getResult()){
				randomTime++;
			}
			randEndTime = System.currentTimeMillis();
			row.createCell(2).setCellValue((randEndTime - randStartTime));

			RANDOMTIME += (randEndTime - randStartTime);
		}


		row = sheet.createRow(iteration);
		row.createCell(0).setCellValue("average:");
		row.createCell(1).setCellValue(HEUNUMTIME/10.0);
		row.createCell(2).setCellValue(RANDOMTIME/10.0);
		FileOutputStream fos = new FileOutputStream("./newresult/result5/" + testFileName + "_" + iteration + ".xls");
		workbook.write(fos);
		fos.close();
	}
}
