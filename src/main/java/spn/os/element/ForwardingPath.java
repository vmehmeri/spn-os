package spn.os.element;

import java.util.ArrayList;
import java.util.List;

public class ForwardingPath {
	private String name;
	private List<ForwardingNode> nodes;
	private AccessControlList acl;
	
	public ForwardingPath() {
		nodes = new ArrayList<ForwardingNode>();
	}
	
	public String getName() {
		return name;
	}
	
	public ForwardingPath setName(String name) {
		this.name = name;
		return this;
	}
	
	public List<ForwardingNode> getNodes() {
		return nodes;
	}
	
	public ForwardingPath setNodes(List<ForwardingNode> nodes) {
		this.nodes = nodes;
		return this;
	}
	
	public ForwardingPath addNode(ForwardingNode node) {
		this.nodes.add(node);
		return this;
	}
	
	public ForwardingPath setAclPolicy(AccessControlList acl) {
		this.acl = acl;
		return this;
	}
	
	public AccessControlList getAclPolicy( ) {
		return this.acl;
	}
	
	public void printToConsole() {
		System.out.println("Forwarding Path: " + this.name);
		System.out.println("ACL:");
		System.out.println("net name:" + this.acl.getNetName());
		System.out.println("dest port:" + Integer.toString(this.acl.getDestPort()));
		System.out.println("ip proto:" + this.acl.getIpProto());
		for (ForwardingNode node : nodes) {
			node.printToConsole();
			System.out.println("------------------------");
		}
	}
}
