package mmcontrol.machineconnection.interfaces;

import java.util.Map;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;

/**
 * Waits for connection requests from starting metrology machines
 * and establishes connection. Just 1 active instance should be running!
 * 
 * @author Michael Zawrel
 */
public interface IMachineConnectionService {

    /**
     * Waits for connection requests of metrology machines, 
     * establishes connection and adds it to the active machine connections.
     * 
     * Channel might be e.g. a Socket (with ip & port stored in a config file)
     */
    public void startService();
    
    /**
     * Closes all active Machine connections and 
     * makes the service stop listening for new connections.
     */
    public void shutdown();
    
    /**
     * Returns a handler for using an active connection to a Machine.
     * 
     * @param machineId
     * @return communication handler
     */
    public IMachineCommunicationService getCommunicationHandleByMachineId(long machineId);

    /**
     * Adds communication handler to active communications
     * and informs the server about a change in active machines.
     * 
     * @param machineId
     * @param communication handler
     */
    public void addToActiveMachinesList(long machineId, IMachineCommunicationService communication);
    
    /**
     * Removes communication handler from active communications
     * and informs the server about a change in active machines.
     * 
     * @param machineId
     */
    public void removeFromActiveMachinesList(long machineId);

}
