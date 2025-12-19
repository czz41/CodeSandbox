public class Main{
    public static void main(String[] args) {
        Integer a = Integer.parseInt(args[0]);
        Integer b = Integer.parseInt(args[1]);
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(args[0]+"+"+args[1]+"="+(a+b));
    }
}