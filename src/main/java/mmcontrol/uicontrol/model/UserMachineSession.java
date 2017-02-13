package mmcontrol.uicontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Represents a session of a registered User on a connected Machine.
 * e.g. a maintenance session, a probe, ...
 * 
 * @author Michael Zawrel
 */
//TODO: add persistence annotations
public abstract class UserMachineSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    
    /*@ManyToOne//(optional = false)
    @JoinColumn(name = "user_id")
    private User user;*/
    
    /*    @ManyToOne
    @JoinColumn(name = "machine", referencedColumnName = "machine_id")
    private Machine machine;*/
    private long machineId; //only until DB is implemented
    
    private Date beginTime;
    
    private Date endTime;

    private Collection<User> observers;
    
    private Protocol protocol;
    
    public UserMachineSession(long machineId) {
        //this.id = getNextAvailableIdFromDb!
        this.machineId = machineId;
        this.beginTime = new Date();
        this.endTime = null;
        this.observers = new ArrayList<>();
        this.protocol = new Protocol();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMachineId() {
        return machineId;
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

    public Collection<User> getObservers() {
        return observers;
    }

    public void setObservers(Collection<User> observers) {
        this.observers = observers;
    }

    public void addObserver(User observer) {
        this.observers.add(observer);
    }

    public Protocol getProtocol() {
        return protocol;
    }

}
