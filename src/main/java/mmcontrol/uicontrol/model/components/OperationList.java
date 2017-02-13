package mmcontrol.uicontrol.model.components;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import mmcontrol.uicontrol.model.enums.EOperation;

/**
 * This class provides a handle to the list of available operations (stored in EOperation).
 * JSF-components access this class.
 * 
 * @author Michael Zawrel
 */
@ManagedBean(name = "operationList")
@ApplicationScoped
public class OperationList {

    public EOperation[] getOperations() {
        return EOperation.values();
    }

}
