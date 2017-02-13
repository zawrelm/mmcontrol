package mmcontrol.uicontrol.model;

import javax.persistence.Entity;

/**
 *
 * @author Michael Zawrel
 */
@Entity
public class SensorDigital extends Sensor {
    
    private boolean value;

    public SensorDigital(String name) {
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
