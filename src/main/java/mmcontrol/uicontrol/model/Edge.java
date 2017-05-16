package mmcontrol.uicontrol.model;

import java.io.Serializable;

/**
 *
 * @author Michael Zawrel
 */
public class Edge implements Serializable {
    
    private double dX;
    private double dY;
    private double length;
    
    public Edge(double x, double y) {
        this.dX = x;
        this.dY = y;
        this.length = Math.sqrt(this.dX*this.dX+this.dY*this.dY);
    }

    public Edge(double x, double y, double value) {
        this.dX = x;
        this.dY = y;
        this.length = value;
    }

    public double getdX() {
        return dX;
    }

    public double getdY() {
        return dY;
    }

    public double getLength() {
        return this.length;
    }
    
}
