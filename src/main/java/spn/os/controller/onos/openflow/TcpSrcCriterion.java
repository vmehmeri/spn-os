package spn.os.controller.onos.openflow;

public class TcpSrcCriterion extends Criterion {
	
	private int tcpPort;
	
	public TcpSrcCriterion(int tcpPort) {
		super(Type.TCP_SRC);
		//This is redundant but needed for GSON serialization:
		this.type = Type.TCP_SRC;
		this.tcpPort = tcpPort;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
}
