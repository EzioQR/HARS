package p_heu.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.VM;
import p_heu.entity.sequence.Sequence;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class PatternDistanceBasedSearch extends DistanceBasedSearch {


    public PatternDistanceBasedSearch(Config config, VM vm) {
        super(config, vm);
    }

    @Override
    protected void sortrule() {

        sortedtime++;
        int finalindex=-1;
        System.out.println("*********************************");
        System.out.println("初始序列为:");
        for (int i = 0; i <this.queue.size() ; i++) {
            this.queue.get(i).setId(i+1);
        }
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
        System.out.println("************");
        System.out.println("一轮排序完成");

        System.out.print("PatternDistance ");
        for (int i = 0; i <queue.size(); i++) {
            System.out.print(queue.get(i).getId()+"号("+queue.get(i).getDistance()+") ");
        }
        System.out.println();
        System.out.println("************");

        if (this.queue.size()>1&&this.queue.get(0).getDistance()>this.queue.get(1).getDistance())
            ruleworktime++;

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
