package p_heu.entity.sequence;

import p_heu.entity.Node;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.pattern.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: ars-programs
 * @description: delete many unecessary information about trace
 * @author: ry
 * @create: 2020-01-14 08:56
 */
public class SimpleSequence {
    private List<String> threadID;
    public List<String> threadlocation;
    private List<String> Code;


    private Set<Pattern> patterns;
    private boolean result;

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
    public List<String> getThreadlocation() {
        return threadlocation;
    }
    public void setThreadlocation(List<String> threadlocation) {
        this.threadlocation = threadlocation;
    }

    public List<String> getCode() {
        return Code;
    }
    public void setCode(List<String> code) {
        Code = code;
    }

    public List<String> getThreadID() {
        return threadID;
    }
    public void setThreadID(List<String> threadID) {
        this.threadID = threadID;
    }


    public SimpleSequence()
    {
        this.patterns=new HashSet<>();
        this.threadID=new ArrayList<>();
        this.Code=new ArrayList<>();
        this.threadlocation=new ArrayList<>();
    }

    public SimpleSequence(List<String> threadID,List<String> code)
    {
        this.threadID=threadID;
        this.Code=code;
    }

    public SimpleSequence copy()
    {
        SimpleSequence newseq=new SimpleSequence();
        newseq.setCode(this.getCode());
        newseq.setThreadID(this.getThreadID());
        newseq.setThreadlocation(this.getThreadlocation());
        newseq.setResult(this.getResult());
        newseq.setPatterns(this.patterns);
        return newseq;
    }

    public void ConvertSequenceToSimpleSequence(Sequence sequence)
    {

        for (Pattern pattern:sequence.getPatterns())
        {
            this.patterns.add(pattern.copy());
        }

        List<Node> nodes=sequence.getNodes();
        this.result=sequence.getResult();

        for (int i = 0; i <nodes.size(); i++) {
            if (nodes.get(i) instanceof ReadWriteNode )
            {

                ReadWriteNode currentnode=(ReadWriteNode)nodes.get(i);
                if (currentnode.getPosition().indexOf("java/")!=-1||currentnode.getPosition().indexOf("sun/")!=-1)
                    continue;
                threadID.add(currentnode.getThread());
                Code.add(currentnode.getPosition());
            }
        }
        HashSet<String> threadnames=new HashSet<>();
        for (int i = 0; i <threadID.size(); i++) {
            threadnames.add(threadID.get(i));
        }
        for (String threadname:threadnames) {
            String location="";
            for (int i = 0; i < this.Code.size(); i++) {
                if (this.threadID.get(i).equals(threadname))
                    location+=i+",";
            }
            this.threadlocation.add(location.substring(0,location.length()-1));
        }
    }

    public boolean isSame(SimpleSequence newsequence)
    {
        if (newsequence.getResult()!=this.getResult())
            return false;

        if (newsequence.getCode().size()!=this.getCode().size())
            return false;

        if (newsequence.getThreadlocation().size()!=this.threadlocation.size())
            return false;

        for (int i = 0; i < newsequence.getCode().size(); i++) {
            if (!newsequence.getCode().get(i).equals(this.getCode().get(i)))
                return false;
        }

        for(String s1:this.threadlocation)
        {
            boolean flag=false;
            for(String s2:this.threadlocation)
            {
                if (s1.equals(s2))
                    flag=true;
            }
            if (!flag)
                return false;
        }

        return true;
    }

    public Set<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<Pattern> patterns) {
        this.patterns = patterns;
    }
    public void print()
    {
        for (int i = 0; i <threadID.size(); i++) {
            System.out.println(threadID.get(i)+" "+Code.get(i));
        }
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
}
