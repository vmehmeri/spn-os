package spn.os.controller.onos.openflow;


public class Ipv4SrcCriterion extends Criterion {
	
	private String ip;
	
	public Ipv4SrcCriterion(String ip) {
		super(Type.IPV4_SRC);
		//This is redundant but needed for GSON serialization:
		this.type = Type.IPV4_SRC;
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
