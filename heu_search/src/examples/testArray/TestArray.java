package testArray;


public class TestArray {

    static int length = 20;
    static int[] arr = new int[length];


    public static void main(String[] args) {

        for (int i = 0; i < length; i++)
            arr[i] = 0;

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < length; i++) {
                    System.out.println("arr[" + i + "]=" + arr[i]);
                    if (arr[i] == 0 && i > 0)
                        throw new RuntimeException("error");
                }


            }


        });
        t2.start();

        for (int i = 0; i < length; i++) {
            arr[i] = i;
        }

        //b = y;
        //int c = y;

        try {
            t2.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}