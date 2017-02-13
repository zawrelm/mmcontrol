package mmcontrol.uicontrol.beans;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import mmcontrol.common.exceptions.MachineOperationTemporarilyForbiddenException;
import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;
import mmcontrol.common.interfaces.machineconnection.IPullMachineStateUpdateService;
import mmcontrol.common.interfaces.uicontrol.IMachineStateUpdateService;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
import mmcontrol.uicontrol.impl.MachineStateUpdateServiceImpl;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.User;
import mmcontrol.uicontrol.model.enums.EMachineState;
import org.icefaces.application.PushRenderer;

/**
 * This class stores all the registered Machines and manages MachineSessions.
 * 
 * @author Michael Zawrel
 */
public class StoredMachines {
    
    private Map<Long, Machine> machines;    //TODO: store list of all existing machines in database and only update machineState
    
    private static IMachineStateUpdateService sus = null;
    
    String bindingNameSUS;  //StateUpdateService
    String bindingNameCS;   //CommunicationService
    private static Registry registry;
    
    MainCtrl main;
    
    public StoredMachines(MainCtrl main) throws RemoteException {

        this.machines = new HashMap<>();

        this.main = main;
    }

    public void onStartup()
    {
        System.out.println("JSF application starting!");
        
        //TODO: read list of existing machines from database
        
        java.io.InputStream is = getClass().getResourceAsStream("/registry.properties");
        if (is != null) {
            java.util.Properties props = new java.util.Properties();

            try {
                props.load(is);

                try {
                    int port = Integer.parseInt(props.getProperty("registry.port"));
                    String host = props.getProperty("registry.host");
                    this.bindingNameSUS = props.getProperty("registry.stateUpdateService");
                    this.bindingNameCS = props.getProperty("registry.communicationService");
                    String bindingNameConS = props.getProperty("registry.connectionService");

                    try {
                        registry = LocateRegistry.createRegistry(port);
                    } catch (RemoteException ex) {
                        registry = LocateRegistry.getRegistry(host, port);
                    }

                    sus = new MachineStateUpdateServiceImpl(this);
                    registry.rebind(this.bindingNameSUS, sus);
                    
                    IPullMachineStateUpdateService activeMachines = (IPullMachineStateUpdateService) registry.lookup(bindingNameConS);
                    activeMachines.pullActiveMachines();

                    System.out.println("JSF application started!");
                    
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid port in properties file.");
                } catch (RemoteException ex) {
                    System.err.println("Could not reach registry: " + ex.getCause().getMessage());
                    ex.printStackTrace();
                } catch (NotBoundException ex) {
                    Logger.getLogger(StoredMachines.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                System.err.println("Could not read properties file.");
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        } else {
            System.err.println("Properties file not found!");
        }
        
    }

    public void onShutdown() {
        try {
            UnicastRemoteObject.unexportObject(sus, true);
            registry.unbind(this.bindingNameSUS);
        } catch (RemoteException | NotBoundException ex) {
        }
        System.out.println("JSF application destroyed!");
    }

    public Map<Long, Machine> getMachines() {
        return machines;
    }

    public void addMachine(Machine machine) {
        this.machines.put(machine.getId(), machine);
    }
    
    public List<Machine> getListOfConnectedMachines() {
        ArrayList<Machine> cm = new ArrayList<>();
        for(Machine m : this.machines.values()) {
            if(m.getState() == EMachineState.CONNECTED) {
                cm.add(m);
            }
        }
        return cm;
    }
    
    /**
     * Creates a MachineSession for a Machine in the list. 
     * This method is called when the ConnectionService informs the Server 
     * via RMI that the Machine with the given machineId has been activated.
     * 
     * A MachineSession is only started if the machineId is known and no session running already
     * 
     * @param machineId 
     * @throws mmcontrol.common.exceptions.MachineUnreachableException 
     * @throws mmcontrol.common.exceptions.MachineOperationTemporarilyForbiddenException 
     */
    public void createMachineSession(long machineId) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if (this.machines.containsKey(machineId)) {
            if (this.machines.get(machineId).getState() == EMachineState.INACTIVE) { //if no session is running
                try {
                    String names = "NAMES BOUND IN REGISTRY:";
                    for(String name : registry.list()) {
                        names += "\n\t- " +name;
                    }
                    System.out.println(names);
                    
                    this.machines.get(machineId).startSession((IMachineCommunicationService) registry.lookup(this.bindingNameCS + machineId));
                    System.out.println("so2");
                } catch (NotBoundException | IOException ex) {
                    throw new MachineUnreachableException();
                }
                PushRenderer.render("machines");
                return;
            }
        }
        System.out.println("Error at creating session for machine with ID " + machineId);
        throw new MachineOperationTemporarilyForbiddenException();
    }
    
    /**
     * Ends a MachineSession for a Machine in the list.
     * This method is called when the ConnectionService informs the Server 
     * via RMI that the Machine with the given machineId has been deactivated.
         * 
     * @param machineId 
     */
    public void endMachineSession(long machineId) {
        if(this.machines.containsKey(machineId)) { //if machine exists
            try {
                User user = this.main.getUserMgmt().getOperator(machineId); //get operator of current UserMachineSession
                this.main.getUserMgmt().getUser(user.getEmail()).getCurrentSession().endUserMachineSession(); //close UMS
            } catch(NullPointerException | UserNotFoundException ex) {}

            this.machines.get(machineId).endSession();
            PushRenderer.render("machines");
        }
        else {
            System.out.println("Error at ending session for machine with ID " +machineId);
        }
    }
    
    //public void shutdownMachine() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException; //really necessary?
    
}
