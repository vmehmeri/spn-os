package spn.os.controller.onos.openflow;


public class Ipv4SrcModificationInstruction extends Instruction {

	private String ip;
	
	public Ipv4SrcModificationInstruction(String ip) {
		super(Type.L3MODIFICATION, Subtype.IPV4_SRC);
		//This is redundant but needed for GSON serialization:
		this.type = Type.L3MODIFICATION;
		this.subtype = Subtype.IPV4_SRC;
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
