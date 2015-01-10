import java.rmi.Naming;

public class RmiClient {
    public static void main(String args[]) throws Exception {
        RmiServerIntf obj = (RmiServerIntf)Naming.lookup("//localhost/RmiServer");
        for (int i = 0; i < 10; i++)
            System.out.println(obj.getMessage());
    }
}
