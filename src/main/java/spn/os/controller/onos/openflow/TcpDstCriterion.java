package spn.os.controller.onos.openflow;


public class TcpDstCriterion extends Criterion {
	
	private int tcpPort;
	
	public TcpDstCriterion(int tcpPort) {
		super(Type.TCP_DST);
		//This is redundant but needed for GSON serialization:
		this.type = Type.TCP_DST;
		this.tcpPort = tcpPort;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

}
