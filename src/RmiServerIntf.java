import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * public interface RmiServerIntf
 * extends Remote
 *
 * Used to specify the methods we can call on a remote
 * server
 */
public interface RmiServerIntf extends Remote {

    /*
     * Returns a generic String message
     */
    public String getMessage() throws RemoteException;
}
