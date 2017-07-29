package spn.os.controller.onos.openflow;

import java.util.ArrayList;
import java.util.List;

public class Treatment {
	private List<Instruction> instructions;
	
	public Treatment(List<Instruction> instructions) {
		this.instructions = instructions;
	}
	
	public Treatment() {
		this.instructions = new ArrayList<Instruction>();
	}
	
	public void addInstruction(Instruction i) {
		this.instructions.add(i);
	}

}
