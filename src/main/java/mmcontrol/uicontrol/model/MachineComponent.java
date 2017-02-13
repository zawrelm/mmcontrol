package mmcontrol.uicontrol.model;

import mmcontrol.uicontrol.model.Machine;
import java.io.Serializable;
import javax.persistence.*;

/**
 * Represents a MachineComponent that is mounted to a Machine.
 * 
 * @author Michael Zawrel
 */
@MappedSuperclass
public abstract class MachineComponent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected long id;
    
    @Column
    private String name; // e.g. pinza x grueso - how to deal with internacionalization?

    @ManyToOne(optional = false)
    @JoinColumn(name = "machine", referencedColumnName = "machine_id")
    private Machine machine;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }
    
    public abstract void setValue(String value); //TODO: "NUMBER" instead of "String"? Define type-safe methods in interfaces for analog/digital-components?
    public abstract String getValueAsString();  //TODO: implement bound generics like MachineComponent<T extends>... OR SIMPLY CLASS "NUMBER"?

}