package mmcontrol.uicontrol.impl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcontrol.common.exceptions.MachineOperationTemporarilyForbiddenException;
import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.interfaces.uicontrol.IMachineStateUpdateService;
import mmcontrol.common.model.StatusMessage;
import mmcontrol.uicontrol.beans.MainCtrl;
import mmcontrol.uicontrol.beans.StoredMachines;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.enums.EMachineState;

/**
 *
 * @author Michael Zawrel
 */
public class MachineStateUpdateServiceImpl extends java.rmi.server.UnicastRemoteObject implements IMachineStateUpdateService {

    private static final long serialVersionUID = 1;

    MainCtrl control;
    
    public MachineStateUpdateServiceImpl() throws RemoteException {
        System.out.println("WARNING: Default constructor called and machine list thus not initialized!");
    }
    
    public MachineStateUpdateServiceImpl(MainCtrl ctrlBean) throws RemoteException {
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
        for(Long mid : this.control.getMachineMgmt().getMachines().keySet()) {
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
        if(this.control.getMachineMgmt().getMachines().containsKey(machineId)) {
            this.deactivateMachine(machineId);
        }
    }
    
    @Override
    public void informStateChange(long machineId, StatusMessage status) throws RemoteException {

        if(!this.control.getMachineMgmt().getMachines().containsKey(machineId)) {
            System.out.print("Change in machine state received but machine-id is unknown! Refreshing Machine list...");
            try {
                this.control.getMachineMgmt().pullActiveMachines();
                System.out.println("done!");
            } catch (RemoteException | NotBoundException ex) {
                System.out.println("failed!");
                return;
            }
        }

        if(this.control.getMachineMgmt().getMachines().containsKey(machineId)) {
            //Get machine object (from database or a list of active machines) and set values of its actuators/sensors
            try {
                String[] values = status.getCurrent().substring(3).split(" ");
                for (int i = 0; i < values.length; i = i + 2) {
                    this.control.getMachineMgmt().getMachines().get(machineId).getComponents()
                            .get(Integer.parseInt(values[i])).setValue(values[i + 1]);
                }
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                System.out.println("Illegal message format received by machine!");
            }
            try {
                this.control.getUserMgmt().getUserHTTPSessionObject(machineId).updateMachineValues();
            } catch (Exception ex) {}
            //System.out.println("CHANGE IN STATE OF MACHINE " + machineId + " RECEIVED VIA RMI!");
        }
        else {
            System.out.println("State update of unknown machine is ignored.");
        }
    }

    private boolean activateMachine(Long mid) throws MachineUnreachableException {
        Machine temp = this.control.getMachineMgmt().getMachines().get(mid);
                
        //TODO: when DB, don't create new Machines (only create session)
        if (temp == null) {
            temp = new Machine(mid);
            this.control.getMachineMgmt().addMachine(temp);
        }
        
        if (temp.getState() == EMachineState.INACTIVE) {
            try {
                this.control.getMachineMgmt().createMachineSession(mid);
                return true;
            } catch (MachineOperationTemporarilyForbiddenException ex) {
                Logger.getLogger(MachineStateUpdateServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return false;
    }
    
    private void deactivateMachine(Long mid) {
        this.control.getMachineMgmt().endMachineSession(mid);
    }
    
}
