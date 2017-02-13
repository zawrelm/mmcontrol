package mmcontrol.common.interfaces.machineconnection;

import java.rmi.Remote;
import java.rmi.RemoteException;
import mmcontrol.common.exceptions.IncompatiblePinException;
import mmcontrol.common.exceptions.MachineUnreachableException;

/**
 * Implementation of the protocol between Server and Controller of Machine.
 * 
 * @author Michael Zawrel
 */
public interface IMachineCommunicationService extends Remote {   //Implementations might implement Runnable
    
    public void setDigitalPin(int pin, boolean value) throws MachineUnreachableException, IncompatiblePinException, RemoteException;

    public void setAnalogValue(String name, double value) throws MachineUnreachableException, IncompatiblePinException, RemoteException;
    
    public void checkConnection() throws MachineUnreachableException, RemoteException;
    
    public void shutdownMachine() throws RemoteException;

    public long getMachineId() throws RemoteException;
    
    /* --> push to callback interface at changes instead of those methods
    public boolean getDigitalSensorValue(int pin) throws MachineUnreachableException, IncompatiblePinException;
    public double getAnalogSensorValue(int pin) throws MachineUnreachableException, IncompatiblePinException;
    public String[] getSensorData() throws MachineUnreachableException;*/
    
}
