package mmcontrol.uicontrol.beans;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.User;
import mmcontrol.uicontrol.model.enums.EOperation;
import org.icefaces.application.PushRenderer;

/**
 * The LoginController manages registered users, it performs login and logout.
 * Objects of this class represent HTTP sessions.
 */
@ManagedBean(name="loginCtrl")
@SessionScoped
public class LoginCtrl implements Serializable {
    
    private String email;
    
    private String password;
   
    @ManagedProperty(value="#{mainCtrl}")
    private MainCtrl main;
    
    private User user = null;
    
    /* if true, error messages is displayed in the login component */
    boolean loginfailed = false;
    
    /* Machine selected to start a session on */
    Machine selectedMachine = null;

    public LoginCtrl() {
        
        // on creation, add this session to the push group
        PushRenderer.addCurrentSession("session");

    }

    @PostConstruct
    private void init() {
        System.out.println("LoginCtrl constructed");
    }
    
    @PreDestroy
    private void cleanup() {
        try {
            this.main.getUserMgmt().setUserHTTPSessionObject(null);
        } catch (UserNotFoundException ex) {
            Logger.getLogger(LoginCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("LoginCtrl destroyed");
    }
    
    /**
     * Performs a login for the user set by 'email' and 'password'.
     * @return Forwards the user to the UserMachineSession, or to the startpage if the login has failed.
     */
    public String login() {
        
        //TODO: REMOVE! This is just while testing!
        this.email = "john@doe.com";
        this.password = "test";
        //END REMOVE
        if(this.user != null) 
            this.logout();
            //return "/machinesession.xhtml";   //TODO: change to display a message that logout is needed first
        
        this.loginfailed = true;
        
        try {
            User toCompare = this.main.getUserMgmt().getUser(this.getEmail()); // fetch user matching email from the userstore
            if(toCompare.getPassword().equals(this.password)) { // compare against password
                this.user = toCompare;
                this.main.getUserMgmt().setUserHTTPSessionObject(this);
                this.main.getUserMgmt().getUser(this.getEmail()).startSession();
                this.loginfailed = false;
                PushRenderer.render("session");
                return "/machinesession.xhtml"; // if email/password matches an existing user, return machine control page
            }
            
        } catch(UserNotFoundException e) {
            System.err.println("User not found - login failed!");
        }
        
        /* if the login has failed, redirect user to the initial page */
        return "/start.xhtml";
        
    }
    
   /**
     * Destroys the current session (and therefore the login).
     * @return Returns the user to the startpage.
     */
    public String logout() {
        try {
            if(this.selectedMachine != null) {
                this.selectedMachine.setOperation(EOperation.WAITING_FOR_CALIBRATION);
            }
            this.main.getUserMgmt().getUser(this.user.getEmail()).endSession();
        } catch (UserNotFoundException ex) {
            Logger.getLogger(LoginCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FacesContext context = FacesContext.getCurrentInstance();
 	ExternalContext ec = context.getExternalContext();
 
 	final HttpServletRequest request = (HttpServletRequest)ec.getRequest();
        request.getSession(false).invalidate();
        return "/start.xhtml";
    }

    public void setMain(MainCtrl main) {
        this.main = main;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isLoginfailed() {
        return loginfailed;
    }

    public void setLoginfailed(boolean loginfailed) {
        this.loginfailed = loginfailed;
    }

    public Machine getSelectedMachine() {
        return selectedMachine;
    }

    public void setSelectedMachine(Machine selectedMachine) {
        this.selectedMachine = selectedMachine;
        System.out.println("Machine selected!");
        PushRenderer.render("session");
    }

    public void updateMachineValues() {
        System.out.println("Machine with ID " +this.selectedMachine.getId() +": Position X=" +this.selectedMachine.getPosXAbs() 
                +", Y=" +this.selectedMachine.getPosYAbs() +", Z=" +this.selectedMachine.getPosZAbs());
        PushRenderer.render("session");
        //FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("foo:bar");
    }
    
}
