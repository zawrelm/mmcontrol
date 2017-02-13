package mmcontrol.uicontrol.jpa_UNUSED;

import java.util.ArrayList;
import java.util.Collection;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import mmcontrol.uicontrol.interfaces.IMachineManagementService;
import mmcontrol.uicontrol.exceptions.MachineUnknownException;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.MachineComponent;
import mmcontrol.uicontrol.model.enums.EMachineState;

/**
 * This class manages state of and access to the stored machines
 * 
 * @author Michael Zawrel
 */
@ManagedBean(name="machineMgmt")
@ApplicationScoped
public class MachineManagementBean implements IMachineManagementService {

    //stores a reference to global EMF for acquiring EM
    protected JPAResourceFactory jpaResourceFactory;
    
    private ArrayList<Machine> machinelist; //NEEDED HERE?

    /**
     * Creates a machine in the database and adds it to the list.
     * 
     * @param machine The machine to be created.
     */
    public void createMachine(Machine machine) {
        EntityManager em = jpaResourceFactory.getEMF().createEntityManager();
        try{
            em.getTransaction().begin();
            em.persist(machine);
            em.getTransaction().commit();
        }finally{
            em.close();
        }
        this.machinelist.add(machine);
    }
        
    /**
     * Finds and returns a machine by its id.
     * @param machineId The machine id to be found.
     * @return machine 
     */
    @Override
    public Machine getMachineById(long machineId){
        EntityManager em = jpaResourceFactory.getEMF().createEntityManager();
        try{
            return em.find(Machine.class, machineId);
        }finally{
            em.close();
        }
    }
    
    //is em.merge(Machine) reliable to do all changes in one method?
    /**
     * Changes the state of a machine defined by its id.
     * @param machineId
     * @param newState 
     */
    public void alterMachineState(long machineId, EMachineState newState){
        EntityManager em = jpaResourceFactory.getEMF().createEntityManager();
        try{
            em.getTransaction().begin();
            Machine machine = em.find(Machine.class, machineId);
            machine.setState(newState);
            em.getTransaction().commit();
        }finally{
            em.close();
        }
    }
    
    public void deleteMachine(long machineId) {
        EntityManager em = jpaResourceFactory.getEMF().createEntityManager();
        try {
            em.getTransaction().begin();
            Machine machine = em.find(Machine.class, machineId);
            em.remove(machine);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public void setMachinelist(ArrayList<Machine> machinelist) {
        this.machinelist = machinelist;
    }
    
    public ArrayList<Machine> getMachinelist() {
        return this.machinelist;
    }

    //@TODO: create an conversion adapter for String to Machine
    //http://www.thoughts-on-java.org/jpa-21-type-converter-better-way-to/
    /**
     * Finds and returns a Machine-object by its name.
     * @param name The machinename to be found.
     * @return 
     * @throws MachineUnknownException 
     */
    @Override
    public Machine getMachineByName(String name) throws MachineUnknownException {
        if(machinelist != null) {
            for(Machine m : machinelist) {
                if(m.getName().equals(name)) {
                    return m;
                }
            }
        }
        
        throw new MachineUnknownException();
    }

    @Override
    public Collection<Machine> getActiveMachines() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<MachineComponent> getMachineComponents(long machineId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setMachineState(long machineId, int newState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createNewMachine(Machine machine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
