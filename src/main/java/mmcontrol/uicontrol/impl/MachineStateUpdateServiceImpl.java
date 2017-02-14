package mmcontrol.uicontrol.impl;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcontrol.common.exceptions.MachineOperationTemporarilyForbiddenException;
import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.interfaces.uicontrol.IMachineStateUpdateService;
import mmcontrol.common.model.StatusMessage;
import mmcontrol.uicontrol.beans.StoredMachines;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.enums.EMachineState;

/**
 *
 * @author Michael Zawrel
 */
public class MachineStateUpdateServiceImpl extends java.rmi.server.UnicastRemoteObject implements IMachineStateUpdateService {

    private static final long serialVersionUID = 1;

    StoredMachines control;
    
    public MachineStateUpdateServiceImpl() throws RemoteException {
        System.out.println("WARNING: Default constructor called and machine list thus not initialized!");
    }
    
    public MachineStateUpdateServiceImpl(StoredMachines ctrlBean) throws RemoteException {
        this.control = ctrlBean;
    }

    @Override
    public void informActiveMachines(HashSet<Long> machineIds) throws RemoteException {
        if(machineIds != null) {
            for(Long mid : machineIds) {
                System.out.println("Activate: " +mid);
                this.activateMachine(mid);
            }
        }
        for(Long mid : this.control.getMachines().keySet()) {
            if(machineIds == null || !machineIds.contains(mid)){
                System.out.println("Deactivate: " +mid);
                this.deactivateMachine(mid);
            }
        }
        System.out.println("CHANGED LIST OF ACTIVE MACHINES RECEIVED VIA RMI!");

    }

    @Override
    public void informMachineConnected(Long machineId) throws RemoteException {
        this.activateMachine(machineId);
    }

    @Override
    public void informMachineDisconnected(Long machineId) throws RemoteException {
        if(this.control.getMachines().containsKey(machineId)) {
            this.deactivateMachine(machineId);
        }
    }
    
    @Override
    public void informStateChange(long machineId, StatusMessage status) throws RemoteException {

        //Get machine object (from database or a list of active machines) and set values of its actuators/sensors
        try {
            String[] values = status.getCurrent().substring(3).split(" ");
            for (int i = 0; i < values.length; i = i + 2) {
                if(this.control.getMachines().containsKey(machineId)) {
                    this.control.getMachines().get(machineId).getComponents()
                            .get(Integer.parseInt(values[i])).setValue(values[i + 1]);
                }
                else {
                    System.err.println("Change in machine state received but machine-id is unknown!");
                    return;
                }
            }
        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
            System.out.println("Illegal message format received by machine !");
        }

        System.out.println("CHANGE IN STATE OF MACHINE " + machineId + " RECEIVED VIA RMI!");
    }

    private boolean activateMachine(Long mid) throws MachineUnreachableException {
        Machine temp = this.control.getMachines().get(mid);
                
        //TODO: when DB, don't create new Machines (only create session)
        if (temp == null) {
            temp = new Machine(mid);
            this.control.addMachine(temp);
        }
        
        if (temp.getState() == EMachineState.INACTIVE) {
            try {
                this.control.createMachineSession(mid);
                return true;
            } catch (MachineOperationTemporarilyForbiddenException ex) {
                Logger.getLogger(MachineStateUpdateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return false;
    }
    
    private void deactivateMachine(Long mid) {
        this.control.endMachineSession(mid);
    }
    
}
