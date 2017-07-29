package spn.os.controller.onos.openflow;


public class OutputInstruction extends Instruction {

	private String port;
	
	public OutputInstruction(String port) {
		super(Type.OUTPUT);
		//This is redundant but needed for GSON serialization:
		this.type = Type.OUTPUT;
		this.port = port;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
