package spn.os.controller.onos.openflow;


public class VlanModificationInstruction extends Instruction {
	
	private int vlanId;
	
	public VlanModificationInstruction(int vlanId) {
		super(Type.L2MODIFICATION, Subtype.VLAN_ID);
		//This is redundant but needed for GSON serialization:
		this.type = Type.L2MODIFICATION;
		this.subtype = Subtype.VLAN_ID;
		this.vlanId = vlanId;
	}

	public int getVlanId() {
		return vlanId;
	}

	public void setVlanId(int vlanId) {
		this.vlanId = vlanId;
	}

}
