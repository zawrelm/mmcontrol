package mmcontrol.uicontrol.model;

/**
 *
 * @author Michael Zawrel
 */
public class ProbeProgramExecution extends UserMachineSession {

    private ProbeProgram program;
    
    public ProbeProgramExecution(long machineId) {
        super(machineId);
    }
    
    public ProbeProgramExecution(long machineId, ProbeProgram program) {
        super(machineId);
        this.program = program;
    }

    public ProbeProgram getProgram() {
        return program;
    }

    public void setProgram(ProbeProgram program) {
        this.program = program;
    }
    
}
