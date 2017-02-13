package mmcontrol.uicontrol.beans;

import java.rmi.RemoteException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import mmcontrol.uicontrol.exceptions.ConnectionServiceNotFoundException;

/**
 *
 * @author Michael Zawrel
 */
@ManagedBean(name="mainCtrl", eager=true)
@ApplicationScoped
public class MainCtrl {
    
    private StoredUsers userMgmt;
    
    private StoredMachines machineMgmt;
    
    public MainCtrl() throws ConnectionServiceNotFoundException {
        
        this.userMgmt = new StoredUsers();
        try {
            this.machineMgmt = new StoredMachines(this);
        } catch (RemoteException ex) {
            throw new ConnectionServiceNotFoundException();
        }
        
    }

    @PostConstruct
    public void init() {
        this.machineMgmt.onStartup();
    }
    
    @PreDestroy
    public void destruct() {
        this.machineMgmt.onShutdown();
    }

    public StoredUsers getUserMgmt() {
        return userMgmt;
    }

    public StoredMachines getMachineMgmt() {
        return machineMgmt;
    }

}
