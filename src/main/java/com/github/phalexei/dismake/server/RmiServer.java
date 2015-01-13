package com.github.phalexei.dismake.server;

import com.github.phalexei.dismake.work.Task;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * public interface RmiServerIntf
 * extends Remote
 *
 * Used to specify the methods we can call on a remote server
 */
public interface RmiServer extends Remote {

    /**
     * If a {@link com.github.phalexei.dismake.work.Task} is available, sends
     * it to the client.
     * <p>
     * Otherwise, sends a special {@link com.github.phalexei.dismake.work.Task}
     * @return a {@link com.github.phalexei.dismake.work.Task} to do
     * @throws RemoteException
     */
    public Task getTask() throws RemoteException;

    /**
     * Notify the server that doneTask is done.
     * <p>
     * This call adds any newly available task to the queue
     * @param doneTask the {@link com.github.phalexei.dismake.work.Task}
     *                 that was just finished
     * @throws RemoteException
     */
    void sendResults(Task doneTask) throws RemoteException;
}
