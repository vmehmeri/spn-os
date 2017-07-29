package spn.os.element;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import spn.os.element.Port;
import spn.exception.InvalidPortNumberException;

public abstract class Vertex {

	protected BigInteger id;
	protected String name;
	protected Map<Integer, Port> ports;

	public Vertex() {
		this.id = BigInteger.ZERO;
		this.name = "";
		this.ports = new HashMap<Integer, Port>();
	}

	public Vertex(BigInteger id, String name) {
		this.id = id;
		this.name = name;
		this.ports = new HashMap<Integer, Port>();
	}

	public String getName() {
		return this.name;
	}
	
	public BigInteger getId() {
		return this.id;
	}

	public String getDeviceId() {
		String deviceId = String.format("of:%016d", this.id.intValue());
//		System.out.println(">>> device id: " + deviceId);
		return deviceId;
	}

	public boolean hasPort(int id) {
		return ports.containsKey(id);
	}

	public Port getPort(int id) throws InvalidPortNumberException {
		if (!ports.containsKey(id))
			throw new InvalidPortNumberException("No port numbered " + id);
		return ports.get(id);
	}

	public Collection<Port> getPorts() {
		return this.ports.values();
	}

	public void addPort(Port port) {
		this.ports.put(port.getNumber(), port);
	}

	public boolean equals(Vertex v) {
		return this.id == v.getId();
	}

	// Update the ports of this vertex with that of a given vertex
	public void update(Vertex v) {
		Collection<Port> ps = v.getPorts();
		if (ps == null) {
			System.out.println("No ports");
			return;
		}
		for (Port p : v.getPorts()) {
			if (p == null)
				System.out.println("Null port");
			if (ports == null)
				System.out.println("ports null");
			if (!ports.containsKey(p.getNumber())) {
				try {
					Port port = p.copy(v);
					this.ports.put(port.getNumber(), port);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				// if this port is already present, update the ingress and
				// egress properties
				Port port = this.ports.get(p.getNumber());
				port.setIngress(port.isIngress() || p.isIngress());
				port.setEgress(port.isEgress() || p.isEgress());
			}
		}
	}

	public String prettyPrint() {
		StringBuilder pretty = new StringBuilder().append("Vertex ID: ")
				.append(id.toString()).append(" Vertex name: ").append(name);
		pretty.append(" | List of ports:\n");
		for (Port p : ports.values()) {
			pretty.append(" ").append(p.prettyPrint()).append("\n");
		}
		return pretty.toString();
	}
}
