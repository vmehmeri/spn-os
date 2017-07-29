package spn.os.controller.onos.openflow;

public abstract class Instruction {
	
	protected Type type;
	protected Subtype subtype;
	
	public Instruction(Type type) {
		
	}
	
	public Instruction(Type type, Subtype subtype) {
		
	}
	
	public enum Type {
		OUTPUT,
		TABLE,
		GROUP,
		METER,
		QUEUE,
		L0MODIFICATION,
		L1MODIFICATION,
		L2MODIFICATION,
		L3MODIFICATION,
		L4MODIFICATION
	};
	
	public enum Subtype {
		LAMBDA,
		OCH,
		ODU_SIGID,
		VLAN_PUSH,
		VLAN_POP,
		VLAN_ID,
		VLAN_PCP,
		ETH_SRC,
		ETH_DST,
		MPLS_LABEL,
		MPLS_PUSH,
		TUNNEL_ID,
		IPV4_SRC,
		IPV4_DST,
		IPV6_SRC,
		IPV6_DST,
		IPV6_FLABEL,
		TCP_SRC,
		UDP_SRC
	}

}
