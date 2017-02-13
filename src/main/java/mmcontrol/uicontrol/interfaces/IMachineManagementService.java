package mmcontrol.uicontrol.interfaces;

import java.util.Collection;
import mmcontrol.uicontrol.exceptions.MachineUnknownException;
import mmcontrol.uicontrol.model.MachineComponent;
import mmcontrol.uicontrol.model.Machine;

/**
 * This class provides the interface point for managing Machines.
 * Implementations of the MachineManagementService access the persistent storage.
 *
 * @author Michael Zawrel
 */
public interface IMachineManagementService {
    
    /**
     * Returns those machines that have a set state indicating that they are active
     * @return 
     */
    public Collection<Machine> getActiveMachines();
    
    /**
     * 
     * 
     * @param machineId
     * @return 
     */
    public Collection<MachineComponent> getMachineComponents(long machineId);

    /**
     * Sets the machine state to the value given by the parameter
     * @param machineId
     * @param newState
     */
    public void setMachineState(long machineId, int newState);
    
    /**
     * Create a new machine
     * @param machine
     */
    public void createNewMachine(Machine machine);
    
    /**
     * 
     * 
     * @param machineId
     * @return 
     * @throws mmcontrol.uicontrol.exceptions.MachineUnknownException 
     */
    public Machine getMachineById(long machineId) throws MachineUnknownException;
    
    /**
     * 
     * 
     * @param machineName
     * @return 
     * @throws mmcontrol.uicontrol.exceptions.MachineUnknownException 
     */
    public Machine getMachineByName(String machineName) throws MachineUnknownException;

}
