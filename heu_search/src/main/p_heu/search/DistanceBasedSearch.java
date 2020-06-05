package p_heu.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.channels.SeekableByteChannel;
import java.util.*;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.RestorableVMState;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.Node;
import p_heu.entity.SearchState;
import p_heu.entity.sequence.Sequence;

public abstract class DistanceBasedSearch extends Search {

    protected Set<Sequence> correctSeqs;
    protected LinkedList<Sequence> queue;
    protected List<Sequence> tree;
    protected Sequence revSequence;
    protected int scheduleThreshod;
    protected Sequence errorSequence;
    protected boolean reachedNoneZero;

    private int lastqueueFirst=-1;
    private boolean deathloop=false;
    protected int ruleworktime=0;
    protected int sortedtime=0;

    protected int firstworktime=0;
    protected int rule1worktime=0;
    protected int rule2worktime=0;
    protected int rule3worktime=0;
    protected int rule4worktime=0;
    protected int randomtime=0;

    public int getFirstworktime() {
        return firstworktime;
    }

    public void setFirstworktime(int firstworktime) {
        this.firstworktime = firstworktime;
    }

    public int getRule1worktime() {
        return rule1worktime;
    }

    public void setRule1worktime(int rule1worktime) {
        this.rule1worktime = rule1worktime;
    }

    public int getRule2worktime() {
        return rule2worktime;
    }

    public void setRule2worktime(int rule2worktime) {
        this.rule2worktime = rule2worktime;
    }

    public int getRule3worktime() {
        return rule3worktime;
    }

    public void setRule3worktime(int rule3worktime) {
        this.rule3worktime = rule3worktime;
    }

    public int getRule4worktime() {
        return rule4worktime;
    }

    public void setRule4worktime(int rule4worktime) {
        this.rule4worktime = rule4worktime;
    }

    public int getRandomtime() {
        return randomtime;
    }

    public void setRandomtime(int randomtime) {
        this.randomtime = randomtime;
    }


