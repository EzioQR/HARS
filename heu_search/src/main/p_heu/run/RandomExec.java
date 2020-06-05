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

public class RandomExec {

	public static int threshod = 0;

	public static void main(String[] args) throws IOException {

		String[] testFileNames = {"alarmclock.AlarmClock","bubblesort.BubbleSort","hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort","producerConsumer.ProducerConsumer",
				"reorder.ReorderTest","SimpleTest.Main","twoStage.Main","org.apache.log4j.TestThrowableStrRep","wronglock2.Main"};

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

		int randomTime = 0;
		int iteration = 10;

		for(int i = 0;i<iteration;i++){

			randomTime = 1;
			while(getCorrectSequence(testFileName).getResult()){
				randomTime++;
			}
		}
		System.out.println("randomTime:" + randomTime);

	}
}
