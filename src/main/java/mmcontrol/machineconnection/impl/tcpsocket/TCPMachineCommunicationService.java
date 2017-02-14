package mmcontrol.machineconnection.impl.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcontrol.common.exceptions.IncompatiblePinException;
import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;
import mmcontrol.common.interfaces.uicontrol.IMachineStateUpdateService;
import mmcontrol.common.model.StatusMessage;
import mmcontrol.machineconnection.exceptions.ServerNotFoundException;
import mmcontrol.machineconnection.interfaces.IMachineConnectionService;

/**
 *
 * @author Michael Zawrel
 */
public class TCPMachineCommunicationService extends java.rmi.server.UnicastRemoteObject implements IMachineCommunicationService, Runnable {

    private static final long serialVersionUID = 1;

    private long machineId;
    private StatusMessage status;

    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private IMachineConnectionService cs;
    
    private Registry registry;
    private String bindingNameCS; //CS = CommunicationService
    private String bindingNameSUS; //SUS = StateUpdateService
    private IMachineStateUpdateService stateUpdateService;
    
    public TCPMachineCommunicationService() throws RemoteException { }
    
    public TCPMachineCommunicationService(long machineId, Socket socket, IMachineConnectionService cs) throws IOException {

        this.machineId = machineId;
        this.status = new StatusMessage();
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.cs = cs;

    }

    @Override
    public void run() {

        java.io.InputStream is = getClass().getResourceAsStream("/registry.properties");
        if (is != null) {
            java.util.Properties props = new java.util.Properties();

            try {

                props.load(is);

                try {

                    int port = Integer.parseInt(props.getProperty("registry.port"));
                    String host = props.getProperty("registry.host");
                    this.bindingNameCS = props.getProperty("registry.communicationService");
                    this.bindingNameSUS = props.getProperty("registry.stateUpdateService");

                    registry = LocateRegistry.getRegistry(host, port);

                    registry.rebind(bindingNameCS+this.machineId, this);
                    System.out.print(bindingNameCS+this.machineId +" running!");
                    
                    try {
                        String response;
                        while ((response = this.receive()) != null) { // if the end of the stream has been reached, stop waiting for input
                            if (response.length() == 0) {
                                continue; // if there is no input to process, skip
                            }
                            String[] parts = response.split(" ");
                            if (parts[0].equals("IN")) {
                                try {
                                    this.updateSensorValues(response);
                                }
                                catch(ServerNotFoundException | IncompatiblePinException e) {
                                    //System.out.print(".");
                                }
                            }
                        }
                        this.socket.close();
                    } catch (IOException ex) {
                    }

                    UnicastRemoteObject.unexportObject(this, true);
                    registry.unbind(bindingNameCS+this.machineId);
                    
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid port in properties file.");
                } catch (NotBoundException e) {
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
        try {
            this.cs.removeFromActiveMachinesList(this.machineId);
        }
        catch(NullPointerException ex) { }
    }
    
    /*TODO: check whether pin-number is valid:
    if(!(pin >= 0 && pin < this.machine.getNumberOfControlPins())) throw new IncompatiblePinException();*/
    @Override
    public void setDigitalPin(int pin, boolean value) throws MachineUnreachableException {
        if(pin < 0) return;
        this.send("SET " + pin + " " + (value ? "1" : "0"));
    }
    
    @Override
    public void setAnalogValue(String name, double value) throws MachineUnreachableException, IncompatiblePinException {
        if(name.equals("X") || (name.equals("Y")) || (name.equals("Z"))) {
            this.send("POS " +name +" " + value);
        }
        else throw new IncompatiblePinException();
    }
    
    @Override
    public void checkConnection() throws MachineUnreachableException, RemoteException {
        this.send("NUL");
    }

    @Override
    public void shutdownMachine() {
        try {
            /* already done in run-method
            try {
                this.cs.removeFromActiveMachinesList(this.machineId);
                UnicastRemoteObject.unexportObject(this, true);
                registry.unbind(bindingNameCS+this.machineId);
            } catch (RemoteException | NotBoundException | NullPointerException ex) { }*/

            this.send("BYE");
            String response = this.receive();
            this.updateSensorValues(response);
            this.close();
        }
        catch (IOException | NullPointerException | IncompatiblePinException ex) { }
        
    }

    @Override
    public long getMachineId() {
        return this.machineId;
    }
    
    private void close() throws IOException {
	this.out.close();
	this.in.close();
	if(!this.socket.isClosed())
            this.socket.close();
        System.out.println("Socket and streams closed!");
    }

    /**
     * 
     * @param message
     * @throws MachineUnreachableException 
     */
    private void send(String message) throws MachineUnreachableException {
        //TODO: ADD SEQUENCE NUMBER TO MESSAGE? (WHERE TO GET IT FROM? SESSION, APPLICATION OR DATABASE-SCOPED?)
        this.out.println(message);
        System.out.println("SENT MESSAGE: " + message);
        if(this.out.checkError()) throw new MachineUnreachableException();
    }

    /**
     * 
     * @return message
     * @throws IOException 
     */
    private String receive() throws IOException {
        String str = this.in.readLine(); //received messages must be a single line!
	if(str == null) throw new IOException();
	return str;
    }
    
    private void updateSensorValues(String response) throws ServerNotFoundException, IncompatiblePinException {

        if(response == null) return;
        if(this.status.getCurrent() != null) this.status.setPrevious(this.status.getCurrent());
        this.status.setCurrent(response);
        try {
            if (this.status.changed()) {
                System.out.println("updateSensorValues - change=true!");
                try {
                    if (this.stateUpdateService == null) {
                        this.stateUpdateService = (IMachineStateUpdateService) registry.lookup(this.bindingNameSUS);
                    }
                    this.stateUpdateService.informStateChange(this.machineId, this.status);
                } catch (RemoteException | NotBoundException ex) {
                    throw new ServerNotFoundException();
                }
            }
        } catch (IndexOutOfBoundsException | ParseException ex) {
            throw new IncompatiblePinException();
        }
    }

}
