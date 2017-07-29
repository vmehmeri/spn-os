package spn.os.element;

import spn.os.element.MACAddress;

public abstract class Port {
	protected int number;
	protected MACAddress mac;
	protected Vertex vertex;
	protected boolean ingress;
	protected boolean egress;

	public Port() {
		this.number = 0;
		this.mac = new MACAddress("00:00:00:00:00:00");
		this.vertex = null;
		this.ingress = false;
		this.egress = false;
	}

	public Port(int number, MACAddress mac, Vertex vertex) {
		this.number = number;
		this.mac = mac;
		this.vertex = vertex;
	}

	public Port(int number, MACAddress mac, Vertex vertex, boolean ingress,
			boolean egress) {
		this.number = number;
		this.mac = mac;
		this.vertex = vertex;
		this.ingress = ingress;
		this.egress = egress;
	}

	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public MACAddress getMac() {
		return this.mac;
	}

	public void setMac(MACAddress mac) {
		this.mac = mac;
	}

	public Vertex getVertex() {
		return this.getVertex();
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public boolean isIngress() {
		return this.ingress;
	}

	public void setIngress(boolean i) {
		this.ingress = i;
	}

	public boolean isEgress() {
		return this.egress;
	}

	public void setEgress(boolean i) {
		this.egress = i;
	}

	public boolean isInternal() {
		return (!this.ingress && !this.egress);
	}

	public String toString() {
		return Integer.toString(this.number);
	}

	public Port copy(Vertex v) throws InstantiationException,
			IllegalAccessException {
		Class<? extends Port> c = this.getClass();
		Port p = (Port) c.newInstance();
		p.setVertex(v);
		return p;
	}

	public String prettyPrint() {
		StringBuilder pretty = new StringBuilder().append("Port number: ")
				.append(this.number);
		if (ingress)
			pretty.append(" (ingress/egress)");
		
		return pretty.toString();
	}
}
