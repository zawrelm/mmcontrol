package mmcontrol.uicontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;
import mmcontrol.uicontrol.model.enums.EMachineState;

/**
 * Stores information about User logins on the server
 * 
 * @author Michael Zawrel
 */
//add persistence annotations
public class UserSession implements Serializable {
    
    protected long id;

    private Date beginTime;
    
    private Date endTime;
    
    private List<UserMachineSession> userMachineSessions;
    
    public UserSession() {
        this.userMachineSessions = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<UserMachineSession> getUserMachineSessions() {
        return userMachineSessions;
    }

    public UserMachineSession getCurrentSession() {
        if(this.userMachineSessions.size() > 0 && this.userMachineSessions.get(this.userMachineSessions.size()-1).getEndTime() == null) {
            return this.userMachineSessions.get(this.userMachineSessions.size()-1);
        }
        else return null;
    }

    public void startUserMachineSession(UserMachineSession ums) { //TODO: create multiple methods for each type of UMS?
        ums.setId(this.userMachineSessions.size()+1);
        ums.setBeginTime(new java.sql.Date(new Date().getTime()));
        this.userMachineSessions.add(ums);
        
    }
    
    public void endUserMachineSession() {
        if(this.getCurrentSession() != null) {
            this.getCurrentSession().setEndTime(new java.sql.Date(new Date().getTime()));
        }
    }

}
