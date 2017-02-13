package mmcontrol.common.interfaces.uicontrol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import mmcontrol.common.model.StatusMessage;

/**
 *
 * @author Michael Zawrel
 */
public interface IMachineStateUpdateService extends Remote {
    
    /**
     * Inform about the active metrology machines.
     * 
     * @param machineIds
     * @throws java.rmi.RemoteException 
     */
    public void informActiveMachines(HashSet<Long> machineIds) throws RemoteException;
    
    /**
     * Inform that a machine got active.
     * 
     * @param machineId
     * @throws java.rmi.RemoteException 
     */
    public void informMachineConnected(Long machineId) throws RemoteException;
    
    /**
     * Inform that a machine got inactive.
     * 
     * @param machineId
     * @throws java.rmi.RemoteException 
     */
    public void informMachineDisconnected(Long machineId) throws RemoteException;
    
    /**
     * Inform about the current state of a metrology machine
     * when machine status changes.
     * 
     * @param machineId
     * @param status 
     * @throws java.rmi.RemoteException 
     */
    public void informStateChange(long machineId, StatusMessage status) throws RemoteException;
    
}
