package spn.os.controller.onos.openflow;



public class VlanPushInstruction extends Instruction {

	public VlanPushInstruction() {
		super(Type.L2MODIFICATION, Subtype.VLAN_PUSH);
		//This is redundant but needed for GSON serialization:
		this.type = Type.L2MODIFICATION;
		this.subtype = Subtype.VLAN_PUSH;
	}

}
