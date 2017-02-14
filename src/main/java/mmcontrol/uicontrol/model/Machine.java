package mmcontrol.uicontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import javax.persistence.*;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;
import mmcontrol.uicontrol.model.enums.EMachineState;
import mmcontrol.uicontrol.model.enums.EOperation;
import mmcontrol.uicontrol.model.enums.EOperationState;

/**
 * Represents a Machine to conduct Probes on.
 * 
 * @author Michael Zawrel
 */
@Entity
@Table(name="machine")
public class Machine implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "machine_id")
    protected long id;
    
    @Column
    private String name;
    
    @Column
    private String type; //e.g. "Numerex A12-39d" OR "tactile"?
    
    @Column
    private String location; //EnumOrganization or exact laboratory number

    @Column
    private String description; //contains exact laboratory number?

    @Transient
    private EMachineState state;    //TODO: think whether persistency necessary

    @Transient
    private EOperation operation;
    
    @Transient
    private EOperationState operationState;

    @Column
    private boolean pathprobeReady; //Bahnsteuerung yes/no

    @OneToMany(cascade=CascadeType.ALL, mappedBy="machine")
    private ArrayList<MachineComponent> components; //actuators and sensors whose index is the pin-list of the PLC
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "machine")
    private ArrayList<MachineSession> sessions;
    
    private double posXZero, posYZero, posZZero;
    
    public Machine(long id) {
        //TODO: read from database of config file!
        this.id = id;
        this.name = "NUMEREX simuliert";
        this.type = "Numerex";
        this.state = EMachineState.INACTIVE;
        this.operation = EOperation.WAITING_FOR_CALIBRATION;
        this.operationState = EOperationState.WAITING;
        this.pathprobeReady = false;
        this.sessions = new ArrayList<>();
        
        this.posXZero = 0d;
        this.posYZero = 0d;
        this.posZZero = 0d;
        
        this.components = new ArrayList<>();
        this.components.add(new ActuatorDigital("MXG+"));   //Index: 0
        this.components.add(new ActuatorDigital("MXG-"));
        this.components.add(new ActuatorDigital("MXF+"));
        this.components.add(new ActuatorDigital("MXF-"));
        this.components.add(new ActuatorDigital("MYG+"));
        this.components.add(new ActuatorDigital("MYG-"));
        this.components.add(new ActuatorDigital("MYF+"));
        this.components.add(new ActuatorDigital("MYF-"));
        this.components.add(new ActuatorDigital("MZG+"));
        this.components.add(new ActuatorDigital("MZG-"));
        this.components.add(new ActuatorDigital("MZF+"));   //Index: 10
        this.components.add(new ActuatorDigital("MZF-"));
        this.components.add(new ActuatorDigital("PXG"));
        this.components.add(new ActuatorDigital("PXF"));
        this.components.add(new ActuatorDigital("PYG"));
        this.components.add(new ActuatorDigital("PYF"));
        this.components.add(new ActuatorDigital("PZG"));
        this.components.add(new ActuatorDigital("PZF"));
        this.components.add(new SensorDigital("AirPressure"));
        this.components.add(new SensorDigital("Contact"));
        this.components.add(new SensorDigital("SPXG"));   //Index: 20
        this.components.add(new SensorDigital("SPXF"));
        this.components.add(new SensorDigital("SPYG"));
        this.components.add(new SensorDigital("SPYF"));
        this.components.add(new SensorDigital("SPZG"));
        this.components.add(new SensorDigital("SPZF"));
        this.components.add(new SensorAnalog("POSX"));
        this.components.add(new SensorAnalog("POSY"));
        this.components.add(new SensorAnalog("POSZ"));
    }
    
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EMachineState getState() {
        return state;
    }

    public void setState(EMachineState state) {
        this.state = state;
    }

    public EOperation getOperation() {
        return operation;
    }

    public void setOperation(EOperation operation) {
        this.operation = operation;
    }

    public EOperationState getOperationState() {
        return operationState;
    }

    public void setOperationState(EOperationState operationState) {
        this.operationState = operationState;
    }

    public boolean isPathprobeReady() {
        return pathprobeReady;
    }

    public void setPathprobeReady(boolean pathprobeReady) {
        this.pathprobeReady = pathprobeReady;
    }

    public ArrayList<MachineComponent> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<MachineComponent> components) {
        this.components = components;
    }

    public ArrayList<MachineSession> getSessions() {
        return sessions;
    }

    public MachineSession getCurrentSession() {
        if(this.sessions.size() > 0 && this.sessions.get(sessions.size()-1).getEndTime() == null) {
            return this.sessions.get(this.sessions.size()-1);
        }
        else return null;
    }

    public void startSession(IMachineCommunicationService communicationService) {
        MachineSession ms = new MachineSession(communicationService);
        ms.setId(this.sessions.size()+1);
        ms.setBeginTime(new java.sql.Date(new Date().getTime()));
        this.sessions.add(ms);
        setState(EMachineState.CONNECTED);
        setOperation(EOperation.WAITING_FOR_CALIBRATION);
        setOperationState(EOperationState.WAITING);
    }
    
    public void endSession() {
        if(this.getCurrentSession() != null) {
            this.getCurrentSession().setCommunicationServiceToNull();
            this.getCurrentSession().setEndTime(new java.sql.Date(new Date().getTime()));
        }
        setState(EMachineState.INACTIVE);
    }
    
    public double getPosXZero() {
        return posXZero;
    }

    public void setPosXZero(double posXZero) {
        this.posXZero = posXZero;
    }

    public double getPosYZero() {
        return posYZero;
    }

    public void setPosYZero(double posYZero) {
        this.posYZero = posYZero;
    }

    public double getPosZZero() {
        return posZZero;
    }

    public void setPosZZero(double posZZero) {
        this.posZZero = posZZero;
    }

    @Override
    public String toString() {
        return "ID: " +this.id +", " +this.name + " (" +this.type +")";
    }
    
    public String getStateAsString() {
        String text = this.name + ":\n";
        for(MachineComponent component : this.components) {
            text += component.getName() + ": " + component.getValueAsString() +"\n";
        }
        return text;
    }
    
}
