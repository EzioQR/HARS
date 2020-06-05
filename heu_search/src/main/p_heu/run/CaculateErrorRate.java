package p_heu.run;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.listener.BasicPatternFindingListener;
import p_heu.listener.ErrorRateListener;
import p_heu.listener.SequenceProduceListener;
import p_heu.search.ErrorRateSearch;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CaculateErrorRate {

	public static void main(String[] args) throws IOException {

		Sequence correctSeq = null;
		Set<Sequence> correctSeqs = null;
		String testFileName = "SingleTest.Main";//"CheckField"
		double heu_error_rate = 0;
		int iteration = 100;
		int errorRun = 0;

		String[] str = new String[]{
				"+classpath=out/production/heu_search",
				"+search.class=p_heu.search.ErrorRateSearch",
				testFileName};
		Config config = new Config(str);
		Filter filter = Filter.createFilePathFilter();

		correctSeq = getCorrectSequence(testFileName);
		while(!correctSeq.getResult()){
			correctSeq = getCorrectSequence(testFileName);
		}
		correctSeqs = new HashSet<>();
		correctSeqs.add(correctSeq);
		ErrorRateListener listener = new ErrorRateListener(correctSeqs);
		listener.setPositionFilter(filter);
		JPF jpf = new JPF(config);
		jpf.addListener(listener);
		jpf.run();
		heu_error_rate = listener.getErrorRate();

		for(int i = 0;i<iteration;i++){

			if(!getCorrectSequence(testFileName).getResult()){
				errorRun++;
			}
		}
		System.out.println("hue_search: " + heu_error_rate + "\n");
		System.out.println("random_search : " + errorRun/100.0 + "\n");
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
		return listener.getSequence();
	}
}
