package mmcontrol.uicontrol.model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author Michael Zawrel
 */
@Entity
public class SensorAnalog extends Sensor {

    @Column
    private double value;

    public SensorAnalog(String name) {
        super.setName(name);
    }
    
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        this.value = Double.parseDouble(value);
    }

    @Override
    public String getValueAsString() {
        return ""+this.value;
    }
    
}
