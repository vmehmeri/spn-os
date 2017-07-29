package spn.os.controller.onos.openflow;



public class Ipv4DstCriterion extends Criterion {
	
	private String ip;
	
	public Ipv4DstCriterion(String ip) {
		super(Type.IPV4_DST);
		//This is redundant but needed for GSON serialization:
		this.type = Type.IPV4_DST;
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
