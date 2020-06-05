package p_heu.listener;

import gov.nasa.jpf.ListenerAdapter;

import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.SearchState;
import p_heu.entity.filter.Filter;
import p_heu.entity.sequence.Sequence;
import p_heu.search.*;
import java.util.ArrayList;
import java.util.Set;

public class BasicPatternFindingListener extends ListenerAdapter {

    private Sequence sequence;
    private SearchState currentState;
    private ArrayList<Node> currentStateNodes;
    private int nodeId;
    private boolean execResult;
    private Filter positionFilter;
    private Set<Sequence> correctSeqs;
    private Sequence errorSequence;


    protected int sortedtime=0;
    protected int firstworktime=0;
    protected int rule1worktime=0;
    protected int rule2worktime=0;
    //protected int rule3worktime=0;
    protected int rule4worktime=0;

    protected double pruntimes=0;
    public double getPrunetimes()
    {
        return pruntimes;
    }
    protected int rule1randomtime=0;
    protected int rule2randomtime=0;
    protected int rule4randomtime=0;

    public int getRule1randomtime() {
        return rule1randomtime;
    }

    public int getRule2randomtime() {
        return rule2randomtime;
    }

    public int getRule4randomtime() {
        return rule4randomtime;
    }



    public int getSortedtime() {
        return sortedtime;
    }

    public int getFirstworktime() {
        return firstworktime;
    }

    public int getRule1worktime() {
        return rule1worktime;
    }

    public int getRule2worktime() {
        return rule2worktime;
    }


//    public int getRule3worktime() {
//        return rule3worktime;
//    }
//
//    public void setRule3worktime(int rule3worktime) {
//        this.rule3worktime = rule3worktime;
//    }

    public int getRule4worktime() {
        return rule4worktime;
    }






    public BasicPatternFindingListener(Set<Sequence> correctSeqs) {
        this.sequence = new Sequence(correctSeqs);
        this.correctSeqs = correctSeqs;
        currentState = null;
        currentStateNodes = null;
        nodeId = 0;
        execResult = true;
        positionFilter = null;
        errorSequence = null;
    }


    private void initCurrentState(VM vm,Search search) {
        //System.out.println("BasicPatternFindinglistern-initCurrentState执行了");
        DistanceBasedSearch dbsearch = (DistanceBasedSearch) search;
        //从search中，获取正确的序列
        correctSeqs = dbsearch.getCorrectSeqs();
        this.sequence = new Sequence(correctSeqs);
        currentState = new SearchState(vm.getStateId(),vm.getRestorableState());
        //将当前的sequence,传入search中
        if (currentState != null) {
            saveLastState();
            dbsearch.addCurrentSequence(sequence);
        }
        currentStateNodes = new ArrayList<>();
    }

    private void saveLastState() {
        sequence = sequence.advance(currentState.getStateId(),currentState.getState(), currentStateNodes);
    }

    public Sequence getSequence() {
        return sequence;
    }

    private int getNodeId() {
        return nodeId++;
    }

    public Set<Sequence> getCorrectSeqs() {
        return correctSeqs;
    }

    public Sequence getErrorSequence() {
        return errorSequence;
    }

    public void setPositionFilter(Filter filter) {
        positionFilter = filter;
    }

    @Override
    public void searchStarted(Search search) {
        super.searchStarted(search);
        DistanceBasedSearch dbsearch = (DistanceBasedSearch) search;
        //设置search中的正确序列，为初始化中第一个正确的执行序列
        dbsearch.setCorrectSeqs(correctSeqs);
        currentStateNodes = new ArrayList<>();
    }

    @Override
    public void stateAdvanced(Search search) {

        VM vm = search.getVM();
        initCurrentState(vm,search);
    }

    @Override
    public void stateRestored(Search search) {
        super.stateRestored(search);
    }

    @Override
    public void propertyViolated(Search search) {
        super.propertyViolated(search);
        execResult = false;
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
        super.instructionExecuted(vm, currentThread, nextInstruction, executedInstruction);
        if (executedInstruction instanceof FieldInstruction) {
            FieldInstruction fins = (FieldInstruction)executedInstruction;
            if (positionFilter != null && !positionFilter.filter(fins.getFileLocation())) {
                return;
            }

//            //lhr
//            System.out.println("FieldInfo="+fins.getFieldInfo().toString());
//            int InstructionIndex=executedInstruction.getInstructionIndex();
//            int position=executedInstruction.getPosition();
//            System.out.println("InstructionIndex="+InstructionIndex+" "+position);
//            MethodInfo message=executedInstruction.getMethodInfo();
//            System.out.println(message.toString());

            FieldInfo fi = fins.getFieldInfo();
            ElementInfo ei = fins.getElementInfo(currentThread);

            String type = fins.isRead() ? "READ" : "WRITE";
            String eiString = ei == null ? "null" : ei.toString();
            String fiName = fi.getName();
            ReadWriteNode node = new ReadWriteNode(getNodeId(), eiString, fiName, type, currentThread.getName(), fins.getFileLocation());
            currentStateNodes.add(node);
        }
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
        super.choiceGeneratorAdvanced(vm, currentCG);
        if (currentCG instanceof ThreadChoiceFromSet) {

            ThreadInfo[] threads = ((ThreadChoiceFromSet)currentCG).getAllThreadChoices();
            if (threads.length == 1) {
                return;
            }
            ThreadInfo ti = (ThreadInfo)currentCG.getNextChoice();
            Instruction insn = ti.getPC();
            String type = insn.getClass().getName();
            ScheduleNode node = new ScheduleNode(getNodeId(), ti.getName(), insn.getFileLocation(), type);
            currentStateNodes.add(node);
        }
    }

    @Override
    public void searchFinished(Search search) {
        super.searchFinished(search);
        //VM vm = search.getVM();
        //initCurrentState(vm,search);
        this.correctSeqs=((DistanceBasedSearch) search).getCorrectSeqs();
        this.errorSequence = ((DistanceBasedSearch) search).getErrorSequence();

        this.firstworktime=((DistanceBasedSearch) search).getFirstworktime();
        this.rule1worktime=((DistanceBasedSearch) search).getRule1worktime();
        this.rule2worktime=((DistanceBasedSearch) search).getRule2worktime();
        //this.rule3worktime=((DistanceBasedSearch) search).getRule3worktime();
        this.rule4worktime=((DistanceBasedSearch) search).getRule4worktime();

        this.rule1randomtime=((DistanceBasedSearch) search).getRule1randomtime();
        this.rule2randomtime=((DistanceBasedSearch) search).getRule2randomtime();
        this.rule4randomtime=((DistanceBasedSearch) search).getRule4randomtime();

        this.sortedtime=((DistanceBasedSearch) search).getSortedtime();


//        this.ruleworktime=((DistanceBasedSearch) search).getRuleworktime();
//        this.sortedtime=((DistanceBasedSearch) search).getSortedtime();
        errorSequence = errorSequence.advanceToEnd(currentState.getStateId(), currentState.getState(), sequence.getNodes(), execResult);
        pruntimes=((DistanceBasedSearch) search).getPrunetimes()*1.0/this.correctSeqs.size();
    }
}