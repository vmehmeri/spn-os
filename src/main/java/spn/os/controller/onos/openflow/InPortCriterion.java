package spn.os.controller.onos.openflow;


public class InPortCriterion extends Criterion {
	
	private String port;
	
	public InPortCriterion(String port) {
		super(Type.IN_PORT);
		//This is redundant but needed for GSON serialization:
		this.type = Type.IN_PORT;
		this.port = port;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
