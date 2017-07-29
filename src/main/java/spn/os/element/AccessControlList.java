package spn.os.element;

public class AccessControlList {
	private String netName;
	private int destPort;
	private String ipProto;
	
	public AccessControlList() {
		
	}
	
	public AccessControlList(String netName, int destPort, String ipProto) {
		this.netName = netName;
		this.destPort = destPort;
		this.ipProto = ipProto;
	}

	public String getNetName() {
		return netName;
	}
	
	public void setNetName(String netName) {
		this.netName = netName;
	}
	
	public int getDestPort() {
		return destPort;
	}
	
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	
	public String getIpProto() {
		return ipProto;
	}
	
	public void setIpProto(String ipProto) {
		this.ipProto = ipProto;
	}
	
	
}
