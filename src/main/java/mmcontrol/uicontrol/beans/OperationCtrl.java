package mmcontrol.uicontrol.beans;


import java.util.Iterator;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import mmcontrol.uicontrol.interfaces.IMachineControlService;
import mmcontrol.common.exceptions.IncompatiblePinException;
import mmcontrol.common.exceptions.MachineOperationTemporarilyForbiddenException;
import mmcontrol.common.exceptions.MachineUnreachableException;
import mmcontrol.common.interfaces.machineconnection.IMachineCommunicationService;
import mmcontrol.uicontrol.model.Machine;
import mmcontrol.uicontrol.model.MachineComponent;
import mmcontrol.uicontrol.model.Position;
import mmcontrol.uicontrol.model.UserMachineSession;
import mmcontrol.uicontrol.model.Vector;
import mmcontrol.uicontrol.model.enums.EOperation;
import mmcontrol.uicontrol.model.enums.EOperationState;
import org.icefaces.application.PushRenderer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import mmcontrol.uicontrol.model.Trace;


//TODO: Implement more functionality (geometric calculations); suggestion: use a geometry library!

/**
 * Provides a common interface for all commands performed by a User on a Machine.
 * It represents a UserMachineSession (= probing session of User on a Machine)
 * 
 * @author Michael Zawrel
 */
@ManagedBean(name="operationCtrl")
@SessionScoped //TODO: find the right Scope or a way to destroy object when Probe is ended!
public class OperationCtrl implements Serializable, IMachineControlService {
    
    @ManagedProperty(value="#{mainCtrl}")
    private MainCtrl mainCtrl;

    @ManagedProperty(value="#{loginCtrl}")
    private LoginCtrl loginCtrl;
    
    private UserMachineSession session;
    private Machine machine;
    private IMachineCommunicationService communication;
    
    private int xfLastMove, xsLastMove, yfLastMove, ysLastMove, zfLastMove, zsLastMove; //protocol line number of last move; negative when still moving

    private ArrayList<Position> measuredPoints;
    
    private Boolean movingX;
    
    private Boolean movingY;
    
    private Boolean movingZ;
    
    private String currentAction;
    
    private Double goPosX;
    
    private Double goPosY;
    
    private Double goPosZ;
    
    private List<Trace> traces;
    
    private FacesMessage facesMessageX;

    private FacesMessage facesMessageY;

    private FacesMessage facesMessageZ;

    
    public OperationCtrl() {
        this.movingX = false;
        this.movingY = false;
        this.movingZ = false;
        this.currentAction = null;
        this.traces = new ArrayList<>();
        this.xfLastMove = 0;
        this.xsLastMove = 0;
        this.yfLastMove = 0;
        this.ysLastMove = 0;
        this.zfLastMove = 0;
        this.zsLastMove = 0;
        
        this.facesMessageX = null;
        this.facesMessageY = null;
        this.facesMessageZ = null;
        
        this.measuredPoints = new ArrayList<>();
    }

    @PostConstruct
    private void init() {
        
//TODO descomentar        this.session = this.loginCtrl.getUser().getCurrentSession().getCurrentSession();
//        this.machine = this.mainCtrl.getMachineMgmt().getMachines().get(this.session.getMachineId());
//        this.communication = this.machine.getCurrentSession().getCommunicationService();
    }
    
