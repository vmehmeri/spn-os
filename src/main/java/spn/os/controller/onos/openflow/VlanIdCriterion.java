package spn.os.controller.onos.openflow;

public class VlanIdCriterion extends Criterion {
	
	private String vlanId;
	
	public VlanIdCriterion(String vlanId) {
		super(Type.VLAN_VID);
		//This is redundant but needed for GSON serialization:
		this.type = Type.VLAN_VID;
		this.vlanId = vlanId;
	}

	public String getVlanId() {
		return vlanId;
	}

	public void setVlanId(String vlanId) {
		this.vlanId = vlanId;
	}

}
