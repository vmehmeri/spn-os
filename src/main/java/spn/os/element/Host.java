package spn.os.element;

import java.math.BigInteger;

public class Host extends Vertex {
	
	private String mgmtIpAddress;
	private String dpIpAddress;
	private ForwardingNode.Domain domain;
	
    public Host() {
        super();
    }

    public Host(BigInteger id, String name)
    {
        super(id, name);
    }
	
    public String getMgmtIpAddress() {
		return mgmtIpAddress;
	}

	public void setMgmtIpAddress(String mgmtIpAddress) {
		this.mgmtIpAddress = mgmtIpAddress;
	}

	public String getDpIpAddress() {
		return dpIpAddress;
	}

	public void setDpIpAddress(String dpIpAddress) {
		this.dpIpAddress = dpIpAddress;
	}

	public ForwardingNode.Domain getDomain() {
		return domain;
	}

	public void setDomain(ForwardingNode.Domain domain) {
		this.domain = domain;
	}


}
