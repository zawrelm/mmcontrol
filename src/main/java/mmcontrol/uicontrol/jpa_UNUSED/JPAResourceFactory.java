package mmcontrol.uicontrol.jpa_UNUSED;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * This class manages access to the resources stored in the database
 * Example: http://www.oracle.com/technetwork/middleware/ias/jsf-jpa-tutorial-095959.html#CHDCCJCD
 * 
 * This is an Application Scoped bean that holds the JPA EntityManagerFactory.
 * By making this bean Application scoped the EntityManagerFactory resource 
 * will be created only once for the application and cached here.
 * 
 * @author Michael Zawrel
 */
//TODO: persistence.xml needed to configure persistence unit???
public class JPAResourceFactory {

    private EntityManagerFactory emf;

    /**
     * Allows access to database data via JPA
     * 
     * @return EntityManagerFactory
     */
    public EntityManagerFactory getEMF (){
        if (emf == null){
            emf = Persistence.createEntityManagerFactory("MetrologyMachineControlPU");
        }
        return emf;
    }
    
}
