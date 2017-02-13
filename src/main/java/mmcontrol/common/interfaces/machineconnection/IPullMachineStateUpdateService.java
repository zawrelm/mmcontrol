package mmcontrol.common.interfaces.machineconnection;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Service for pulling information about active machines.
 * Information is sent to IMachineStateUpdateService.
 * 
 * @author Michael Zawrel
 */
public interface IPullMachineStateUpdateService extends Remote {

    /**
     * Triggers an informActiveMachines-message to IMachineStateUpdateService.
     * 
     * @throws RemoteException 
     */
    public void pullActiveMachines() throws RemoteException;
    
}
