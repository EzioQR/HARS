package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.Node;
import p_heu.entity.SearchState;
import p_heu.entity.pattern.Pattern;
import p_heu.entity.sequence.Sequence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public abstract class CoverageBasedSearch extends Search {

    protected Set<Sequence> correctSeqs;

    protected LinkedList<Sequence> queue;
    protected Sequence revSequence;
    protected int scheduleThreshod;
    protected Sequence errorSequence;
    protected boolean reachedNoneZero;

    private List<Sequence> errorsequences;
    private int lastqueueFirst=-1;
    private boolean deathloop=false;

    private File file;
    private FileWriter fw;
    private BufferedWriter bw;

    private File file2;
    private FileWriter fw2;
    private BufferedWriter bw2;

    private Set<Pattern> CoverPatterns;

    private int forwardtime=0;

    public void mergeNewPatterns(Set<Pattern> newPatterns)
    {

        if (this.CoverPatterns.isEmpty())
        {
            for(Pattern pattern:newPatterns)
            {
                Pattern temp=pattern.copy();
                this.CoverPatterns.add(temp);
            }
            return;
        }

        for (Pattern newpattern:newPatterns)
        {
            boolean flag=false;
            for (Pattern alreadypattern:CoverPatterns)
            {
                if (newpattern.isSamePattern(alreadypattern))
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
                this.CoverPatterns.add(temp);
            }
        }
    }

    public void setFilepath(String filepath) throws IOException {
        this.file = new File(filepath);
        if(!file.exists()){
            file.getParentFile().mkdirs();
        }
        this.file.createNewFile();
        this.fw = new FileWriter(file, true);
        this.bw = new BufferedWriter(fw);

        int lastindex =filepath.lastIndexOf("\\");
        int lastindex2 =filepath.lastIndexOf(".csv");
        this.file2 = new File(filepath.substring(0,lastindex)+"\\"+filepath.substring(lastindex+1,lastindex2)+"errorsequences.csv");
        if(!file2.exists()){
            file2.getParentFile().mkdirs();
        }
        this.file2.createNewFile();
        this.fw2 = new FileWriter(file2, true);
        this.bw2 = new BufferedWriter(fw2);

    }

    protected CoverageBasedSearch(Config config, VM vm) {
        super(config, vm);
        this.correctSeqs = new HashSet<>();
        this.queue = new LinkedList<>();
        this.revSequence = null;
        this.scheduleThreshod = 5;
        this.errorSequence = null;
        this.reachedNoneZero = false;
        this.CoverPatterns=new HashSet<>();
    }

    @Override
    public boolean requestBacktrack () {
        doBacktrack = true;
        return true;
    }

    @Override
    public boolean supportsBacktrack () {
        return true;
    }

    @Override
    public void search() {
        // TODO 编写search函数
        //每个从队列中拿出的距离最远的Sequence
        Sequence sequence = null;
        notifySearchStarted();

        errorsequences=new ArrayList<>();
        scheduleThreshod = 5;
        Sequence tempsequence=null;
        //保存初始状态
        RestorableVMState init_state = vm.getRestorableState();
        while(!done){
            if(isEndState()){
                //设置正确执行序列的状态为TRUE
                sequence.setResult(true);
                sequence.setFinished(true);
                addCorrectSeqs(sequence);

                vm.restoreState(init_state);
                vm.resetNextCG();
                //当前序列置为空
                sequence = null;
                continue;
            }
            if (sequence!=null)
            {
                System.out.print("seqss:\t[");
                for(SearchState state : sequence.getStates()) {
                    System.out.print(state.getStateId() + ", ");
                }
                System.out.println("]");
            }
             while(forward()){
                notifyStateAdvanced();
                forwardtime++;

                //将当前的状态合并到上一状态之后，并添加到队列中
                sequence = mergeSeq(sequence,revSequence);
                queue.add(sequence.copy());

                if(currentError != null){
                    notifyPropertyViolated();
                    if(hasPropertyTermination()){
                        break;
                    }
                }

                if(!checkStateSpaceLimit()){
                    notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
                    //can't go on, we exhausted our memory
                    break;
                }
                if(backtrack()){
                    //回溯
                    notifyStateBacktracked();
                }
                tempsequence=sequence.copy();
            }
            System.out.println("queue的大小="+queue.size());
            System.out.print("seqss\t[");
            for(SearchState state : sequence.getStates()) {
                System.out.print(state.getStateId() + ", ");
            }
            System.out.println("]");
            if (currentError!=null)
            {
                if (!checksequence(sequence)||errorsequences.isEmpty())
                {
                    errorsequences.add(sequence.copy());
                }

                try {
                    bw2.write(""+errorsequences.size());
                    bw2.newLine();
                    bw2.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                queue.removeLast();
                System.out.println("找到了"+errorsequences.size()+"个错误");
                System.out.println("forwardtime="+forwardtime);
                System.out.print("failed\t[");
                for(SearchState state : sequence.getStates()) {
                    System.out.print(state.getStateId() + ", ");
                }
                System.out.println("]");
                if (deathloop)
                    deathloop=false;
                if (queue.size()>0)
                {
                    sequence = queue.poll();
                    vm.restoreState(tempsequence.getLastState().getState());
                }
                else {
                    vm.restoreState(init_state);
                    vm.resetNextCG();
                    sequence = null;
                }
                done=false;
                currentError=null;
                continue;
            }

            //对当前队列进行排序
            randQueue();
            sortQueue();
            //根据阈值删除队列中多余的sequence
            while(scheduleThreshod > 0 && queue.size() > scheduleThreshod){
                queue.removeLast();
            }
            //判断当前队列中是否存在sequence，当队列size 小于0 表明找到一个正确的sequence
            if(queue.size() > 0){
                sequence = queue.poll();
                vm.restoreState(sequence.getLastState().getState());
            }else{
                //将所有正确的sequence添加到正确的序列集合中
                sequence.setResult(true);
                sequence.setFinished(true);
                addCorrectSeqs(sequence);
                vm.restoreState(init_state);
                vm.resetNextCG();
                //当前序列置为空
                sequence = null;
                System.gc();
            }
            //记录pattern变化的数量
            if (sequence!=null)
            {
                Set<Pattern> newsequencePatterns=sequence.getPatterns();
                mergeNewPatterns(newsequencePatterns);
                try {
                    int newpatternsetsize=this.CoverPatterns.size();
                    bw.write(""+newpatternsetsize);
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        notifySearchFinished();
    }
    protected  Sequence mergeSeq(Sequence seqOld,Sequence seqNew){
        if(seqOld!=null){
            SearchState currentState = seqNew.getLastState();
            return seqOld.advance(currentState.getStateId(),currentState.getState(),seqNew.getNodes());
        }else{
             return seqNew;
        }

    }

    protected void addCorrectSeqs(Sequence seqs) {
        System.out.print("pass\t[");
        for(SearchState state : seqs.getStates()) {
            System.out.print(state.getStateId() + ", ");
        }
        System.out.println("]");
//        for (Sequence s : queue) {
//            s.setMatched(false);
//        }
        correctSeqs.add(seqs);
    }

    public void addCurrentSequence(Sequence seq){
        this.revSequence = seq;
    }

    public Sequence getErrorSequence() {
        return errorSequence;
    }

    public void addQueue(Sequence seq) {
        queue.add(seq);
    }

    public Set<Sequence> getCorrectSeqs() {
        return correctSeqs;
    }

    public void setCorrectSeqs(Set<Sequence> correctSeqs) {
        this.correctSeqs = correctSeqs;
    }

    protected Sequence findSequenceByLastState(int lastStateId) {
        for (Sequence seq : queue) {
            if (seq.getLastState().getStateId() == lastStateId) {
                return seq;
            }
        }
        return null;
    }
    public boolean checksequence(Sequence seq)
    {
        for (Sequence origin:errorsequences)
        {
            if (seq.getStates().size()==origin.getStates().size())
            {
                boolean flag=false;
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
    public void stateAdvance(int lastStateId, List<Node> nodes) {
        Sequence seq = findSequenceByLastState(lastStateId);
        queue.remove(seq);
    }

    protected void randQueue() {
        Collections.shuffle(this.queue);//随机打乱原队列的顺序
        //Collections.sort(this.queue, getComparator());
    }

    protected String findorder(LinkedList<Sequence> subqueue)
    {
        String s="";
        for (int i = 0; i <subqueue.size(); i++) {
            s=s+subqueue.get(i).getId()+",";
        }
        return s.substring(0,s.length()-1);
    }

    protected void CreateQueueById(String s)
    {
        String []ids=s.split(",");
        List<String> resultList= new ArrayList<>(Arrays.asList(ids));

        Iterator<String> it = resultList.iterator();
        while(it.hasNext()){
            String str = (String)it.next();
            if("".equals(str)){
                it.remove();
            }
        }

        LinkedList<Sequence> tempqueue=new LinkedList<>();
        for (int i = 0; i < resultList.size(); i++) {
            int id=Integer.parseInt(resultList.get(i));
            for (int j = 0; j < queue.size(); j++) {
                if (queue.get(j).getId()==id)
                    tempqueue.add(queue.get(j));
            }
        }
        queue=tempqueue;

    }

    protected LinkedList<Sequence> subLinkedList(LinkedList<Sequence>base,int finalindex)
    {
        LinkedList<Sequence> result=new LinkedList<>();
        for (int i = 0; i <finalindex; i++) {
            result.add(base.get(i));
        }
        return result;
    }

    protected void sortQueue() {
        if (!deathloop)
        {
            sortrule();
            if (this.lastqueueFirst == this.queue.getFirst().getId() && this.queue.size()>1)
            {
                System.out.println("陷入了死循环....");
                deathloop=true;
                randQueue();
                return;
            }

            this.lastqueueFirst=this.queue.getFirst().getId();
        }
        else
        {
            randQueue();
        }

    }

    protected abstract void sortrule();
}
