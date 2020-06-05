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
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class CaculateRunNum {
	public static void notifyend() throws Exception {
		// TODO 自动生成的方法存根

		String readline = null;
		String inTemp = null;
		//String outTemp = null;
		String turnLine = "\n";
		final String client = "Client:";
		final String server = "Server:";

		int port = 4000;
		byte ipAddressTemp[] = {127, 0, 0, 1};
		InetAddress ipAddress = InetAddress.getByAddress(ipAddressTemp);

		//首先直接创建socket,端口号1~1023为系统保存，一般设在1023之外
		Socket socket = new Socket(ipAddress, port);

		//创建三个流，系统输入流BufferedReader systemIn，socket输入流BufferedReader socketIn，socket输出流PrintWriter socketOut;
		BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
		BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter socketOut = new PrintWriter(socket.getOutputStream());

		OutputStream outputStream = socket.getOutputStream();
		InputStream inputStream = socket.getInputStream();

		byte[] bytes = new byte[1024];
		String message="end";

		System.out.println("通知python结束");
		outputStream.write(message.getBytes());

		systemIn.close();
		socketIn.close();
		socketOut.close();
		socket.close();





	}
	public static void main(String[] args) throws IOException {

//		String[] testFileNames = {"consisitency.Main","critical.Critical","datarace.Main",
//				"hashcodetest.HashCodeTest","linkedlist.BugTester","mergesort.MergeSort"};

        File file=new File("G:\\expProject\\ars-programs-ofGaoYi\\Coverage");
//		String [] testFileNames= file.list();
        String[] testFileNames = {"airline.Main"};//critical.Critical
		for(String testFileName : testFileNames){
            {
                Run(testFileName);
                new File("G:\\iteration.txt").delete();
            }
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

	public static void Run(String testFileName) throws IOException {


		Sequence correctSeq = null;
		int randomTime = 0;
		Set<Sequence> correctSeqs = null;
		int iteration = 100;
		int HEUNUM = 0;
		int NEWSUM=0;
		int RANDOM = 0;
        long runtimes= 0;
		String[] str = new String[]{
				"+classpath=out/production/heu_search",
				"+search.class=p_heu.search.IntelligentBasedSearch",
				testFileName};
		Config config = new Config(str);
		Filter filter = Filter.createFilePathFilter();

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

		int QueueN=5;
		try {
			writeTxtFile("IntelligentBasedSearch",new File("G:\\methodname.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		row = sheet.createRow(0);
		row.createCell(0).setCellValue("Index");
		row.createCell(1).setCellValue("N="+QueueN);
        row.createCell(2).setCellValue("Time=");
		try {
			writeTxtFile(""+QueueN,new File("G:\\QueueNumber2.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}

        long time1= System.currentTimeMillis();
        long time2= System.currentTimeMillis();

		for(int i = 1;i<=iteration;i++){
			System.out.println("--------------current iteration: " + i);





			row = sheet.createRow(i);
            time1= System.currentTimeMillis();
			correctSeq = getCorrectSequence(testFileName);
            time2= System.currentTimeMillis();
            long firsttime=time2-time1;
			if(correctSeq.getResult() == true){

				correctSeqs = new HashSet<>();
				correctSeqs.add(correctSeq);
                time1= System.currentTimeMillis();
				BasicPatternFindingListener listener = new BasicPatternFindingListener(correctSeqs);
				listener.setPositionFilter(filter);
				JPF jpf = new JPF(config);
				jpf.addListener(listener);
				jpf.run();
				jpf = null;
                time2= System.currentTimeMillis();

                long runtime=time2-time1+firsttime;
                runtimes+=runtime;
				System.gc();

                int p=listener.getCorrectSeqs().size()+1;
				row.createCell(0).setCellValue(i);
				row.createCell(1).setCellValue(listener.getCorrectSeqs().size()+1);
                row.createCell(2).setCellValue(runtime);
				HEUNUM += (listener.getCorrectSeqs().size()+1);
				System.gc();
                bw3.write("--------------current iteration: " + i+" p="+p);
                bw3.newLine();
                bw3.flush();

			}else{
				row.createCell(0).setCellValue(i);
				row.createCell(1).setCellValue(1);
                row.createCell(2).setCellValue(firsttime);
                runtimes+=firsttime;
				HEUNUM += 1;
				System.gc();
                bw3.write("--------------current iteration: " + i+" p="+1);
                bw3.newLine();
                bw3.flush();
			}
//
//			correctSeqs = new HashSet<>();
//			BasicPatternFindingListener listener2 = new BasicPatternFindingListener(correctSeqs);
//			JPF jpf = new JPF(config2);
//			jpf.addListener(listener2);
//			jpf.run();
//			jpf = null;
//			System.gc();
//			row.createCell(2).setCellValue(listener2.getCorrectSeqs().size()+1);
//			NEWSUM+= (listener2.getCorrectSeqs().size()+1);

		}

//		//通知python可以结束了
//		try {
//			notifyend();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

//		for(int i = 0;i<iteration;i++){
//			row = sheet.getRow(i);
//			randomTime = 1;
//			while(getCorrectSequence(testFileName).getResult()){
//				randomTime++;
//			}
//			RANDOM += randomTime;
//			row.createCell(3).setCellValue(randomTime);
//		}
		row = sheet.createRow(101);
		row.createCell(0).setCellValue("average:");
		row.createCell(1).setCellValue(HEUNUM/100.0);
        row.createCell(2).setCellValue(runtimes/100.0);
//		row.createCell(2).setCellValue(NEWSUM/100.0);
//		row.createCell(2).setCellValue(RANDOM/100.0);
		FileOutputStream fos = new FileOutputStream("G:\\N="+QueueN+"\\" + testFileName + "_" + iteration + ".xls");
		workbook.write(fos);
		fos.close();
		System.out.println("ARS="+HEUNUM/100.0);
		bw3.close();
		fw3.close();
//		System.out.println("NewMethod="+NEWSUM/100.0);
//		System.out.println("Random="+RANDOM/100.0);


	}
}
