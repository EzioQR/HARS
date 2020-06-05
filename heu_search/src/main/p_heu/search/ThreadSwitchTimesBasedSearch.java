package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.sequence.Sequence;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;


public class ThreadSwitchTimesBasedSearch extends DistanceBasedSearch{
    public int findmaxthreadswitch()
    {
        int max=-1;
        int maxid=-1;
        for (int i = 0; i <this.queue.size(); i++) {
            if (this.queue.get(i).getSwitchtimes()>max)
            {
                maxid=i;
                max=this.queue.get(i).getSwitchtimes();
            }
        }
        return maxid;
    }

    public ThreadSwitchTimesBasedSearch(Config config, VM vm) {
        super(config, vm);
    }

    @Override
    protected void sortrule() {
        if (queue.size()<2)
            return;

        sortedtime++;
        int finalindex=-1;
        for (int i = 0; i <this.queue.size() ; i++) {
            this.queue.get(i).setId(i+1);
        }
        System.out.println("*********************************");
        System.out.println("初始序列为:");
        for (int i = 0; i <this.queue.size() ; i++) {
            System.out.print(this.queue.get(i).getId()+" ");
        }
        System.out.println();

        //一轮排序
        Collections.sort(this.queue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getDistance(),o2.getDistance()));
            }
        });
        System.out.println("一轮排序完成");


        double current=Double.POSITIVE_INFINITY;
        for (int i = 0; i <queue.size() ; i++) {
            if (i==0)
                current=queue.get(i).getDistance();
            if (queue.get(i).getDistance()<current)
            {
                finalindex=i;
                break;
            }
            if (i==queue.size()-1)
                finalindex=i+1;
            System.out.print(queue.get(i).getId()+" ");
        }
        System.out.println();

        System.out.print("distance=:");
        for (int i = 0; i <queue.size() ; i++) {
            System.out.print(queue.get(i).getDistance()+" ");
        }
        System.out.println();
        String s1="";
        for (int i = finalindex; i <queue.size() ; i++) {
            s1=s1+queue.get(i).getId()+",";
        }
        if (!s1.isEmpty())
            s1=s1.substring(0,s1.length()-1);
        System.out.println("s1="+s1);
        LinkedList<Sequence> subqueue=subLinkedList(queue,finalindex);


        if (subqueue.size()<2)
        {
            ruleworktime++;
            return;
        }

        //二轮排序
        System.out.println();
        Collections.sort(subqueue, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {

                return (int) (-1 * Double.compare(o1.getSwitchtimes(),o2.getSwitchtimes()));
            }
        });
        System.out.println("二轮排序完成");

        System.out.print("二轮排序完成所有序列");
        for (int i = 0; i <subqueue.size() ; i++) {
            System.out.print(subqueue.get(i).getId()+" ");
        }
        System.out.println();

        System.out.print("ThreadSwitchTimes=:");
        for (int i = 0; i <subqueue.size() ; i++) {
            System.out.print(subqueue.get(i).getSwitchtimes()+" ");
        }
        System.out.println();

        current=Double.POSITIVE_INFINITY;
        for (int i = 0; i <subqueue.size() ; i++) {
            if (i==0)
                current=subqueue.get(i).getSwitchtimes();
            if (subqueue.get(i).getSwitchtimes()<current)
            {
                finalindex=i;
                break;
            }
            if (i==subqueue.size()-1)
                finalindex=i+1;
            System.out.print(subqueue.get(i).getId()+" ");
        }
        System.out.println();

        String s2="";
        for (int i = finalindex; i <subqueue.size() ; i++) {
            s2=s2+subqueue.get(i).getId()+",";
        }
        if (!s2.isEmpty())
            s2=s2.substring(0,s2.length()-1);
        System.out.println("s2="+s2);
        subqueue=subLinkedList(subqueue,finalindex);


        String base=findorder(subqueue);
        String total=base+","+s2+","+s1;
        System.out.println("total2="+total);
        CreateQueueById(total);
        if (subqueue.size()<2)
        {
            ruleworktime++;
            return;
        }
//
//        //三轮排序
//        System.out.println();
//        Collections.sort(subqueue, new Comparator<Sequence>() {
//            @Override
//            public int compare(Sequence o1, Sequence o2) {
//
//                return (int) (-1 * Double.compare(o1.getWritetimes(),o2.getWritetimes()));
//            }
//        });
//        System.out.println("三轮排序完成");
//
//        System.out.print("三轮排序完成所有序列");
//        for (int i = 0; i <subqueue.size() ; i++) {
//            System.out.print(subqueue.get(i).getId()+" ");
//        }
//        System.out.println();
//
//        System.out.print("writetimes=:");
//        for (int i = 0; i <subqueue.size() ; i++) {
//            System.out.print(subqueue.get(i).getWritetimes()+" ");
//        }
//        System.out.println();
//
//        current=Double.POSITIVE_INFINITY;
//        for (int i = 0; i <subqueue.size() ; i++) {
//            if (i==0)
//                current=subqueue.get(i).getWritetimes();
//            if (subqueue.get(i).getWritetimes()<current)
//            {
//                finalindex=i;
//                break;
//            }
//            if (finalindex==subqueue.size()-1)
//                finalindex=i+1;
//            System.out.print(subqueue.get(i).getId()+" ");
//        }
//        System.out.println();
//
//
//        String s3="";
//        for (int i = finalindex; i <subqueue.size() ; i++) {
//            s3=s3+subqueue.get(i).getId()+",";
//        }
//        if (!s3.isEmpty())
//            s3=s3.substring(0,s3.length()-1);
//        System.out.println("s3="+s3);
//        subqueue=subLinkedList(subqueue,finalindex);
//
//        base=findorder(subqueue);
//        total=base+","+s3+","+s2+","+s1;
//        System.out.println("total3="+total);
//        CreateQueueById(total);

//        System.out.println("最后的次序为:");
//        for (int i = 0; i <queue.size(); i++) {
//            System.out.print(queue.get(i).getId()+" ");
//        }
//        System.out.println();
//
//        System.out.println("*********************************");
    }
}

