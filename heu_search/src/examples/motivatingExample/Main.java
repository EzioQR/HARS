package motivatingExample;

public class Main {
    public static int account;
    public static class Customer implements Runnable{
        private int cost;
        public Customer(int cost)
        {
            this.cost=cost;
        }
        public void run()
        {
            int temp=account+cost;
            account=temp;
        }
    }

    public static void main(String[] args) {
        account=0;
        Thread t1=new Thread(new Customer(10));
        Thread t2=new Thread(new Customer(10));
        t1.start();
        t2.start();
        assert (account==20) :"wrong!";
    }
}
