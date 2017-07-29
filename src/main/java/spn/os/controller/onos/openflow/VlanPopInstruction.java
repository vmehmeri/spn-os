package spn.os.controller.onos.openflow;



public class VlanPopInstruction extends Instruction {

	public VlanPopInstruction() {
		super(Type.L2MODIFICATION, Subtype.VLAN_POP);
		//This is redundant but needed for GSON serialization:
		this.type = Type.L2MODIFICATION;
		this.subtype = Subtype.VLAN_POP;
	}

}
