package mmcontrol.common.model;

import java.io.Serializable;
import java.text.ParseException;

/**
 * Stores the last two status information messages of a metrology machine.
 * 
 * @author Michael Zawrel
 */
public class StatusMessage implements Serializable {
    
    private String current;     // the most recent status message
    
    private String previous;    // the second most recent status message
    
    public StatusMessage() {
        
        this.current = null;
        this.previous = null;
        
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }
    
    /**
     * Determines whether the new status represents a notable change to the previous.
     * 
     * @return notably new status
     * @throws IndexOutOfBoundsException if machine status message is too short
     * @throws ParseException if machine status message contains letters at the position of analog values
     */
    public boolean changed() throws IndexOutOfBoundsException, ParseException {
        if(this.current == null) return false;          // we don't want to display a null state
        else if(this.previous == null) return true;     // we want to display if not null anymore
        else {
            if(!this.current.substring(0, 122).equals(this.previous.substring(0, 122))) return true;    //if change in digital pins
            if(this.current.substring(124).equals(this.previous.substring(124))) return false;   //if no change in digital&analog values
            
            /* Filter out measurement inaccuracies (<threshold -> no change) */
            String[] valuesCurrent = this.current.substring(124).split(" ");
            String[] valuesPrevious = this.previous.substring(124).split(" ");
            /*System.out.println(this.current.substring(0, 122));
            System.out.println("X" +valuesPrevious[valuesCurrent.length-5]
                              +" Y" +valuesPrevious[valuesCurrent.length-3]
                              +" Z" +valuesPrevious[valuesCurrent.length-1]);*/

            Double deltaX = Double.parseDouble(valuesPrevious[valuesPrevious.length-5]) 
                    - Double.parseDouble(valuesCurrent[valuesCurrent.length-5]);
            if(Math.abs(deltaX) > 0.000001d) return true;  //has to be higher than distance moved by motor slow per update cycle
            
            Double deltaY = Double.parseDouble(valuesPrevious[valuesPrevious.length-3]) 
                    - Double.parseDouble(valuesCurrent[valuesCurrent.length-3]);
            if(Math.abs(deltaY) > 0.000001d) return true;

            Double deltaZ = Double.parseDouble(valuesPrevious[valuesPrevious.length-1]) 
                    - Double.parseDouble(valuesCurrent[valuesCurrent.length-1]);
            if(Math.abs(deltaZ) > 0.0001d) return true;
        }
        return false;
    }
    
}
