package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.SearchState;
import p_heu.entity.sequence.Sequence;

import java.io.*;
import java.sql.Time;
import java.util.*;

public class SingleExecutionSearch2 extends Search {

    protected Set<Sequence> correctSeqs;
    protected LinkedList<Sequence> queue;
    protected Sequence revSequence;

    private List<Sequence> errorsequences;
    private List<Sequence> correctsequences;



    public SingleExecutionSearch2(Config config, VM vm) {
        super(config, vm);
        this.correctSeqs = new HashSet<>();
        this.queue = new LinkedList<>();
        this.revSequence = null;

        this.errorsequences=new ArrayList<>();
        this.correctsequences=new ArrayList<>();

    }

    protected void randQueue() {
        Collections.shuffle(this.queue);//随机打乱原队列的顺序
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

    public void search() {
        // TODO 编写search函数
        notifySearchStarted();
        //保存初始状态
        RestorableVMState init_state = vm.getRestorableState();
        long currenttime1=System.currentTimeMillis();
        long currenttime2=System.currentTimeMillis();

        Sequence sequence = null;
        //创建写文件
        String program=readTxtFile(new File("F:\\program.txt"));

        new File("F:\\program.txt").delete();

        String writepath="F:\\ars-programs\\PrepareForMCTS\\"+program+".csv";
        File writeFile=new File(writepath);
        FileWriter fw1=null ;
        BufferedWriter bw1=null ;
        if(!writeFile.exists()){
            writeFile.getParentFile().mkdirs();
        }
        String columns="errorTraces,WriteTime,totalTraces";
        try {
            fw1= new FileWriter(writeFile, true);
            bw1= new BufferedWriter(fw1);
            bw1.write(columns);
            bw1.newLine();
            bw1.flush();
            writeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long TimeLimit =1000*60*60*2;


        while(!done){
            currenttime2=System.currentTimeMillis();
            if ((currenttime2-currenttime1)>= TimeLimit)
            {
                done=true;
                break;
            }

            while(forward()){
                long cur=currenttime2-currenttime1;
                System.out.println("当前运行了"+cur+"毫秒");
                notifyStateAdvanced();

                sequence=mergeSeq(sequence,revSequence);
//                System.out.print("当前的trace是\t[");
//                for(SearchState state : sequence.getStates()) {
//                    System.out.print(state.getStateId() + ", ");
//                }
//                System.out.println("]");


                if(currentError != null){
                    notifyPropertyViolated();

                    if(hasPropertyTermination()){
                        break;
                    }
                }

                if (depth >= depthLimit) {
                    done=true;
                    notifySearchConstraintHit("depth limit reached: " + depthLimit);
                    break;
                }


                if(!checkStateSpaceLimit()){
                    notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
                    //can't go on, we exhausted our memory
                    done=true;
                    break;
                }
            }

            if (currentError!=null)
            {
                if (!checksequence(sequence,errorsequences)||errorsequences.isEmpty())
                {
                    errorsequences.add(sequence.copy());

                    System.out.println("找到了"+errorsequences.size()+"个错误");

                    System.out.print("failed\t[");
                    for(SearchState state : sequence.getStates()) {
                        System.out.print(state.getStateId() + ", ");
                    }
                    System.out.println("]");

                    currenttime2=System.currentTimeMillis();
                    long runtime=currenttime2-currenttime1;
                    int tottaltraces=errorsequences.size()+correctsequences.size();
                    try {
                        bw1.write(errorsequences.size()+","+runtime+","+tottaltraces);
                        bw1.newLine();
                        bw1.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }




                vm.restoreState(init_state);
                vm.resetNextCG();
                sequence = null;

                done=false;
                currentError=null;
                continue;
            }

            if (isEndState())
            {
                if (!checksequence(sequence,correctsequences)||correctsequences.isEmpty())
                {
                    correctsequences.add(sequence.copy());

                    System.out.print("pass\t[");
                    for(SearchState state : sequence.getStates()) {
                        System.out.print(state.getStateId() + ", ");
                    }
                    System.out.println("]");

                }



                vm.restoreState(init_state);
                vm.resetNextCG();
                sequence = null;

                continue;
            }

        }

        try {
            fw1.close();
            bw1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifySearchFinished();
    }

    public boolean requestBacktrack () {
        doBacktrack = true;

        return true;
    }

    public boolean checksequence(Sequence seq,List<Sequence> seqs)
    {
        for (Sequence origin:seqs)
        {
            if (seq.getStates().size()==origin.getStates().size())
            {
                boolean flag=true;
                for (int i = 0; i < seq.getStates().size(); i++) {
                    if (seq.getStates().get(i).getStateId()!=origin.getStates().get(i).getStateId())
                        flag=false;
                }
                if (flag)
                {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean supportsBacktrack () {
        return true;
    }

    protected  Sequence mergeSeq(Sequence seqOld,Sequence seqNew){
        if(seqOld!=null){
            SearchState currentState = seqNew.getLastState();
            return seqOld.advance(currentState.getStateId(),currentState.getState(),seqNew.getNodes());
        }else{
            return seqNew;
        }

    }
    public void setCorrectSeqs(Set<Sequence> correctSeqs) {
        this.correctSeqs = correctSeqs;
    }
    protected void addCorrectSeqs(Sequence seqs) {
        System.out.print("pass\t[");
        for(SearchState state : seqs.getStates()) {
            System.out.print(state.getStateId() + ", ");
        }
        System.out.println("]");
        correctSeqs.add(seqs);
    }

    public void addCurrentSequence(Sequence seq){
        this.revSequence = seq;
    }

    public Set<Sequence> getCorrectSeqs() {
        return correctSeqs;
    }
}
