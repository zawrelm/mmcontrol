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
    String bindingNameConS; //ConnectionService/PullService
    private static Registry registry = null; //Registry needs to be statically referenced in order to not be distributedly garbage collected!
    
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
                    this.bindingNameConS = props.getProperty("registry.connectionService");

                    sus = new MachineStateUpdateServiceImpl(main);
                    
                    try {
                        registry = LocateRegistry.getRegistry(host, port);
                        registry.rebind(this.bindingNameSUS, sus);
                        System.out.println("RMI-Registry found on host '" +host +":" +port +"\'");
                    } catch (RemoteException ex) {
                        registry = LocateRegistry.createRegistry(port);
                        registry.rebind(this.bindingNameSUS, sus);
                        System.out.println("RMI-Registry on host \'" +host +":" +port +"\' not found, created local registry");
                    }

                    this.pullActiveMachines();

                    System.out.println("JSF application started!");
                    
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid port in properties file.");
                } catch (RemoteException ex) {
                    System.err.println("Could not reach registry: " + ex.getCause().getMessage());
                    ex.printStackTrace();
                } catch (NotBoundException ex) {
                    System.err.println();
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
    
    public void pullActiveMachines() throws RemoteException, NotBoundException {
        IPullMachineStateUpdateService activeMachines = (IPullMachineStateUpdateService) registry.lookup(this.bindingNameConS);
        activeMachines.pullActiveMachines();
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
                    System.out.println("Session with machine " +machineId +" established!");
                } catch (NotBoundException | IOException ex) {
                    throw new MachineUnreachableException();
                }
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
                System.out.println("Stop probe:1");
                LoginCtrl userSession = this.main.getUserMgmt().getUserHTTPSessionObject(machineId);
                System.out.println("Stop probe:2");
                this.main.getUserMgmt().getUser(userSession.getUser().getEmail()).getCurrentSession().endUserMachineSession(); //close UMS
                System.out.println("Stop probe:3");
                this.main.getUserMgmt().getProbeSessionBean(machineId).stopProbe();
            } catch(NullPointerException | UserNotFoundException ex) {
                System.out.println("Couldn't end user session for machine with ID " +machineId 
                        + " or no user session was active! (" +ex.getLocalizedMessage() +")");
            }

            this.machines.get(machineId).endSession();
        }
        else {
            System.out.println("Machine with ID " +machineId +" tried to disconnect, but this machine is unknown.");
        }
    }
    
    //public void shutdownMachine() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException; //really necessary?
    
}
