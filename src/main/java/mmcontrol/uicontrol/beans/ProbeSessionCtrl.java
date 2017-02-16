/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmcontrol.uicontrol.beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import mmcontrol.uicontrol.model.Probe;
import mmcontrol.uicontrol.model.ProbeProgram;
import mmcontrol.uicontrol.model.ProbeProgramExecution;
import mmcontrol.uicontrol.model.UserMachineSession;
import mmcontrol.uicontrol.model.enums.EMachineState;
import org.icefaces.application.PushRenderer;

/**
 * The OperationController performs probing operations by a User on Machines.
 *
 * @author Michael Zawrel
 */
@ManagedBean(name="sessionCtrl")
@SessionScoped
public class ProbeSessionCtrl implements Serializable {
    
    @ManagedProperty(value="#{loginCtrl}")
    private LoginCtrl loginCtrl;
    
    private UserMachineSession activeSession = null;
    
    public ProbeSessionCtrl() {

        // on load, add this session to the session push group
        PushRenderer.addCurrentSession("session");

    }

    public void startProbeWithoutRecording() {
        long machineId = this.loginCtrl.selectedMachine.getId();
        this.loginCtrl.getUser().getCurrentSession().startUserMachineSession(new Probe(machineId));
        this.loginCtrl.getSelectedMachine().setState(EMachineState.PROBING_WITHOUT_RECORDING);
        this.activeSession = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
        System.out.println("Probe started on machine: " +machineId);
        PushRenderer.render("session");
    }
    
    public void startProbeWithRecording() {
        long machineId = this.loginCtrl.selectedMachine.getId();
        this.loginCtrl.getUser().getCurrentSession().startUserMachineSession(new ProbeProgram(machineId));
        this.loginCtrl.getSelectedMachine().setState(EMachineState.PROBING_AND_RECORDING);
        this.activeSession = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
        PushRenderer.render("session");
    }

    public void startProbeProgramExecution(ProbeProgram program) {
        long machineId = this.loginCtrl.selectedMachine.getId();
        this.loginCtrl.getUser().getCurrentSession().startUserMachineSession(new ProbeProgramExecution(machineId));
        this.loginCtrl.getSelectedMachine().setState(EMachineState.PROBING_PROGRAM_RUNNING);
        this.activeSession = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
        PushRenderer.render("session");
    }
    
    public void stopProbe() {
        this.loginCtrl.getUser().getCurrentSession().endUserMachineSession();
        this.loginCtrl.getSelectedMachine().setState(EMachineState.CONNECTED);
        this.loginCtrl.setSelectedMachine(null);
        this.activeSession = null;
        PushRenderer.render("session");
    }

    public void setLoginCtrl(LoginCtrl loginCtrl) {
        this.loginCtrl = loginCtrl;
    }

    public UserMachineSession getActiveSession() {
        return activeSession;
    }

}
