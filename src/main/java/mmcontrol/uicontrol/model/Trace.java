package mmcontrol.uicontrol.model;

import java.io.Serializable;

/**
 *
 * @author Michael Zawrel
 */
public class Trace implements Serializable {
    
    private Integer id;
    
    private String operation;

    private String desc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    
    
    
    
}
