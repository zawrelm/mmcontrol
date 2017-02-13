package mmcontrol.uicontrol.utils;

import java.util.Iterator;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import mmcontrol.uicontrol.beans.MainCtrl;
import mmcontrol.uicontrol.model.Machine;

/**
 *
 * @author Michael Zawrel
 */
public class MachineConverter implements Converter {

    @ManagedProperty(value="#{mainCtrl}")
    private MainCtrl main;
        
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {

        try {
            for(Machine m : main.getMachineMgmt().getMachines().values()) {
                String value = m.getId() +" - " +m.getName();
                if(string.equals(value)) return main.getMachineMgmt().getMachines().get(m.getId());
            }
        }
        catch (Exception ex) { }
        
        return null;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        if(o.getClass() == Machine.class) {
            Machine m = (Machine) o;
            return m.getId() +" - " +m.getName();
        }
        return null;
    }
    
}
