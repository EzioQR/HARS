package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.tool.Run;
import gov.nasa.jpf.vm.VM;
import org.omg.SendingContext.RunTime;
import p_heu.entity.ReadWriteNode;
import p_heu.entity.ScheduleNode;
import p_heu.entity.sequence.Sequence;

import java.util.*;

public class IntelligentBasedSearch extends DistanceBasedSearch{

    public IntelligentBasedSearch(Config config, VM vm) {
        super(config, vm);
    }
    public LinkedList h1()
    {
        LinkedList<Sequence> tempqueue=new LinkedList<>();
        double max=queue.get(0).getDistance();
        for (int i =0; i <this.queue.size(); i++) {
            if (this.queue.get(i).getDistance()==max)
                tempqueue.add(this.queue.get(i).copy());
            else if (this.queue.get(i).getDistance()>max)
                throw new RuntimeException("第一轮排序没能成功");
        }

        Collections.sort(tempqueue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getSwitchtimes(),o2.getSwitchtimes()));
            }
        });
        return tempqueue;
    }

    public LinkedList h2()
    {
        LinkedList<Sequence> tempqueue=new LinkedList<>();
        double max=queue.get(0).getDistance();
        for (int i = 0; i <this.queue.size(); i++) {
            if (this.queue.get(i).getDistance()==max)
                tempqueue.add(this.queue.get(i).copy());
            else if (this.queue.get(i).getDistance()>max)
                throw new RuntimeException("第一轮排序没能成功");
        }

        Collections.sort(tempqueue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getWritetimes(),o2.getWritetimes()));
            }
        });
        return tempqueue;
    }

    public LinkedList h3()
    {
        LinkedList<Sequence> tempqueue=new LinkedList<>();
        double max=queue.get(0).getDistance();
        for (int i = 0; i <this.queue.size(); i++) {
            if (this.queue.get(i).getDistance()==max)
                tempqueue.add(this.queue.get(i).copy());
            else if (this.queue.get(i).getDistance()>max)
                throw new RuntimeException("第一轮排序没能成功");
        }

        Collections.sort(tempqueue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getRWswitchtimes(),o2.getRWswitchtimes()));
            }
        });
        return tempqueue;
    }

    public LinkedList h4()
    {
        LinkedList<Sequence> tempqueue=new LinkedList<>();
        double max=queue.get(0).getDistance();
        for (int i =0; i <this.queue.size(); i++) {
            if (this.queue.get(i).getDistance()==max)
                tempqueue.add(this.queue.get(i).copy());
            else if (this.queue.get(i).getDistance()>max)
                throw new RuntimeException("第一轮排序没能成功");
        }

        Collections.sort(tempqueue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getsharedvarnumbers(),o2.getsharedvarnumbers()));
            }
        });
        return tempqueue;
    }

    public double checkSortIsValid(LinkedList<Sequence> sequences,String feature)
    {
        double first=-1;double second=-1;

        if (feature.equals("ThreadSwitch"))
        {
            first=sequences.get(0).getSwitchtimes();
            second=sequences.get(1).getSwitchtimes();
        }
        else if (feature.equals("RWSwitch"))
        {
            first=sequences.get(0).getRWswitchtimes();
            second=sequences.get(1).getRWswitchtimes();
        }
        else if (feature.equals("SharedVariable"))
        {
            first=sequences.get(0).getsharedvarnumbers();
            second=sequences.get(1).getsharedvarnumbers();
        }
        else if (feature.equals("Writetime"))
        {
            first=sequences.get(0).getWritetimes();
            second=sequences.get(1).getWritetimes();
        }


        if (first==-1 && second==-1)
            throw new RuntimeException("比较之前没成功赋值");

        else if (first<second)
            throw new RuntimeException("排序失效");

        double degree=-1;
        if(first==second)
            degree=1;
        else
            degree=(1+first)*1.0/(1+second);
        return degree;
    }


