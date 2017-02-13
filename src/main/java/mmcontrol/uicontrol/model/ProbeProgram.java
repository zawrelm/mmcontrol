package mmcontrol.uicontrol.model;

import java.sql.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Michael Zawrel
 */
public class ProbeProgram extends Probe {

/*    @Temporal(TemporalType.TIMESTAMP)
    private Date timeRecorded;
    
    @ManyToOne
    @JoinColumn(referencedColumnName = "machine_id")
    private Machine recordingMachine;*/
    
    @ManyToOne
    @JoinColumn(referencedColumnName = "user_id")
    private User creator;
    
    @Column
    private List<String> commands;

    public ProbeProgram(long machineId) {
        super(machineId);
        //this.timeRecorded = new java.sql.Date(new java.util.Date().getTime());
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<String> getCommands() {
        return commands;
    }

    //TODO: override protocolling method to generate program commands!
}
