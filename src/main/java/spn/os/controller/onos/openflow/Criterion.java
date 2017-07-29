package spn.os.controller.onos.openflow;


public abstract class Criterion {

	protected Type type;
	
	public Criterion(Type type) {
		
	}
	
	public Type getType() {
		return type;
	}
	
	public enum Type {
		ETH_TYPE,
		ETH_DST,
		ETH_SRC,
		IN_PORT,
		IN_PHY_PORT,
		METADATA,
		VLAN_VID,
		VLAN_PCP,
		INNER_VLAN_VID,
		INNER_VLAN_PCP,
		IP_DSCP,
		IP_ECN,
		IP_PROTO,
		IPV4_SRC,
		IPV4_DST,
		TCP_SRC,
		TCP_DST,
		UDP_SRC,
		UDP_DST,
		SCTP_SRC,
		SCTP_DST,
		ICMPV4_TYPE,
		ICMPV4_CODE,
		IPV6_SRC,
		IPV6_DST,
		IPV6_FLABEL,
		ICMPV6_TYPE,
		ICMPV6_CODE,
		IPV6_ND_TARGET,
		IPV6_ND_SLL,
		IPV6_ND_TLL,
		MPLS_LABEL,
		IPV6_EXTHDR,
		OCH_SIGID,
		CHANNEL_SPACING,
		SPACING_MULTIPLIER,
		SLOT_GRANULARITY,
		TUNNEL_ID,
		OCH_SIGTYPE,
		ODU_SIGID,
		ODU_SIGTYPE
		
		
	};
	
}
