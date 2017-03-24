package mmcontrol.uicontrol.beans;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import mmcontrol.uicontrol.exceptions.UserNotFoundException;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.User;
import org.icefaces.application.PushRenderer;

/**
 * The LoginController manages registered users, it performs login and logout.
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
    
    /**
     * Performs a login for the user set by 'email' and 'password'.
     * @return Forwards the user to the UserMachineSession, or to the startpage if the login has failed.
     */
    public String login() {
        
        //TODO: REMOVE! This is just while testing!
        this.email = "john@doe.com";
        this.password = "test";
        //END REMOVE
        
        this.loginfailed = true;
        
        try {
            User toCompare = main.getUserMgmt().getUser(this.getEmail()); // fetch user matching email from the userstore
            if(toCompare.getPassword().equals(this.password)) { // compare against password
                this.loginfailed = false;
                this.main.getUserMgmt().getUser(this.getEmail()).startSession();
                this.user = toCompare;
                PushRenderer.render("session");
                return "/machinesession.xhtml"; // if email/password matches an existing user, return machine control page
            }
            
        } catch(UserNotFoundException e) {
            
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

    public void setMain(MainCtrl main) {
        this.main = main;
    }
    
}