    @Override
    public boolean moveXFastForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                /* Check whether current state of machine actuators allows actuator activation */
                if(!comp.get(19).getValueAsString().equals("0")) return false;   /* CHECK sensing head contact */
                if(comp.get(18).getValueAsString().equals("0")) return false;    /* CHECK no air pressure */
                if(!comp.get(1).getValueAsString().equals("0")) return false;    /* CHECK fast moving backwards */
                if(!comp.get(2).getValueAsString().equals("0")) return false;    /* CHECK slow moving forward */
                if(!comp.get(3).getValueAsString().equals("0")) return false;    /* CHECK slow moving backwards */
                if(comp.get(20).getValueAsString().equals("0")) return false;    /* CHECK clip fast opened */
                if(!comp.get(21).getValueAsString().equals("0")) return false;   /* CHECK clip slow closed */
            }
            
            if(this.xfLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move x rapidly from " +this.getComponentValue(26));
                this.xfLastMove = -(this.session.getProtocol().getLength()-1);
            }
            
            this.communication.setDigitalPin(0, move);

            if(this.xfLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.xfLastMove, " to " +this.getComponentValue(26));
                this.xfLastMove = -this.xfLastMove;
            }
            
            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveXFastBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(!comp.get(19).getValueAsString().equals("0")) return false;   /* CHECK sensing head contact */
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(0).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(2).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(3).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(comp.get(20).getValueAsString().equals("0")) return false;     /* CHECK clip fast opened */
                if(!comp.get(21).getValueAsString().equals("0")) return false;    /* CHECK clip slow closed */
            }

            if(this.xfLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move x rapidly from " +this.getComponentValue(26));
                this.xfLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(1, move);

            if(this.xfLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.xfLastMove, " to " +this.getComponentValue(26));
                this.xfLastMove = -this.xfLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveXSlowForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException(); //TODO: remove this check where useless
        
            
        /*
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        // remove existing messages
        Iterator<FacesMessage> i = facesContext.getMessages();
        while (i.hasNext()) {
            i.next();
            i.remove();
        }        
        
        //UIComponent component = event.getComponent();
        String message = "Info Sample";
        FacesMessage facesMessage = new FacesMessage((FacesMessage.Severity) FacesMessage.VALUES.get(0), message, message);
        facesContext.addMessage(component.getClientId() null, facesMessage);
        */
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(0).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(1).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(3).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(!comp.get(20).getValueAsString().equals("0")) return false;     /* CHECK clip fast closed */
                if(comp.get(21).getValueAsString().equals("0")) return false;    /* CHECK clip slow opened */
            }
            
            if(this.xsLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move x slowly from " +this.getComponentValue(26));
                this.xsLastMove = -(this.session.getProtocol().getLength()-1);
            }
            
            this.communication.setDigitalPin(2, move);

            if(this.xsLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.xsLastMove, " to " +this.getComponentValue(26));
                this.xsLastMove = -this.xsLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveXSlowBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(0).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(1).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(2).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(20).getValueAsString().equals("0")) return false;     /* CHECK clip fast closed */
                if(comp.get(21).getValueAsString().equals("0")) return false;    /* CHECK clip slow opened */
            }
            
            if(this.xsLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move x slowly from " +this.getComponentValue(26));
                this.xsLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(3, move);

            if(this.xsLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.xsLastMove, " to " +this.getComponentValue(26));
                this.xsLastMove = -this.xsLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveYFastForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(!comp.get(19).getValueAsString().equals("0")) return false;    /* CHECK sensing head contact */
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(5).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(6).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(7).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(comp.get(22).getValueAsString().equals("0")) return false;     /* CHECK clip fast opened */
                if(!comp.get(23).getValueAsString().equals("0")) return false;    /* CHECK clip slow closed */
            }
            
            if(this.yfLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move y rapidly from " +this.getComponentValue(27));
                this.yfLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(4, move);

            if(this.yfLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.yfLastMove, " to " +this.getComponentValue(27));
                this.yfLastMove = -this.yfLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveYFastBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(!comp.get(19).getValueAsString().equals("0")) return false;    /* CHECK sensing head contact */
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(4).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(6).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(7).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(comp.get(22).getValueAsString().equals("0")) return false;     /* CHECK clip fast opened */
                if(!comp.get(23).getValueAsString().equals("0")) return false;    /* CHECK clip slow closed */
            }
            
            if(this.yfLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move y rapidly from " +this.getComponentValue(27));
                this.yfLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(5, move);

            if(this.yfLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.yfLastMove, " to " +this.getComponentValue(27));
                this.yfLastMove = -this.yfLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveYSlowForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(4).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(5).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(7).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(!comp.get(22).getValueAsString().equals("0")) return false;     /* CHECK clip fast closed */
                if(comp.get(23).getValueAsString().equals("0")) return false;    /* CHECK clip slow opened */
            }
            
            if(this.ysLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move y slowly from " +this.getComponentValue(27));
                this.ysLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(6, move);

            if(this.ysLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.ysLastMove, " to " +this.getComponentValue(27));
                this.ysLastMove = -this.ysLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveYSlowBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(4).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(5).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(6).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(22).getValueAsString().equals("0")) return false;     /* CHECK clip fast closed */
                if(comp.get(23).getValueAsString().equals("0")) return false;    /* CHECK clip slow opened */
            }
            
            if(this.ysLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move y slowly from " +this.getComponentValue(27));
                this.ysLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(7, move);
            
            if(this.ysLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.ysLastMove, " to " +this.getComponentValue(27));
                this.ysLastMove = -this.ysLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    public void move(ActionEvent event) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        String axis;
        String orientation;
        //String speed;
        System.out.println("Movimiento 1");
        axis = (String)event.getComponent().getAttributes().get("axis");
        orientation = (String)event.getComponent().getAttributes().get("orientation");

        FacesMessage facesMessage; // = (axis == "x") ? this.facesMessageX : (axis == "y") ? this.facesMessageY : this.facesMessageZ;
        //speed = (String)event.getComponent().getAttributes().get("speed");

        System.out.println("Movimiento " + orientation + axis );

        FacesContext facesContext = FacesContext.getCurrentInstance();

        if ( (0 == axis.compareTo("x") && movingX) 
           ||(0 == axis.compareTo("y") && movingY) 
           ||(0 == axis.compareTo("z") && movingZ)) {
            // remove existing messages
            //Iterator<FacesMessage> i = facesContext.getMessages();
            //while (i.hasNext()) {
            //    i.next();
            //    i.remove();
            //}
            facesMessage = null;

        } else {
            // add messages
//            FacesContext context = FacesContext.getCurrentInstance();
//            String movingAxisMessage = context.getApplication().evaluateExpressionGet(context, "#{text['some.movingAxis']}", String.class);
            UIComponent component = event.getComponent();
            String message = "#{msg.movingAxis1} " + orientation + axis + ".\n" + "#{msg.movingAxis2}";
            facesMessage = new FacesMessage((FacesMessage.Severity) FacesMessage.VALUES.get(0), message, message);
            facesContext.addMessage(component.getClientId(), facesMessage);

        }
        switch (axis) {
            case "x":
                this.facesMessageX = facesMessage;
                this.movingX = !this.movingX;
                break;
            case "y":
                this.facesMessageY = facesMessage;
                this.movingY = !this.movingY;
                break;
            case "z":
                this.facesMessageZ = facesMessage;
                this.movingZ = !this.movingZ;
                break;
        }

        
    }
    
    @Override
    public boolean moveZFastForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        System.out.println("Movimiento 2");
        return true;
