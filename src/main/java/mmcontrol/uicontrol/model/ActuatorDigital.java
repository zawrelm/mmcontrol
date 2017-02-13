package mmcontrol.uicontrol.model;

import javax.persistence.Entity;

/**
 *
 * @author Michael Zawrel
 */
@Entity
public class ActuatorDigital extends Actuator {
    
    private boolean value;

    public ActuatorDigital(String name) {
        super.setName(name);
    }
    
    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        if(value.equals("1")) this.value = true;
        else this.value = false;
    }

    @Override
    public String getValueAsString() {
        if(this.value) return "1";
        else return "0";
    }

}
