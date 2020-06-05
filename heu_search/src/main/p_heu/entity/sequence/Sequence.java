package p_heu.entity.sequence;

import java.util.*;
import gov.nasa.jpf.vm.RestorableVMState;
import p_heu.entity.*;
import p_heu.entity.pattern.MemoryAccessPair;
import p_heu.entity.pattern.Pattern;

public class Sequence{
    private int id;
    private List<Node> nodes;
    private List<SearchState> states;
    private boolean finished;
    private boolean result;
    private double distance;

    private String index;


    private String fatherIndex;

    private Set<Pattern> patterns;

    public Set<Sequence> getCorrectSeqs() {
        return correctSeqs;
    }

    private Set<Sequence> correctSeqs;//当前程序中记录的Tpassed的trace的集合,用于计算当前的sequnce与之距离

    private double writetimes;
    private int switchtimes;
    private int RWswitchtimes;
    private int sharedvarnumbers;

    private boolean isFullyExplored;

    private boolean consist;//distance的值是否过期

    private boolean matched;

    private List<ChildNode> childs=new ArrayList<>();;


    public boolean getConsist()
    {
        return this.consist;
    }
    public boolean getmatched()
    {
        return this.matched;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isConsist() {
        return consist;
    }

    public void setmatched(boolean value)
    {
        this.matched=value;
    }

    public List<ChildNode> getChilds() {
        return childs;
    }

    public String getFatherIndex() {
        setFatherIndex();
        return fatherIndex;
    }

    public void setFatherIndex() {
        getIndex();
        this.fatherIndex=getIndex().substring(0,getIndex().length()-2);
    }

    public Sequence() {
        this.id=0;
        this.nodes = new ArrayList<>();
        this.states = new ArrayList<>();
        this.finished = false;
        this.result = false;
        this.distance = 0;
        this.consist = true;
        this.patterns = null;
        this.matched = false;
        this.correctSeqs = null;
        this.sharedvarnumbers=0;
        this.writetimes=0;
        this.switchtimes=0;
        this.RWswitchtimes=0;
        this.isFullyExplored=false;
        this.childs=new ArrayList<>();
    }


    public boolean isFullyExplored() {
        return isFullyExplored;
    }

    public void setFullyExplored(boolean fullyExplored) {
        isFullyExplored = fullyExplored;
    }

    public String getIndex() {
        setIndex();
        return index;
    }


    public void setChildren(String index,boolean isFullyexplored) {
        ChildNode node=new ChildNode();
        node.setIndex(index);
        node.setFullyExplored(isFullyexplored);
        this.childs.add(node.copy());
    }

    public boolean CheckIfhavedThisChildren(Sequence sequence)
    {
        String index=sequence.getIndex();
        for (int i = 0; i <childs.size(); i++) {
            if (childs.get(i).getIndex().equals(index))
            {
                return true;
            }
        }
        return false;
    }

    public void setIndex() {
        String s=""+states.get(0).getStateId();
        for (int i = 1; i <states.size(); i++) {
            s=s+","+states.get(i).getStateId();
        }
        this.index=s;
    }
    public Sequence(Set<Sequence> correctSeqs) {
        this();
        this.correctSeqs = correctSeqs;
    }
    private Sequence(int id,List<Node> nodes, List<SearchState> states, boolean finished,
                     boolean result, double distance, boolean consist, Set<Pattern> patterns, boolean matched, Set<Sequence> correctSeqs,int switchtimes,double writetimes,int RWswitchtimes,int sharedvarnumbers) {
        this.id=id;
        this.nodes = new ArrayList<>();
        this.nodes.addAll(nodes);
        this.states = new ArrayList<>();
        this.states.addAll(states);
        this.finished = finished;
        this.result = result;
        this.distance = distance;
        this.consist = consist;
        this.patterns = patterns;
        this.matched = matched;
        this.correctSeqs = correctSeqs;

    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Sequence copy() {
        return new Sequence(
                id,nodes, states, finished, result, distance, consist, patterns, matched, correctSeqs,switchtimes,writetimes,RWswitchtimes,sharedvarnumbers
        );
    }

    private void reduceSeq() {
        //从前往后，从后往前同时遍历，删除sequence中相对靠后的相同的RW节点
        List<Node> nodesList = this.getNodes();
        for (int i = 0; i < nodesList.size(); i++) {
            if (nodesList.get(i) instanceof ReadWriteNode) {
                for (int j = i - 1; j >= 0; j--) {
                    if (nodesList.get(j) instanceof ReadWriteNode) {
                        ReadWriteNode rwi = (ReadWriteNode) nodesList.get(i);
                        ReadWriteNode rwj = (ReadWriteNode) nodesList.get(j);
                        if ((rwi.getId() != rwj.getId())
                                && rwi.getElement().equals(rwj.getElement())
                                && rwi.getField().equals(rwj.getField())
                                && rwi.getThread().equals(rwj.getThread())
                                && rwi.getType().equals(rwj.getType())
                                && rwi.getPosition().equals(rwj.getPosition())) {
                            this.getNodes().remove(j);
                            i--;
                            break;
                        }
                    }
                }
            }
        }
    }

    public List<Node> getNodes() {
        return this.nodes;
    }


    public List<SearchState> getStates() {
        return this.states;
    }

    public SearchState getLastState() {
        if (this.states.size() == 0) {
            return null;
        }
        else {
            return this.states.get(this.states.size() - 1);
        }
    }

//	public void addState(SearchState state) {
//		this.states.add(state);
//	}

    public boolean isFinished() {
        return this.finished;
    }

    public boolean getResult() {
        return this.result;
    }

//    public void setResult(boolean result) {
//	    this.result = result;
//	    if (this.finished) {
//	        throw new RuntimeException("Already a finished sequence.");
//        }
//        else {
//	        this.finished = true;
//        }
//    }

    public Sequence advance(int stateId, RestorableVMState state, List<Node> nodes) {
        Sequence seq = this.copy();
        seq.states.add(new SearchState(stateId, state));
        seq.nodes.addAll(nodes);
        seq.consist = false;
        seq.matched = false;
        seq.reduceSeq();
        return seq;
    }

    public Sequence advanceToEnd(int stateId, RestorableVMState state, List<Node> nodes, boolean result) {
        Sequence seq = advance(stateId,state, nodes);
        seq.finished = true;
        seq.result = result;
        seq.consist = false;
        seq.matched = false;
        seq.reduceSeq();
        return seq;
    }

    public double getDistance() {
        if (consist) {
            return this.distance;
        }
        else {
            return calculateDistance();
        }
    }

    public int calThreadSwitch() {
        int count = 0;
        ScheduleNode last = null;
        for (Node node : this.nodes) {
            if (!(node instanceof ScheduleNode)) {
                continue;
            }
            ScheduleNode current = (ScheduleNode)node;
            if (last != null && (!last.getThread().equals(current.getThread()))) {
                count += 1;
            }
            last = current;
        }
        this.switchtimes=count;
        return count;
    }

    private double calwritetimes()
    {
        int count =0;
        int RWtimes=0;
        for (Node node : this.nodes) {
            if (node instanceof ScheduleNode) {
                continue;
            }
            ReadWriteNode current = (ReadWriteNode)node;
            RWtimes++;
            if (current.getType().equals("WRITE") ) {
                count += 1;
            }
        }
        this.writetimes=count;
        //this.writetimes =count*1.0/RWtimes;
        return count;
    }

    private int calsharedvarnumbers()
    {
        int count =0;
        HashSet<String> varnames=new HashSet<>();
        for (Node node : this.nodes) {
            if (node instanceof ScheduleNode) {
                continue;
            }
            else{
                ReadWriteNode current = (ReadWriteNode)node;
                varnames.add(current.getElement()+"-"+current.getField());
            }
        }
        this.sharedvarnumbers =varnames.size();
        return varnames.size();

    }

    public int calRWswitchtimes()
    {
        int count = 0;
        ReadWriteNode last = null;
        for (Node node : this.nodes) {
            if (node instanceof ScheduleNode) {
                continue;
            }
            ReadWriteNode current = (ReadWriteNode)node;
            if (last != null && (!last.getType().equals(current.getType()))) {
                count += 1;
            }
            last = current;
        }
        this.RWswitchtimes=count;
        return count;
    }

    public double getWritetimes() { return calwritetimes(); }

    public int getSwitchtimes() {
        return calThreadSwitch();
    }

    public int getRWswitchtimes(){return calRWswitchtimes();}

    public int getsharedvarnumbers(){return calsharedvarnumbers();}

    private double calculateDistance() {
        if (correctSeqs == null) {
            throw new RuntimeException(
                    "correct sequence set is null: please use another constructor to create sequence or set correct sequence."
            );
        }

        int smallestSize = -1;
        for (Sequence correctSeq : correctSeqs) {

            Set<Pattern> differencePatterns = new HashSet<>();
            for (Pattern p : this.getPatterns()) {
                if (!correctSeq.isIn(p)) {
                    differencePatterns.add(p);
                }
            }
            //System.out.println("diff size: " + differencePatterns.size());
            if (smallestSize == -1 || differencePatterns.size() < smallestSize) {
                smallestSize = differencePatterns.size();
            }
        }
        this.distance = smallestSize;

        this.consist = true;
        return this.distance;
    }

    //把差集换成Jaccorcd系数
    private double calculateDistance2() {
        if (correctSeqs == null) {
            throw new RuntimeException(
                    "correct sequence set is null: please use another constructor to create sequence or set correct sequence."
            );
        }

        double smallestSize = -1;

        for (Sequence correctSeq : correctSeqs) {

            HashSet <Pattern> jiaoji=new HashSet();
            HashSet<Pattern> bingji=new HashSet();

            jiaoji.addAll(this.getPatterns());
            jiaoji.retainAll(correctSeq.getPatterns());

            bingji.addAll(this.getPatterns());
            bingji.addAll(correctSeq.getPatterns());

            double Jaccord=jiaoji.size()*1.0/bingji.size();

            if (smallestSize == -1 ||Jaccord< smallestSize) {
                smallestSize =Jaccord;
            }
        }
        this.distance = smallestSize;

        this.consist = true;
        return this.distance;
    }

    public String getFatherindex() {
        String s=""+states.get(0).getStateId();
        for (int i = 1; i <states.size()-1; i++) {
            s=s+","+states.get(i).getStateId();
        }

        return s;
    }

    public void ChangeChildren(String index,boolean ifexplored) {
        System.out.println("待更新的儿子节点为"+index);
        for (int i = 0; i <this.childs.size() ; i++) {
            if (this.childs.get(i).getIndex().equals(index)) {
                this.childs.get(i).setFullyExplored(ifexplored);
                break;
            }
        }
    }

    public boolean CheckIsFullyExplored() {
        for (int i = 0; i <this.childs.size(); i++) {
            if (this.childs.get(i).getFullyExplored()==false)
                return false;
        }
        return true;
    }

    public void printChildren()
    {
        if (this.childs.size()==0)
        {
            System.out.println("我是叶子节点,我没有儿子");
            System.out.println();
            return;
        }
        else {
            System.out.println();
            System.out.println("*********************");
            for (int i = 0; i <childs.size(); i++) {
                String index=childs.get(i).getIndex();
                boolean result=childs.get(i).getFullyExplored();
                System.out.println("当前儿子节点是:"+index+"\t它是否被完全扩展:"+result);
            }
            System.out.println("*********************\n");
        }
    }

    //当correctSeqs发生变动时，需要调用，使distance保持更新
    public void distanceNeedUpdate() {
        this.consist = false;
    }

    public Set<Pattern> getPatterns() {
        if (matched) {
            return this.patterns;
        }
        else {
            this.patterns = matchPatterns(Pattern.getPatternSet());
            this.matched = true;
            return this.patterns;
        }
    }

    public void setCorrectSeqs(Set<Sequence> correctSeqs) {
        this.correctSeqs = correctSeqs;
    }

    public boolean isIn(Pattern pattern) {
        Set<Pattern> patterns = this.getPatterns();
        for (Pattern p : patterns) {
            if (p.isSameExecptThread(pattern)) {
                return true;
            }
        }
        return false;
    }

    public Set<Pattern> matchPatterns(String patternSet) {
        Set<Pattern> result = new HashSet<>();
        MemoryAccessPair[] pairs = Pattern.getMemoryAccessPairs();
        List<MemoryAccessPair> matchedPairs = matchPairs(pairs);
        for (Pattern pair : matchedPairs) {
            boolean contains = false;
            for (Pattern p : result) {
                if (p.isSameExecptThread(pair)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                result.add(pair);
            }
        }
//        result.addAll(matchedPairs);
        for (int i = 0; i < matchedPairs.size(); ++i) {
            for (int j = i + 1; j < matchedPairs.size(); ++j) {
                MemoryAccessPair pair1 = matchedPairs.get(i);
                MemoryAccessPair pair2 = matchedPairs.get(j);
                Pattern pattern = null;
                if (patternSet.equals("falcon")) {
                    pattern = Pattern.tryConstructFalconPattern(pair1, pair2);
                }
                else if (patternSet.equals("unicorn")) {
                    pattern = Pattern.tryConstrucUnicornPattern(pair1, pair2);
                }
                else {
                    throw new RuntimeException("unknown pattern set");
                }
                if (pattern != null) {

                    //检查result中是否已经有相同的pattern
                    boolean contains = false;
                    for (Pattern p : result) {
                        if (pattern.isSameExecptThread(p)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        result.add(pattern);
                    }
                }
            }
        }
        return result;
    }

    private List<MemoryAccessPair> matchPairs(MemoryAccessPair[] pairs) {
        List<MemoryAccessPair> result = new ArrayList<>();
        for (int i = 0; i < this.nodes.size(); ++i) {
            if (this.nodes.get(i) instanceof ReadWriteNode) {
                ReadWriteNode node = (ReadWriteNode)this.nodes.get(i);
                result.addAll(matchNext(node, i + 1, pairs));
            }
        }
        return result;
    }

    private Set<MemoryAccessPair> matchNext(ReadWriteNode node, int begin, MemoryAccessPair[] pairs) {
        Set<MemoryAccessPair> matchedPairs = new HashSet<>();
        for (int i = begin; i < this.nodes.size(); ++i) {
            if (!(this.nodes.get(i) instanceof ReadWriteNode)) {//判断是否是读写点
                continue;
            }
            ReadWriteNode nextNode = (ReadWriteNode)this.nodes.get(i);
            if (!node.isSameInstance(nextNode)) {   //  判断操作的是否是同一个变量
                continue;
            }
            MemoryAccessPair matchedPair = isMatch(node, nextNode, pairs);
            if (matchedPair != null) {  //判断是否匹配成功
                matchedPairs.add(matchedPair);
            }
            if (nextNode.getType().equals("WRITE")) { //如果当前结点是WRITE不管匹配是否成功，都跳出循环
                break;
            }
        }
        return matchedPairs;
    }

    private MemoryAccessPair isMatch(ReadWriteNode node1, ReadWriteNode node2, MemoryAccessPair[] pairs) {
        //是否是同一个变量已经在上一个函数检查，这里不需要

        for (MemoryAccessPair pair : pairs) {
            //检查线程是否与pair要求一致
            PatternTypeNode[] ptNode = pair.getPatternType().getNodes();
            if (node1.getThread().equals(node2.getThread()) != ptNode[0].getThread().equals(ptNode[1].getThread())) {
                continue;
            }

            //检查读写种类是否一致
            if (node1.getType().equals(ptNode[0].getType()) && node2.getType().equals(ptNode[1].getType())) {
                return new MemoryAccessPair(pair.getPatternType(), new ReadWriteNode[] {
                        node1, node2
                });
            }
        }
        return null;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Sequence {\n");
        stringBuilder.append("\tnodes:\n");
        for (Node node : nodes) {
            stringBuilder.append("\t");
            stringBuilder.append(node);
            stringBuilder.append("\n");
        }
        stringBuilder.append("\tstates:\n");
        for (SearchState state : states) {
            stringBuilder.append("\t");
            stringBuilder.append(state);
            stringBuilder.append("\n");
        }
        stringBuilder.append("\tfinished: ");
        stringBuilder.append(finished);
        stringBuilder.append("\n\tresult: ");
        stringBuilder.append(result);
        stringBuilder.append("\n}");
        return stringBuilder.toString();
    }
}
