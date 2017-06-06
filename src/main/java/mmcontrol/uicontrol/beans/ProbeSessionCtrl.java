/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mmcontrol.uicontrol.beans;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
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
@ViewScoped
public class ProbeSessionCtrl implements Serializable {
    
    @ManagedProperty(value="#{mainCtrl}")
    private MainCtrl main;

    @ManagedProperty(value="#{loginCtrl}")
    private LoginCtrl loginCtrl;
    
    private UserMachineSession activeSession;
    
    public ProbeSessionCtrl() {

        this.activeSession = null;
        
        // on load, add this session to the session push group
        PushRenderer.addCurrentSession("session");

    }

    @PostConstruct
    private void init() {
        try {
            this.main.getUserMgmt().setUserProbeSessionBean(this);
            System.out.println("ProbeSessionCtrl constructed");
        } catch (UserNotFoundException ex) {
            System.out.println("ProbeSessionCtrl constructed but probe management not activated");
        }
    }
    
    @PreDestroy
    private void cleanup() {
        try {
            this.main.getUserMgmt().setUserProbeSessionBean(null);
        } catch (UserNotFoundException ex) { }
        System.out.println("ProbeSessionCtrl destroyed");
    }

    public void startProbeWithoutRecording() {
        long machineId = this.loginCtrl.getSelectedMachine().getId();
        this.loginCtrl.getUser().getCurrentSession().startUserMachineSession(new Probe(machineId));
        this.loginCtrl.getSelectedMachine().setState(EMachineState.PROBING_WITHOUT_RECORDING);
        this.activeSession = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
        System.out.println("Probe started on machine: " +machineId);
        PushRenderer.render("session");
    }
    
    public void startProbeWithRecording() {
        long machineId = this.loginCtrl.getSelectedMachine().getId();
        this.loginCtrl.getUser().getCurrentSession().startUserMachineSession(new ProbeProgram(machineId));
        this.loginCtrl.getSelectedMachine().setState(EMachineState.PROBING_AND_RECORDING);
        this.activeSession = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
        PushRenderer.render("session");
    }

    public void startProbeProgramExecution(ProbeProgram program) {
        long machineId = this.loginCtrl.getSelectedMachine().getId();
        this.loginCtrl.getUser().getCurrentSession().startUserMachineSession(new ProbeProgramExecution(machineId));
        this.loginCtrl.getSelectedMachine().setState(EMachineState.PROBING_PROGRAM_RUNNING);
        this.activeSession = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
        PushRenderer.render("session");
    }
    
    @PreDestroy
    public void stopProbe() {
        this.loginCtrl.getUser().getCurrentSession().endUserMachineSession();
        this.loginCtrl.getSelectedMachine().setState(EMachineState.CONNECTED);
        System.out.println("Probe stopped on machine: " +this.loginCtrl.getSelectedMachine().getId());
        this.loginCtrl.setSelectedMachine(null);
        this.activeSession = null;
        System.out.print("Remove operationCtrl from context...");
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("operationCtrl");
        System.out.print("done! - ");
        PushRenderer.getPortableRenderer().render("session");
        System.out.println("rendered!");
    }

    public MainCtrl getMain() {
        return this.main;
    }

    public void setMain(MainCtrl main) {
        this.main = main;
    }

    public LoginCtrl getLoginCtrl() {
        return this.loginCtrl;
    }

    public void setLoginCtrl(LoginCtrl loginCtrl) {
        this.loginCtrl = loginCtrl;
    }

    public UserMachineSession getActiveSession() {
        return activeSession;
    }

}