    protected DistanceBasedSearch(Config config, VM vm) {
        super(config, vm);
        this.correctSeqs = new HashSet<>();
        this.queue = new LinkedList<>();
        this.tree=new ArrayList<>();
        this.revSequence = null;
        scheduleThreshod = 5;
        errorSequence = null;
        reachedNoneZero = false;
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

    public int ifaccesses(Sequence seq)
    {
        seq.setIndex();
        String index=seq.getIndex();
        for (int i = 0; i < this.tree.size(); i++) {
            String treeElementIndex=tree.get(i).getIndex();
            if (treeElementIndex.equals(index))
                return i;
        }
        return -1;
    }

    public void Prune()
    {
        for (int i = 0; i < this.queue.size(); i++) {
            Sequence temp=queue.get(i);
            int treeindex=ifaccesses(temp);
            if (treeindex!=-1)
            {
                if (tree.get(treeindex).isFullyExplored())
                {
                    System.out.println("当前被剪枝的节点是: "+tree.get(treeindex).getIndex());
                    queue.remove(queue.get(i));
                    i--;
                }
            }
        }
    }

    public void updatetree(Sequence sequence)
    {
        System.out.println("更新tree");
        int index=ifaccesses(sequence);
        System.out.println("没更新前 "+sequence.getIndex()+"它是"+tree.get(index).isFinished()+"叶子节点");
        tree.get(index).setResult(true);
        tree.get(index).setFinished(true);
        System.out.println("更新完后 "+sequence.getIndex()+"它是"+tree.get(index).isFinished()+"叶子节点");

        String s=sequence.getIndex();
        String []ss=s.split(",");
        int updatetimes=ss.length;

        for (int k =updatetimes; k>1 ; k--) {
            String updatestring=ss[0];
            for (int j = 1; j < k; j++) {
                updatestring=updatestring+","+ss[j];
            }

            for (int i = 0; i<tree.size(); i++) {

                if (tree.get(i).getIndex().equals(updatestring))
                {
                    System.out.println("\n当前更新扩展情况的节点是:"+updatestring+"它的扩展情况是"+tree.get(i).isFullyExplored()+"\t它有"+tree.get(i).getChilds().size()+"个儿子");


                    System.out.println("****************");
                    for (int j = 0; j < tree.get(i).getChilds().size(); j++) {
                        System.out.println(tree.get(i).getIndex());
                    }
                    System.out.println("****************");

                    boolean exploredresult=tree.get(i).CheckIsFullyExplored();
                    tree.get(i).setFullyExplored(exploredresult);//先把自己的扩展信息更新了
                    System.out.println("更新完后"+tree.get(i).getIndex()+"当前节点的扩展信息为"+tree.get(i).isFullyExplored());

                    tree.get(i).printChildren();

                    boolean newexploredresult=exploredresult;
                    if (!newexploredresult)
                        return;

                    //再把父节点中保存的对应当前子节点的扩展信息更新了
                    int fatherlocation=ifAccessedByString(tree.get(i).getFatherindex());
                    tree.get(fatherlocation).ChangeChildren(tree.get(i).getIndex(),tree.get(i).isFullyExplored());

                }
            }

        }
    }

    public int ifAccessedByString(String index)
    {
        for (int i = 0; i <this.tree.size() ; i++)
        {
            Sequence temp=this.tree.get(i);
            String tempindex=temp.getIndex();
            if (tempindex.equals(index))
            {
                return i;
            }
        }
        return -1;
    }

    public void check()
    {
        List<Sequence> result = new ArrayList<>(correctSeqs);
        for (int i = 0; i < result.size(); i++) {
            for (int j = i+1; j <result.size(); j++) {
                if (result.get(i).getIndex().equals(result.get(j).getIndex()))
                {
                    throw new RuntimeException(result.get(i).getIndex()+" 剪枝不彻底");
                }
            }
        }
    }

    @Override
    public void search() {
        // TODO 编写search函数
        //每个从队列中拿出的距离最远的Sequence
        Sequence sequence = null;
        notifySearchStarted();
        this.tree=new ArrayList<>();
        String searchStrategy=readTxtFile(new File("G:\\methodname.txt"));
        if (searchStrategy.equals("PatternDistanceBasedSearch"))
            scheduleThreshod = Integer.parseInt(readTxtFile(new File("G:\\QueueNumber1.txt")));
        else if (searchStrategy.equals("IntelligentBasedSearch"))
            scheduleThreshod = Integer.parseInt(readTxtFile(new File("G:\\QueueNumber2.txt")));
        else
            throw new RuntimeException("SearchStrategyError");

        //保存初始状态
        RestorableVMState init_state = vm.getRestorableState();

        System.out.println("搜索前找到了"+correctSeqs.size()+"个正确的trace");

        for (Sequence e:correctSeqs)
        {
            System.out.println(e.getIndex());
        }

        while(!done){

            while(forward()){
                notifyStateAdvanced();
                //将当前的状态合并到上一状态之后，并添加到队列中
                queue.add(mergeSeq(sequence, revSequence).copy());
                if(currentError != null){

                    for (Sequence temp:correctSeqs) {
                        System.out.print("Final\t[");
                        for(SearchState state : temp.getStates()) {
                            System.out.print(state.getStateId() + ", ");
                        }
                        System.out.println("]");
                    }
                    check();
                    System.out.println("rule1time="+rule1worktime+" rule2time="+rule2worktime+" rule3time="+rule3worktime+" rule4time="+rule4worktime+" randomtime="+randomtime);
                    //printTree();
                    notifyPropertyViolated();
                    if(hasPropertyTermination()){
                        errorSequence = sequence;
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
            }




            if(currentError != null){
                break;
            }

            if(isEndState()){
                //设置正确执行序列的状态为TRUE
                sequence.setResult(true);
                sequence.setFinished(true);
                addCorrectSeqs(sequence);
                updatetree(sequence);

                vm.restoreState(init_state);
                vm.resetNextCG();
                //当前序列置为空
                sequence = null;
                queue.clear();
                continue;
            }

            if (sequence!=null) {
                System.out.println("********------------*****");
                System.out.println("当前的爸爸节点是:" + sequence.getIndex());
                int fatherlocation = ifaccesses(sequence);
                System.out.println("当前从树中得到的是:"+tree.get(fatherlocation).getIndex());
                System.out.println("Tpassed Size="+correctSeqs.size());
                for (int i = 0; i < queue.size(); i++) {

                    System.out.print("queue[");
                    for (SearchState state : queue.get(i).getStates()) {
                        System.out.print(state.getStateId() + ", ");
                    }
                    System.out.println("]");

                    if (queue.get(i).getFatherindex().equals(sequence.getIndex()))
                    {
                        if (tree.get(fatherlocation).CheckIfhavedThisChildren(queue.get(i)))
                            continue;
                        else {
                            String childrenIndex=queue.get(i).getIndex();
                            tree.get(fatherlocation).setChildren(childrenIndex, false);

                        }
                    }
                }
                System.out.println("********------------*****\n");

                Prune();

//                System.out.println("********------------*****");
//                System.out.println("剪完枝之后queue的情况");
//                for (int i = 0; i < queue.size(); i++) {
//                    System.out.print("[");
//                    for (SearchState state : queue.get(i).getStates()) {
//                        System.out.print(state.getStateId() + ", ");
//                    }
//                    System.out.println("]");
//                }
//                System.out.println("********------------*****\n");
            }
            //对当前队列进行排序
            randQueue();
            sortQueue();
//            if (reachedNoneZero) {
//                System.out.println();
//                scheduleThreshod = 1;
//                reachedNoneZero = false;
//
//            }
            //根据阈值删除队列中多余的sequence
            while(scheduleThreshod > 0 && queue.size() > scheduleThreshod){
                queue.removeLast();
            }

            //判断当前队列中是否存在sequence，当队列size 小于0 表明找到一个正确的sequence
            if(queue.size() > 0){
                sequence = queue.poll().copy();

                System.out.println("当前选中的trace是"+sequence.getIndex());
                if (ifaccesses(sequence)==-1)
                    tree.add(sequence.copy());
                vm.restoreState(sequence.getLastState().getState());
            }else{
                //将所有正确的sequence添加到正确的序列集合中
                sequence.setResult(true);
                sequence.setFinished(true);
                addCorrectSeqs(sequence);
                updatetree(sequence);
                vm.restoreState(init_state);
                vm.resetNextCG();
                //当前序列置为空
                sequence = null;
                queue.clear();
                continue;
            }

        }
        notifySearchFinished();
    }

    protected Sequence mergeSeq(Sequence seqOld, Sequence seqNew){
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

    protected void sortQueue() {
        sortrule();
    }


    protected abstract void sortrule();

    public void printTree()
    {
        System.out.println("\n\n+********************");
        System.out.println("更新完后看一下树的情况...");
        for (int i = 0; i <tree.size() ; i++) {
            System.out.print("当前节点为"+tree.get(i).getIndex()+"\t他是否是叶子节点"+tree.get(i).isFinished()+'\t'+"它有"+tree.get(i).getChilds().size()+"个儿子\t");
            for (int j = 0; j <tree.get(i).getChilds().size() ; j++) {
                System.out.print(tree.get(i).getChilds().get(j).getIndex()+"\t");
            }
            System.out.println("是否被完全访问="+tree.get(i).isFullyExplored());


        }
        System.out.println("********************\n\n");
    }

}


