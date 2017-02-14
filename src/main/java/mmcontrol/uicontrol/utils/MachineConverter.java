package mmcontrol.uicontrol.utils;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import mmcontrol.uicontrol.beans.MainCtrl;
import mmcontrol.uicontrol.model.Machine;

/**
 *
 * @author Michael Zawrel
 */
@ManagedBean(name = "machineConverter")
@ApplicationScoped
@FacesConverter(value = "machineConverter")
public class MachineConverter implements Converter {

    @ManagedProperty(value="#{mainCtrl}")
    private MainCtrl main;
        
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String string) {
//        try {
            for(Machine m : main.getMachineMgmt().getMachines().values()) {
                System.out.println("gAO2");
                String value = "ID: " +m.getId() +", " +m.getName() + " (" +m.getType() +")";
                if(string.equals(value)) return main.getMachineMgmt().getMachines().get(m.getId());
            }
//        }
//        catch (Exception ex) { }
        System.out.println("gAO3");
        return null;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        System.out.println("gAS");
        if(o.getClass() == Machine.class) {
            Machine m = (Machine) o;
            return "ID: " +m.getId() +", " +m.getName() + " (" +m.getType() +")";
        }
        return null;
    }

    public MainCtrl getMain() {
        return main;
    }

    public void setMain(MainCtrl main) {
        this.main = main;
    }
    
}
