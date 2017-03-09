package mmcontrol.machineconnection.impl.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;
import mmcontrol.common.interfaces.machineconnection.IPullMachineStateUpdateService;
import mmcontrol.common.interfaces.uicontrol.IMachineStateUpdateService;
import mmcontrol.machineconnection.exceptions.ConnectionHandshakeFailedException;
import mmcontrol.machineconnection.interfaces.IMachineConnectionService;
import mmcontrol.machineconnection.model.enums.EConnectionEvent;

/**
 * Waits for connection requests from starting metrology machines
 * and establishes connection. Just 1 active instance should be running!
 * 
 * @author Michael Zawrel
 */
public class TCPMachineConnectionService extends java.rmi.server.UnicastRemoteObject implements IMachineConnectionService, IPullMachineStateUpdateService, Runnable {

    private static final long serialVersionUID = 1;
    private static Registry registry = null; //Registry needs to be statically referenced in order to not be distributedly garbage collected!
    
    private final int servicePort;
    private Map<Long, IMachineCommunicationService> activeMachines;

    private static TCPMachineConnectionService connectionService = null;
    private static IMachineStateUpdateService stateUpdateService = null;
    private String bindingNameSUS;
    
    private ExecutorService pool;
    private ServerSocket serverSocket;

    private boolean running;
    
    public TCPMachineConnectionService() throws RemoteException {
        this.servicePort = 6881;  //TODO: read servicePort from configuration file
        this.activeMachines = Collections.synchronizedMap(new HashMap<Long, IMachineCommunicationService>());
        this.bindingNameSUS = null;
    }

    @Override
    public void run() {
            this.startService();
    }