//    public void updateQueue(LinkedList<Sequence> tempqueue)
//    {
//        for (int i = 0; i <tempqueue.size(); i++) {
//            this.queue.set(i,tempqueue.get(i).copy());
//        }
//    }
    public void updateQueue2(LinkedList<Sequence> tempqueue)
    {
        String index=tempqueue.getFirst().getIndex();
        int s=-1;
        for (int i = 0; i <queue.size(); i++) {
            if (queue.get(i).getIndex().equals(index))
            {
                s=i;
                break;
            }
        }
        Sequence temp=queue.get(s).copy();
        for (int i =s; i >=1; i--) {
            queue.set(i,queue.get(i-1));
        }
        queue.set(0,temp.copy());
    }


    @Override
    protected void sortrule() {

        if(queue.size()<2)
            return;
        for (int i = 0; i <this.queue.size() ; i++) {
            this.queue.get(i).setId(i+1);
        }


        Collections.sort(this.queue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getDistance(),o2.getDistance()));
            }
        });

        //如果第一轮排序有效直接返回
        if (this.queue.get(0).getDistance()>this.queue.get(1).getDistance())
        {
            this.firstworktime++;
            return;
        }

        LinkedList<Sequence> subqueue1=h1();double degree1=checkSortIsValid(subqueue1,"ThreadSwitch");
        LinkedList<Sequence> subqueue2=h2();double degree2=checkSortIsValid(subqueue2,"Writetime");
        LinkedList<Sequence> subqueue4=h4();double degree4=checkSortIsValid(subqueue4,"SharedVariable");


        System.out.println("**********************");
        System.out.println("degree1="+degree1+" degree2="+degree2+" degree4="+degree4);
        System.out.print("ARSpattern ");
        for (int i = 0; i <queue.size(); i++) {
            System.out.print(queue.get(i).getId()+"号("+queue.get(i).getDistance()+") ");
        }
        System.out.println();

        System.out.print("rule1 ");
        for (int i = 0; i <queue.size(); i++) {
            System.out.print(queue.get(i).getId()+"号("+queue.get(i).getSwitchtimes()+") ");
        }
        System.out.println();

        System.out.print("rule2 ");
        for (int i = 0; i <queue.size(); i++) {
            System.out.print(queue.get(i).getId()+"号("+queue.get(i).getWritetimes()+") ");
        }
        System.out.println();


        System.out.print("rule4 ");
        for (int i = 0; i <queue.size(); i++) {
            System.out.print(queue.get(i).getId()+"号("+queue.get(i).getsharedvarnumbers()+") ");
        }
        System.out.println();


        Map<String,LinkedList<Sequence>> map = new HashMap<String,LinkedList<Sequence>>();
        map.put("ThreadSwitch", subqueue1);
        map.put("Writetime", subqueue2);
        map.put("SharedVariable", subqueue4);


        LinkedList<Sequence> finalresult=null;

        double max=degree1;
        if (degree2>max)
            max=degree2;
        if(degree4>max)
            max=degree4;

        List<String> subqueues=new ArrayList<>();
        if (degree1==max)
            subqueues.add("ThreadSwitch");
        if (degree2==max)
            subqueues.add("Writetime");
        if (degree4==max)
            subqueues.add("SharedVariable");

        if (subqueues.size()==1)
        {
            finalresult=map.get(subqueues.get(0));
            System.out.println("选中了"+subqueues.get(0));

            if (subqueues.get(0).equals("ThreadSwitch"))
                rule1worktime++;
            else if (subqueues.get(0).equals("Writetime"))
                rule2worktime++;
            else if (subqueues.get(0).equals("SharedVariable"))
                rule4worktime++;

            updateQueue2(finalresult);
        }
        else {
            //随机选一种作为第二轮排序的结果
            int index = (int) (Math.random() * subqueues.size());
            //System.out.println("选中了" + subqueues.get(index));

            if (subqueues.get(0).equals("ThreadSwitch"))
                rule1randomtime++;
            else if (subqueues.get(0).equals("Writetime"))
                rule2randomtime++;
            else if (subqueues.get(0).equals("SharedVariable"))
                rule4randomtime++;

            finalresult = map.get(subqueues.get(index));
            //       Collections.shuffle(finalresult);
            updateQueue2(finalresult);
            //      Collections.shuffle(queue);
        }

//        System.out.print("ARSpattern ");
//        for (int i = 0; i <queue.size(); i++) {
//            System.out.print(queue.get(i).getId()+"号("+queue.get(i).getDistance()+") ");
//        }
//        System.out.println();
//
//        System.out.println("**********************\n");
        return;
//        //根据相似度选择
//        Map<String,Double> cosmap = new HashMap<String,Double>();
//        for (int i = 0; i <subqueues.size(); i++) {
//            String feature=subqueues.get(i);
//            cosmap.put(feature,cos(map.get(subqueues.get(i)),feature));
//        }
//
//        Iterator map1it=cosmap.entrySet().iterator();
//        double min=-1;
//        String minfeature="";
//        while(map1it.hasNext())
//        {
//            Map.Entry<String,Double> entry=(Map.Entry<String,Double>) map1it.next();
//            if (min<entry.getValue())
//            {
//                min=entry.getValue();
//                minfeature=entry.getKey();
//            }
//        }
//
//        updateQueue(map.get(minfeature));
//        if (subqueues.get(0).equals("ThreadSwitch"))
//            rule1worktime++;
//        else if (subqueues.get(0).equals("Writetime"))
//            rule2worktime++;
//        else if (subqueues.get(0).equals("RWSwitch"))
//            rule3worktime++;
//        else if (subqueues.get(0).equals("SharedVariable"))
//            rule4worktime++;
    }
}