//        if(this.communication == null) throw new MachineUnreachableException();
//        //boolean move = true;
//        
//        FacesContext facesContext = FacesContext.getCurrentInstance();
//                       
//        
//        try {
//            ArrayList<MachineComponent> comp = this.machine.getComponents();
//            
////            if ( 0 == moving.compareTo("true") ) {
////                moving = "false";
////                // remove existing messages
////                Iterator<FacesMessage> i = facesContext.getMessages();
////                while (i.hasNext()) {
////                    i.next();
////                    i.remove();
////                }
////
////            } else {
////                // add messages
////                UIComponent component = event.getComponent();
////                String message = "Moviendo +Z";
////                FacesMessage facesMessage = new FacesMessage((FacesMessage.Severity) FacesMessage.VALUES.get(0), message, message);
////                facesContext.addMessage(component.getClientId(), facesMessage);
////
////                moving = "true";
////            }
//            
//            
//            
//            
//            if(move) {
//                if(!comp.get(19).getValueAsString().equals("0")) return false;    /* CHECK sensing head contact */
//                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
//                if(!comp.get(9).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
//                if(!comp.get(10).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
//                if(!comp.get(11).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
//                if(comp.get(24).getValueAsString().equals("0")) return false;     /* CHECK clip fast opened */
//                if(!comp.get(25).getValueAsString().equals("0")) return false;    /* CHECK clip slow closed */
//            }
//            
//            if(this.zfLastMove >= 0 && move) {
//                this.session.getProtocol().addLine("move z rapidly from " +this.getComponentValue(28));
//                this.zfLastMove = -(this.session.getProtocol().getLength()-1);
//            }
//
//            this.communication.setDigitalPin(8, move);
//            
//            if(this.zfLastMove < 0 && !move) {
//                this.session.getProtocol().appendToLine(-this.zfLastMove, " to " +this.getComponentValue(28));
//                this.zfLastMove = -this.zfLastMove;
//            }
//
//            return true;
//
//        } catch (IncompatiblePinException | RemoteException ex) {
//            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        return false;
    }

    @Override
    public boolean moveZFastBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(!comp.get(19).getValueAsString().equals("0")) return false;    /* CHECK sensing head contact */
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(8).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(10).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(11).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(comp.get(24).getValueAsString().equals("0")) return false;     /* CHECK clip fast opened */
                if(!comp.get(25).getValueAsString().equals("0")) return false;    /* CHECK clip slow closed */
            }
            
            if(this.zfLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move z rapidly from " +this.getComponentValue(28));
                this.zfLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(9, move);
            
            if(this.zfLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.zfLastMove, " to " +this.getComponentValue(28));
                this.zfLastMove = -this.zfLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveZSlowForward(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(8).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(9).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(11).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                if(!comp.get(24).getValueAsString().equals("0")) return false;     /* CHECK clip fast closed */
                if(comp.get(25).getValueAsString().equals("0")) return false;    /* CHECK clip slow opened */
            }
            
            if(this.zsLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move z slowly from " +this.getComponentValue(28));
                this.zsLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(10, move);
            
            if(this.zsLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.zsLastMove, " to " +this.getComponentValue(28));
                this.zsLastMove = -this.zsLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveZSlowBackwards(boolean move) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(move) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(8).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(9).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                if(!comp.get(10).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(24).getValueAsString().equals("0")) return false;     /* CHECK clip fast closed */
                if(comp.get(25).getValueAsString().equals("0")) return false;    /* CHECK clip slow opened */
            }
            
            if(this.zsLastMove >= 0 && move) {
                this.session.getProtocol().addLine("move z slowly from " +this.getComponentValue(28));
                this.zsLastMove = -(this.session.getProtocol().getLength()-1);
            }

            this.communication.setDigitalPin(11, move);
            
            if(this.zsLastMove < 0 && !move) {
                this.session.getProtocol().appendToLine(-this.zsLastMove, " to " +this.getComponentValue(28));
                this.zsLastMove = -this.zsLastMove;
            }

            return true;

        } catch (IncompatiblePinException | RemoteException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean setXFastConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(connected) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(0).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(1).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                this.communication.setDigitalPin(2, false);             /* STOP slow moving forward */
                Thread.sleep(100);  //TODO: instead of a fixed sleep timer, check machine state for success before performing next step
                this.communication.setDigitalPin(3, false);             /* STOP slow moving backwards */
                Thread.sleep(100);
            }
            else {
                this.communication.setDigitalPin(0, false);
                Thread.sleep(100);
                this.communication.setDigitalPin(1, false);
                Thread.sleep(100);
            }
            
            this.communication.setDigitalPin(12, connected);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean setXSlowConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(connected) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(2).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(3).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                this.communication.setDigitalPin(0, false);             /* STOP fast moving forward */
                Thread.sleep(100);
                this.communication.setDigitalPin(1, false);             /* STOP fast moving backwards */
                Thread.sleep(100);
            }
            else {
                this.communication.setDigitalPin(2, false);
                Thread.sleep(100);
                this.communication.setDigitalPin(3, false);
                Thread.sleep(100);
            }
            
            this.communication.setDigitalPin(13, connected);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean setYFastConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(connected) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(4).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(5).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                this.communication.setDigitalPin(6, false);             /* STOP slow moving forward */
                
                Thread.sleep(100);  //TODO: instead of a fixed sleep timer, check machine state for success before performing next step
                this.communication.setDigitalPin(7, false);             /* STOP slow moving backwards */
                Thread.sleep(100);
            }
            else {
                this.communication.setDigitalPin(4, false);
                Thread.sleep(100);
                this.communication.setDigitalPin(5, false);
                Thread.sleep(100);
            }
            
            this.communication.setDigitalPin(14, connected);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean setYSlowConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(connected) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(6).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(7).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                this.communication.setDigitalPin(4, false);             /* STOP fast moving forward */
                Thread.sleep(100);
                this.communication.setDigitalPin(5, false);             /* STOP fast moving backwards */
                Thread.sleep(100);
            }
            else {
                this.communication.setDigitalPin(6, false);
                Thread.sleep(100);
                this.communication.setDigitalPin(7, false);
                Thread.sleep(100);
            }
            
            this.communication.setDigitalPin(15, connected);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean setZFastConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(connected) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(8).getValueAsString().equals("0")) return false;     /* CHECK fast moving forward */
                if(!comp.get(9).getValueAsString().equals("0")) return false;     /* CHECK fast moving backwards */
                this.communication.setDigitalPin(10, false);             /* STOP slow moving forward */
                Thread.sleep(100);  //TODO: instead of a fixed sleep timer, check machine state for success before performing next step
                this.communication.setDigitalPin(11, false);             /* STOP slow moving backwards */
                Thread.sleep(100);
            }
            else {
                this.communication.setDigitalPin(8, false);
                Thread.sleep(100);
                this.communication.setDigitalPin(9, false);
                Thread.sleep(100);
                if(comp.get(25).getValueAsString().equals("0")) {
                    //THIS ENSURES THAT NOT BOTH CLIPS GET DISCONNECTED AT THE SAME TIME OR THE AXIS WILL FALL AND BREAK!!!
                    //TODO: IF ADDITIONAL "MANUAL MODE" SENSOR/PEDAL IS INTRODUCED AND ITS VALUE == 1, ALLOW OPENING THIS CLIP
                    return false;
                }
            }
            
            this.communication.setDigitalPin(16, connected);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean setZSlowConnected(boolean connected) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            if(connected) {
                if(comp.get(18).getValueAsString().equals("0")) return false;     /* CHECK no air pressure */
                if(!comp.get(10).getValueAsString().equals("0")) return false;     /* CHECK slow moving forward */
                if(!comp.get(11).getValueAsString().equals("0")) return false;     /* CHECK slow moving backwards */
                this.communication.setDigitalPin(8, false);             /* STOP fast moving forward */
                Thread.sleep(100);
                this.communication.setDigitalPin(9, false);             /* STOP fast moving backwards */
                Thread.sleep(100);
            }
            else {
                this.communication.setDigitalPin(10, false);
                Thread.sleep(100);
                this.communication.setDigitalPin(11, false);
                Thread.sleep(100);
                if(comp.get(24).getValueAsString().equals("0")) {
                    //THIS ENSURES THAT NOT BOTH CLIPS GET DISCONNECTED AT THE SAME TIME OR THE AXIS WILL FALL AND BREAK!!!
                    //TODO: IF ADDITIONAL "MANUAL MODE" SENSOR/PEDAL IS INTRODUCED AND ITS VALUE == 1, ALLOW OPENING THIS CLIP
                    return false;
                }
            }
            
            this.communication.setDigitalPin(17, connected);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveXToPosition(double position) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            
            if(!comp.get(19).getValueAsString().equals("0")) { System.out.println("X"); return false; }   /* CHECK sensing head contact */
            if(comp.get(18).getValueAsString().equals("0")) { System.out.println("A"); return false; }    /* CHECK no air pressure */
            if(!comp.get(0).getValueAsString().equals("0")) { System.out.println("B"); return false; }    /* CHECK fast moving forward */
            if(!comp.get(1).getValueAsString().equals("0")) { System.out.println("C"); return false; }    /* CHECK fast moving backwards */
            if(!comp.get(2).getValueAsString().equals("0")) { System.out.println("D"); return false; }    /* CHECK slow moving forward */
            if(!comp.get(3).getValueAsString().equals("0")) { System.out.println("E"); return false; }    /* CHECK slow moving backwards */

            this.communication.setDigitalPin(12, true);
            Thread.sleep(100);
            this.communication.setDigitalPin(13, false);
            Thread.sleep(100);
            this.communication.setAnalogValue("X", position);

            this.session.getProtocol().addLine("move x-position from " +this.getComponentValue(26) +" to " +position);

            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveYToPosition(double position) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            
            if(!comp.get(19).getValueAsString().equals("0")) { System.out.println("X"); return false; }   /* CHECK sensing head contact */
            if(comp.get(18).getValueAsString().equals("0")) { System.out.println("A"); return false; }    /* CHECK no air pressure */
            if(!comp.get(4).getValueAsString().equals("0")) { System.out.println("B"); return false; }    /* CHECK fast moving forward */
            if(!comp.get(5).getValueAsString().equals("0")) { System.out.println("C"); return false; }    /* CHECK fast moving backwards */
            if(!comp.get(6).getValueAsString().equals("0")) { System.out.println("D"); return false; }    /* CHECK slow moving forward */
            if(!comp.get(7).getValueAsString().equals("0")) { System.out.println("E"); return false; }    /* CHECK slow moving backwards */

            this.communication.setDigitalPin(14, true);
            Thread.sleep(100);
            this.communication.setDigitalPin(15, false);
            Thread.sleep(100);
            this.communication.setAnalogValue("Y", position);

            this.session.getProtocol().addLine("move y-position from " +this.getComponentValue(27) +" to " +position);

            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public boolean moveZToPosition(double position) throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.communication == null) throw new MachineUnreachableException();
        try {
            ArrayList<MachineComponent> comp = this.machine.getComponents();
            
            if(!comp.get(19).getValueAsString().equals("0")) { System.out.println("X"); return false; }   /* CHECK sensing head contact */
            if(comp.get(18).getValueAsString().equals("0")) { System.out.println("A"); return false; }    /* CHECK no air pressure */
            if(!comp.get(8).getValueAsString().equals("0")) { System.out.println("B"); return false; }    /* CHECK fast moving forward */
            if(!comp.get(9).getValueAsString().equals("0")) { System.out.println("C"); return false; }    /* CHECK fast moving backwards */
            if(!comp.get(10).getValueAsString().equals("0")) { System.out.println("D"); return false; }    /* CHECK slow moving forward */
            if(!comp.get(11).getValueAsString().equals("0")) { System.out.println("E"); return false; }    /* CHECK slow moving backwards */

            this.communication.setDigitalPin(16, true);
            Thread.sleep(100);
            this.communication.setDigitalPin(17, false);
            Thread.sleep(100);
            this.communication.setAnalogValue("Z", position);

            this.session.getProtocol().addLine("move z-position from " +this.getComponentValue(28) +" to " +position);
            
            return true;

        } catch (IncompatiblePinException | RemoteException | InterruptedException ex) {
            Logger.getLogger(OperationCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    @Override
    public boolean calibrateMachine() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.machine.getOperation() == EOperation.WAITING_FOR_CALIBRATION) {
            this.machine.setOperation(EOperation.CALIBRATION);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }
    
    @Override
    public boolean probePoint() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            this.machine.setOperation(EOperation.P3_PROBE_POINT);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }

    @Override
    public boolean probeInnerDiameter() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            this.machine.setOperation(EOperation.P3_PROBE_INNER_DIAMETER);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }

    @Override
    public boolean probeOuterDiameter() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            this.machine.setOperation(EOperation.P3_PROBE_OUTER_DIAMETER);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }

    @Override
    public boolean probePlane() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
                System.out.println("moveZFastForward"); // TODO eliminar
                this.currentAction="probePlane";
                if ( null == this.machine ) return true;
        if(this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            this.machine.setOperation(EOperation.P3_PROBE_PLANE);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }

    @Override
    public boolean probeDistancePlanePoint() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            this.machine.setOperation(EOperation.P3_PROBE_DISTANCE_PLANE_POINT);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }

    @Override
    public boolean probeDistanceParallelPlanes() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        if(this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            this.machine.setOperation(EOperation.P3_PROBE_DISTANCE_PARALLEL_PLANES);
            this.machine.setOperationState(EOperationState.SHAPE1_0_POINTS_MEASURED);
            return true;
        }
        else return false;
    }

    @Override
    public boolean cancelOperation() {
        if(this.machine.getOperation() == EOperation.WAITING_FOR_CALIBRATION || this.machine.getOperation() == EOperation.WAITING_ALREADY_CALIBRATED) {
            return false;
        }
        else if(this.machine.getOperation() == EOperation.CALIBRATION) {
            this.machine.setOperation(EOperation.WAITING_FOR_CALIBRATION);
        }
        else {
            this.machine.setOperation(EOperation.WAITING_ALREADY_CALIBRATED);
        }
        //TODO: ?IMPLEMENT A PROTOCOL COMMAND "DEF" THAT RE-SETS ALL MACHINE-ACTUATORS TO THEIR DEFAULT VALUES?
        this.measuredPoints.clear();
        this.machine.setOperationState(EOperationState.WAITING);
        this.session.getProtocol().addLine("THE OPERATION HAS BEEN CANCELED!");
        return true;
    }

    /**
     * Does alle the geometric operations, a function library might be used instead of programming the calculations.
     * e.g. http://stackoverflow.com/questions/2115976/geometry-library-for-java
     * 
     * @return 
     */
    @Override
    public boolean finishOperation() {
        switch(this.machine.getOperation()) {
            case CALIBRATION:
                if(this.machine.getOperationState() == EOperationState.SHAPE1_SUFFICIENT_POINTS_MEASURED) {
                    this.machine.setPosXZero(this.measuredPoints.get(0).getX());
                    this.machine.setPosYZero(this.measuredPoints.get(1).getY());
                    this.machine.setPosZZero(this.measuredPoints.get(2).getZ());
                    this.session.getProtocol().addLine("Calibration finished, machines 0-positions set to (" 
                            +this.machine.getPosXZero() +" | " +this.machine.getPosYZero() 
                            +" | " +this.machine.getPosZZero() +")!");
                }
                break;
            case P3_PROBE_POINT:
                if(this.machine.getOperationState() == EOperationState.SHAPE1_SUFFICIENT_POINTS_MEASURED) {
                    this.session.getProtocol().addLine("Point probed, coordinates (" 
                            +this.measuredPoints.get(0).getX() +" | " +this.measuredPoints.get(1).getY()
                            +" | " +this.measuredPoints.get(2).getZ() +")!");
                }                
                break;
            case P3_PROBE_INNER_DIAMETER:
                if(this.machine.getOperationState() == EOperationState.SHAPE1_SUFFICIENT_POINTS_MEASURED) {
                    
                    /* Calculate outer circle central point and diameter */
                    /* SIMPLIFYING ASSUMPTION: Circle is assumed to be perfectly horizontally orientated (z-axis is ignored) */
                    /* Instructional example (German) here:
                        http://www.matheprofi.at/Umkreismittelpunkt%20eines%20Dreiecks.pdf
                        http://www.arndt-bruenner.de/mathe/scripts/Dreiecksberechnung.htm
                       Instructions in English:
                        http://paulbourke.net/geometry/circlesphere/
                    */
                    /* Step 1: Calculate length of triangle sides */
                    Vector a, b, c;
                    a = new Vector(
                            (this.measuredPoints.get(2).getX() - this.measuredPoints.get(1).getX())*
                            (this.measuredPoints.get(2).getX() - this.measuredPoints.get(1).getX()),
                            (this.measuredPoints.get(2).getY() - this.measuredPoints.get(1).getY())*
                            (this.measuredPoints.get(2).getY() - this.measuredPoints.get(1).getY())
                    );
                    b = new Vector(
                            (this.measuredPoints.get(2).getX() - this.measuredPoints.get(0).getX())*
                            (this.measuredPoints.get(2).getX() - this.measuredPoints.get(0).getX()),
                            (this.measuredPoints.get(2).getY() - this.measuredPoints.get(0).getY())*
                            (this.measuredPoints.get(2).getY() - this.measuredPoints.get(0).getY())
                    );
                    c = new Vector(
                            (this.measuredPoints.get(1).getX() - this.measuredPoints.get(0).getX())*
                            (this.measuredPoints.get(1).getX() - this.measuredPoints.get(0).getX()),
                            (this.measuredPoints.get(1).getY() - this.measuredPoints.get(0).getY())*
                            (this.measuredPoints.get(1).getY() - this.measuredPoints.get(0).getY())
                    );

                    /* Step 2: Calculate bisectors of triangle sides */
                    Position ha = new Position( 0.5*(this.measuredPoints.get(1).getX()+this.measuredPoints.get(2).getX()),
                                                0.5*(this.measuredPoints.get(1).getY()+this.measuredPoints.get(2).getY()),
                                                0d);
                    Position hb = new Position( 0.5*(this.measuredPoints.get(0).getX()+this.measuredPoints.get(2).getX()),
                                                0.5*(this.measuredPoints.get(0).getY()+this.measuredPoints.get(2).getY()),
                                                0d);
                    
                    /* Step 3: Calculate normal vector towards bisectors */
                    Vector na = new Vector(a.getX(), a.getY(), a.getX()*ha.getX()+a.getY()*ha.getY());
                    Vector nb = new Vector(b.getX(), b.getY(), b.getX()*hb.getX()+b.getY()*hb.getY());

                    /* Step 4: Calculate position of intersection of normal vectors by resolving equation system */
                    Vector step = new Vector(na.getX()*nb.getY()-na.getY()*nb.getX(), 
                                             0d, 
                                             na.getValue()*nb.getY()-na.getY()*nb.getValue()
                    );
                    double x = step.getValue()/step.getX();
                    double y = (na.getValue()-na.getX()*x)/na.getY();
                    
                    Position centerpoint = new Position(x,y,this.measuredPoints.get(0).getZ());
                                                        
                    
                    /* Step 5: Calculate angle alpha */
                    double alpha = Math.acos((a.getValue() * a.getValue() - b.getValue() * b.getValue() 
                            - c.getValue() * c.getValue()) / (-2 * b.getValue() * c.getValue()));
                    
                    /* Step 6: Calculate radius of outer circle */
                    double radius = a.getValue() / (2 * Math.sin(alpha));
                    
                    this.session.getProtocol().addLine("Diameter measured, circle with center (" +centerpoint.getX() +", "
                            +centerpoint.getY() +", " +centerpoint.getZ() +") and diameter " +(radius*2) +"!");
                }
                break;
            case P3_PROBE_OUTER_DIAMETER:
                break;
            case P3_PROBE_PLANE:
                if(this.machine.getOperationState() == EOperationState.SHAPE1_SUFFICIENT_POINTS_MEASURED) {
                    
                    /* Calculate orientation of a plane given by three points */
                    /* Instruction in German: http://www.matheboard.de/archive/517950/thread.html */
                    /* Step 1: Calculate general coordinate equation 
                               by solving formula system of specific coordinate equations
                               and read normal vector out of general coordinate equation */
                    /* Step 2: Calculate angle between normal vector and the three axis separately */
                }
                break;
            case P3_PROBE_DISTANCE_PLANE_POINT:
                /* Instruction in English: http://mathworld.wolfram.com/Point-PlaneDistance.html */
                break;
            case P3_PROBE_DISTANCE_PARALLEL_PLANES:
                break;
            default:
                return false;
        }
        this.measuredPoints.clear();
        this.machine.setOperation(EOperation.WAITING_ALREADY_CALIBRATED);
        this.machine.setOperationState(EOperationState.WAITING);
        return true;
    }
    
    @Override
    public boolean measurePosition() throws MachineUnreachableException, MachineOperationTemporarilyForbiddenException {
        switch(this.machine.getOperation()) {
            case WAITING_FOR_CALIBRATION:
            case WAITING_ALREADY_CALIBRATED:
                break;
            case CALIBRATION:
            case P3_PROBE_POINT:
                switch(this.machine.getOperationState()) {
                    case SHAPE1_0_POINTS_MEASURED:
                        this.measuredPoints.add(0, new Position(Double.parseDouble(this.getComponentValue(26)), 
                                Double.parseDouble(this.getComponentValue(27)), Double.parseDouble(this.getComponentValue(28))));
                        this.machine.setOperationState(EOperationState.SHAPE1_1_POINT_MEASURED);
                        break;
                    case SHAPE1_1_POINT_MEASURED:
                        this.measuredPoints.add(1, new Position(Double.parseDouble(this.getComponentValue(26)), 
                                Double.parseDouble(this.getComponentValue(27)), Double.parseDouble(this.getComponentValue(28))));
                        this.machine.setOperationState(EOperationState.SHAPE1_2_POINTS_MEASURED);
                        break;
                    case SHAPE1_2_POINTS_MEASURED:
                        this.measuredPoints.add(2, new Position(Double.parseDouble(this.getComponentValue(26)), 
                                Double.parseDouble(this.getComponentValue(27)), Double.parseDouble(this.getComponentValue(28))));
                        this.machine.setOperationState(EOperationState.SHAPE1_SUFFICIENT_POINTS_MEASURED);
                        this.finishOperation();
                        break;
                }
                break;
            case P3_PROBE_INNER_DIAMETER:
            case P3_PROBE_OUTER_DIAMETER:
            case P3_PROBE_PLANE:
                this.measuredPoints.add(new Position(Double.parseDouble(this.getComponentValue(26)), 
                        Double.parseDouble(this.getComponentValue(27)), Double.parseDouble(this.getComponentValue(28))));
                switch(this.machine.getOperationState()) {
                    case SHAPE1_0_POINTS_MEASURED:
                        this.machine.setOperationState(EOperationState.SHAPE1_1_POINT_MEASURED);
                        break;
                    case SHAPE1_1_POINT_MEASURED:
                        this.machine.setOperationState(EOperationState.SHAPE1_2_POINTS_MEASURED);
                        break;
                    case SHAPE1_2_POINTS_MEASURED:
                        this.machine.setOperationState(EOperationState.SHAPE1_SUFFICIENT_POINTS_MEASURED);
                        this.finishOperation(); //TODO: IMPLEMENT FUNCTIONALITY TO CALCULATE SHAPE WITH MORE POINTS MEASURED!!
                        break;
                    default:
                        break;
                }
                break;
            case P3_PROBE_DISTANCE_PLANE_POINT:
                break;
            case P3_PROBE_DISTANCE_PARALLEL_PLANES:
                break;
            default:
                break;
        }
        return true;
    }

    public Machine getMachine() {
        return this.machine;
    }
    
    private String getComponentValue(int index) {
        ArrayList<MachineComponent> comp = this.machine.getComponents();
        if(index >= comp.size())
            return "INVALID COMPONENT NUMBER";
        return comp.get(index).getValueAsString();
    }

    public void setLoginCtrl(LoginCtrl loginCtrl) {
        this.loginCtrl = loginCtrl;
    }

    public void setMainCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    public List<Trace> getTraces() {
        return traces;
    }

    public void setTraces(List<Trace> traces) {
        this.traces = traces;
    }
    
        
    public String getCurrentAction() {
        return this.currentAction;
    }
    
    public void setCurrentAction(String currentAction){
        this.currentAction = currentAction;
    }

    public Double getGoPosX() {
        return goPosX;
    }

    public void setGoPosX(Double goPosX) {
        this.goPosX = goPosX;
    }

    public Double getGoPosY() {
        return goPosY;
    }

    public void setGoPosY(Double goPosY) {
        this.goPosY = goPosY;
    }

    public Double getGoPosZ() {
        return goPosZ;
    }

    public void setGoPosZ(Double goPosZ) {
        this.goPosZ = goPosZ;
    }

    public Boolean getMovingX() {
        return movingX;
    }

    public void setMovingX(Boolean movingX) {
        this.movingX = movingX;
    }

    public Boolean getMovingY() {
        return movingY;
    }

    public void setMovingY(Boolean movingY) {
        this.movingY = movingY;
    }

    public Boolean getMovingZ() {
        return movingZ;
    }

    public void setMovingZ(Boolean movingZ) {
        this.movingZ = movingZ;
    }

    


}
