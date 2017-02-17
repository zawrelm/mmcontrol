package mmcontrol.uicontrol.interfaces;

import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.exceptions.MachineOperationTemporarilyForbiddenException;
import mmcontrol.uicontrol.model.UserMachineSession;

/**
 * This class is the interface to operate metrology machines.
 * Each UserMachineSession holds one instance of a class implementing this interface.
 * 
 * The methods of this class are the functions, an operator has to control a machine.
 * Depending on the machine type and configuration, not all methods might be implemented/available.
 *
 * @author Michael Zawrel
 */
public interface IMachineControlService {

    /* FUNCTION GROUP 1: SESSION FUNCTIONS (General functions before starting a probe) */
    //public UserMachineSession getSession(); //TODO: really necessary?
    //public void inviteObserverToSession(User user) throws MachineUnreachableException;
    //public void joinSessionAsObserver(User user);
    //public void startProbeWithoutRecording() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    //public void startProbeAndRecording() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    //public void startProbeProgram(ProbeProgram program) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    //public void stopProbe() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;

    /* FUNCTION GROUP 2: DIRECT ACTUATOR CONTROL (simple movement without a state) */
    public boolean moveXFastForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveXFastBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveXSlowForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveXSlowBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveYFastForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveYFastBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveYSlowForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveYSlowBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveZFastForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveZFastBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveZSlowForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveZSlowBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean setXFastConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean setXSlowConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean setYFastConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean setYSlowConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean setZFastConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean setZSlowConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    
    public boolean moveXToPosition(double position) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveYToPosition(double position) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean moveZToPosition(double position) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    
    public boolean measurePosition() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    
    /* FUNCTION GROUP 3: COMPLEX PROBE OPERATIONS (Operations that have a state) */
    public boolean calibrateMachine() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException; //ALL AXES SEPARATELY
    public boolean probePoint() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException; //ALL AXES SEPARATELY
    public boolean probeInnerDiameter() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean probeOuterDiameter() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean probePlane() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean probeDistancePlanePoint() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean probeDistanceParallelPlanes() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException;
    public boolean cancelOperation();
    public boolean finishOperation();

}
