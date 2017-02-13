package mmcontrol.uicontrol.model;

import java.io.Serializable;

/**
 *
 * @author Michael Zawrel
 */
public class Vector implements Serializable {
    
    private double x;
    private double y;
    private double value;
    
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
        this.value = Math.sqrt(this.x*this.x+this.y*this.y);
    }

    public Vector(double x, double y, double value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getValue() {
        return this.value;
    }
    
}