    @Override
    public void startService() {

        String bindingName = null;

        try {
            pool = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(servicePort);
            this.running = true;

            java.io.InputStream is = getClass().getResourceAsStream("/registry.properties");
            if (is != null) {
                java.util.Properties props = new java.util.Properties();

                try {
                    props.load(is);

                    try {

                        int regPort = Integer.parseInt(props.getProperty("registry.port"));
                        String host = props.getProperty("registry.host");
                        bindingName = props.getProperty("registry.connectionService");
                        this.bindingNameSUS = props.getProperty("registry.stateUpdateService");
                        
                        try {
                            registry = LocateRegistry.createRegistry(regPort);
                        } catch (RemoteException ex) {
                            registry = LocateRegistry.getRegistry(host, regPort);
                        }
                        connectionService = this;
                        registry.rebind(bindingName, connectionService);
                        System.out.println("Waiting for connection...");
                        
                        while (running) {
                            try {
                                Socket socket = serverSocket.accept();
                                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                                System.out.print("A Machine is trying to connect...");
                                out.println("WHO");
                                if (out.checkError()) {
                                    System.out.println("ERROR: Machine unreachable!");
                                    throw new ConnectionHandshakeFailedException();
                                }

                                String response = in.readLine();
                                if (response == null) {
                                    System.out.println("ERROR: No response!");
                                    throw new ConnectionHandshakeFailedException();
                                }

                                    String[] parts = response.split(" ");
                                    if (parts.length == 2 && parts[0].equals("ID")) {

                                        Long id = Long.parseLong(parts[1]);
                                        System.out.print("ID " + id + " received...");

                                        if (this.activeMachines.containsKey(id)) {
                                            try {
                                                this.activeMachines.get(id).checkConnection();
                                                System.out.println("ERROR: Machine with ID " + id + " is already connected!");
                                                throw new ConnectionHandshakeFailedException();
                                            } catch(MachineUnreachableException ex) {
                                                this.activeMachines.get(id).shutdownMachine();
                                                //TODO: remove thread from executorservice-pool?
                                                this.removeFromActiveMachinesList(id);
                                                System.out.println("Machine with ID " + id + " is now inactive!");
                                            }
                                        }

                                        TCPMachineCommunicationService communication = new TCPMachineCommunicationService(id, socket, this);
                                        pool.execute(communication);
                                        System.out.println("Machine with ID " + id + " is now active!");
                                        this.addToActiveMachinesList(id, communication);
                                    } else {
                                        System.out.println("ERROR: Unknown response format!");
                                    }
                            } catch (ConnectionHandshakeFailedException ex) {
                                System.out.println("ERROR: Handshake failed, machine not connected!");
                            } catch (IOException ex) {
                                System.out.println("ERROR: Interruption while connecting machine!");
                            } catch (Exception ex) {
                                System.out.println("ERROR: Unknown response format!");
                            }
                        }

                    } catch (NumberFormatException ex) {
                        System.err.println("Invalid port in properties file.");
                    } catch (RemoteException ex) {
                        System.err.println("Could not reach registry: " + ex.getCause().getMessage());
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

        } catch (IOException ex) {
            pool.shutdown();
        } finally {
            try {
                UnicastRemoteObject.unexportObject(this, true);
                registry.unbind(bindingName);
            } catch (NullPointerException | RemoteException | NotBoundException ex) { }
        }
        System.out.println("The service has been shut down successfully!");
    }
    
    @Override
    public void shutdown() {
		
	this.running = false;
	if(this.serverSocket != null) try {
            this.serverSocket.close();
        } catch (IOException ex) { }
	
        for(IMachineCommunicationService comm : this.activeMachines.values()) {
            try {
                comm.shutdownMachine();
            } catch (NullPointerException | RemoteException ex) { }
        }
        pool.shutdown();
        
/*	Iterator<TCPCommunicationTest> it = this.activeMachines.values().iterator();
	while(it.hasNext()) {
            it.next().shutdownMachine();
            it.remove();
	}*/
    }

    @Override
    public void addToActiveMachinesList(long machineId, IMachineCommunicationService communication) {
        this.activeMachines.put(machineId, communication);
        
        try {
            this.informActiveMachines(EConnectionEvent.MACHINE_CONNECTED, machineId);
        } catch (NotBoundException | IOException | NumberFormatException ex) {
            System.err.println("Server unreachable! Not informed about activation of machine " + machineId + "!");
            return;
        }

        System.out.println("Server informed about activation of machine " + machineId + "!");
    }

    @Override
    public void removeFromActiveMachinesList(long machineId) {
        this.activeMachines.remove(machineId);
        
        try {
            this.informActiveMachines(EConnectionEvent.MACHINE_DISCONNECTED, machineId);
        } catch (NotBoundException | IOException | NumberFormatException ex) {
            System.err.println("Server unreachable! Not informed about shutdown of machine " + machineId + "!");
            return;
        }

        System.out.println("Server informed about shutdown of machine " +machineId +"!");
    }
    
    @Override
    public IMachineCommunicationService getCommunicationHandleByMachineId(long machineId) {
        return this.activeMachines.get(machineId);
    }

    @Override
    public void pullActiveMachines() throws RemoteException {
        try {
            if(!this.informActiveMachines(EConnectionEvent.SERVER_PULL, 0))
                this.informActiveMachines(EConnectionEvent.SERVER_PULL, 0); //retry once if server was unreachable
        } catch (NotBoundException | IOException| NumberFormatException ex) {
            System.err.println("Server unreachable! Not informed about active machines!");
            return;
        }
    }
    
    /**
     * Connect to server via RMI and send information about connected machines.
     * 
     * @param event
     * @param machineId (not necessary if list of connected machines requested)
     * @throws IOException
     * @throws NotBoundException
     * @throws NumberFormatException 
     */
    private boolean informActiveMachines(EConnectionEvent event, long machineId) 
            throws IOException, NotBoundException, NumberFormatException {
        
        if(stateUpdateService == null) {
            System.out.print("SUS: StateUpdateService-Lookup...");
            stateUpdateService = (IMachineStateUpdateService) registry.lookup(this.bindingNameSUS);
            System.out.println("SUS found!");
        }

        try {
            switch (event) {
                case MACHINE_CONNECTED:
                    System.out.print("SUS: inform connect...");
                    for(int i = 0; i < 10; i++) { // Retry until CommunicationService is registered
                        try {
                            stateUpdateService.informMachineConnected(machineId);
                            i = 10;
                        } catch (RemoteException e) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) { }
                            System.out.print("retry...");
                        }
                    }
                    break;
                case MACHINE_DISCONNECTED:
                    System.out.print("SUS: inform disconnect...");
                    stateUpdateService.informMachineDisconnected(machineId);
                    break;
                default:
                    System.out.print("SUS: inform active machines...");
                    if (this.activeMachines == null || this.activeMachines.isEmpty()) {
                        stateUpdateService.informActiveMachines(null);
                    } else {
                        stateUpdateService.informActiveMachines(new HashSet(this.activeMachines.keySet()));
                    }
                    break;
            }
            System.out.println(" ...informed!");
            return true;
            
        } catch (NoSuchObjectException e) {
            stateUpdateService = null;
            System.out.println("SUS NOT FOUND! Object has been reset!");
            return false;
        }
    
    }

}
