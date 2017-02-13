package mmcontrol.uicontrol.model;

import java.io.Serializable;
import java.sql.Date;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;

/**
 *
 * @author Michael Zawrel
 */
@Entity
public class MachineSession implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date beginTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Transient
    private IMachineCommunicationService communicationService;
    
    /**
     * TODO: To display all UserSessions on this Machine (look up in database)
     * private Map<Long, UserMachineSession> userSessions;
     */
    
    public MachineSession(IMachineCommunicationService communicationService) {
        this.communicationService = communicationService;
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

    public IMachineCommunicationService getCommunicationService() {
        return communicationService;
    }

    public void setCommunicationServiceToNull() {
        this.communicationService = null;
    }
    
}
