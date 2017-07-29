package spn.os.element;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.math.BigInteger;

import com.google.gson.Gson;

import spn.os.Console;
/**
 * Base class for all networks
 * @author Shrutarshi Basu <shrutarshi.basu@us.fujitsu.com>
 * @author Cong Chen <Cong.Chen@us.fujitsu.com>
 *
 */
@SuppressWarnings("rawtypes")
public abstract class Network {
    protected Map<BigInteger, Vertex>    vertices;
    protected Map<BigInteger, Host>     hosts;
    protected Map<BigInteger, Switch>   switches;      // Map of dpid to switch objects
    protected Map<BigInteger, Link>     links;
    protected String jsonStr;
    protected Console console = Console.getConsole(this);
    
    public Network() {
        this.vertices = new HashMap<BigInteger, Vertex>();
        this.hosts    = new HashMap<BigInteger, Host>();
        this.switches = new HashMap<BigInteger, Switch>();
        this.links    = new HashMap<BigInteger, Link>();
    }

    public Network(Set<? extends Host> hosts, Set<? extends Switch> switches,
                   Set<? extends Link> links) {
        this.hosts = new HashMap<BigInteger, Host>();
        for (Host host : hosts) {
            this.addHost(host);
        };
        this.switches = new HashMap<BigInteger, Switch>();
        for(Switch sw : switches) {
            this.addSwitch(sw);
        }
        this.links = new HashMap<BigInteger, Link>();
        for(Link l : links) {
            this.addLink(l);
        }
    }
    
    public void setJsonStr(String jsonStr) {
    	this.jsonStr = jsonStr;
    }

    public void addVertex(Vertex v) {
        if(v != null) {
            vertices.put(v.getId(), v);
            if (v instanceof Switch)
                this.addSwitch((Switch)v);
            if (v instanceof Host)
                this.addHost((Host)v);
        }
    }

    public Vertex getVertex(BigInteger i) {
        return vertices.get(i);
    }

    public void addSwitch(Switch sw) {
        if(sw != null) {
            vertices.put(sw.getId(), sw);
            switches.put(sw.getId(), sw);
        } else System.out.println("Null switch");
    }

    public void removeSwitch(Switch sw) {
        if(sw != null)
            switches.remove(sw.getId());
    }

    public Switch getSwitch(BigInteger i) {
        return switches.get(i);
    }

    public Collection<? extends Switch> getSwitches() {
        return switches.values();
    }

    public void addHost(Host h) {
        if(h != null) {
            vertices.put(h.getId(), h);
            hosts.put(h.getId(), h);
        }
    }

    public void removeHost(Host h) {
        if (h != null) {
            hosts.remove(h.getId());
        }
    }

    public Host getHost(BigInteger id) {
        return hosts.get(id);
    }
    
    public Map<BigInteger, Host> getHosts() {
    	return hosts;
    }

    public void addLink(Link link) {
        if(link != null) {
            Vertex src = this.vertices.get(link.getSrcVertex().getId());
            if (src == null)
                this.addVertex(link.getSrcVertex());
            //       else
            //           src.update(link.getSrcVertex());

            Vertex dst = this.vertices.get(link.getDstVertex().getId());
            if (dst == null)
                this.addVertex(link.getDstVertex());
            //     else
            //       src.update(link.getDstVertex());

            links.put(link.getId(), link);
        }
    }

    public void removeLink(Link link) {
        if(link != null)
            links.remove(link);
    }

    public Link getLink(BigInteger id) {
        return links.get(id);
    }

    // TODO(basus): this is very inefficient and should be replaced by a proper
    // link representation
    public Link getLink(BigInteger srcId, int srcPortId,
                        BigInteger dstId, int dstPortId) {
        for (Map.Entry<BigInteger, Link> entry : this.links.entrySet()) {
            Link link = entry.getValue();
            if ((link.getSrcVertex().getId().equals(srcId)) &&
                (link.getSrcPort().getNumber() == srcPortId) &&
                (link.getDstVertex().getId().equals(dstId)) &&
                (link.getDstPort().getNumber() == dstPortId))
                return link;
        }
        return null;
    }

    public Location getLocation(BigInteger vertexId, int portId) {
        Vertex vertex = vertices.get(vertexId);
        Port port = vertex.getPort(portId);
        if ( port != null && vertex != null)
            return new Location(vertex, port, port.isIngress(), port.isEgress());
        else
            return null;
    }

    public OpticalLocation getOpticalLocation(BigInteger vertexId, int portId, Short wavelength) {
        Vertex vertex = vertices.get(vertexId);
        Port port = vertex.getPort(portId);
        if ( port != null && vertex != null)
            return new OpticalLocation(vertex, port, wavelength, port.isIngress(), port.isEgress());
        else
            return null;
    }

    public int numVertices() {
        return vertices.size();
    }

    public int numHosts() {
        return hosts.size();
    }

    public int numSwitches() {
        return switches.size();
    }

    public int numLinks() {
        return this.links.size();
    }

    // TODO(basus) These should probably be removed in favor of having ingress
    // and egress fields in the object, but that would need rewriting the parser too.
    public Set<Port> getIngresses() {
        HashSet<Port> set = new HashSet<Port>();
        for (Switch sw : this.switches.values()) {
            for (Object p : sw.getPorts())
                if (((Port) p).isIngress())
                    set.add((Port) p);
        }
        return set;
    }

    public Set<Port> getEgresses() {
        HashSet<Port> set = new HashSet<Port>();
        for (Switch sw : this.switches.values())
            for (Object p : sw.getPorts())
                if (((Port) p).isEgress())
                    set.add((Port) p);
        return set;
    }

    // Add the ingress ports from a given network to this one, assuming the
    // corresponding vertices exist in this one.
    public void addIngresses(Network network) {
//    	console.debug("Adding ingresses to the network");
    	
        for (Map.Entry<BigInteger, Switch> entry : this.switches.entrySet()) {
            Switch sw = entry.getValue();
            if (network == null)  {
            	console.error("Network is null!");
            	return;
            }
            Switch other = network.getSwitch(sw.getId());
            if (other != null) {
                for (Object o : other.getPorts()) {
                    Port p = (Port) o;
                    if (p.isIngress()) {
//                		console.debug( 
//                    			String.format("Port %d of switch %d is an ingress", 
//                    					p.getNumber(),sw.getId()) );
                        sw.addPort(new PacketPort(p.getNumber(), p.getMac(), sw,
                                                  p.isIngress(), p.isEgress()));
                    }
                }
            }
        }
    }

    public void addEgresses(Network network) {
        for (Map.Entry<BigInteger, Switch> entry : this.switches.entrySet()) {
            Switch sw = entry.getValue();
            Switch other = network.getSwitch(sw.getId());
            if (other != null) {
                for (Object o : other.getPorts()) {
                    Port p = (Port) o;
                    if (p.isEgress()) {
//                    	console.debug( 
//                    			String.format("Port %d of switch %d is an egress", 
//                    					p.getNumber(),sw.getId()) );
                        sw.addPort(new PacketPort(p.getNumber(), p.getMac(), sw,
                                                  p.isIngress(), p.isEgress()));
                    }
                }
            }
        }
    }


    public String getIngressPredicate(String switchField, String portField) {
        String result = "";
        int i = 0;
        for (Switch sw : this.switches.values()) {
            Collection ports = sw.getPorts();
            for (Object p : ports) {
                if (((Port) p).isIngress()) {                 	
                    if (i!= 0)
                        result = result + " or\n";
                    result = result + "(" + switchField + "=" +
                        sw.getId().toString() + " and " + portField + "=" +
                        ((Port) p).getNumber() + ")";
                    i = 1;
                }
            }
        }
        return result;
    }

    public String getEgressPredicate(String switchField, String portField) {
        String result = "";
        int i = 0;
        for (Switch sw : this.switches.values()) {
            Collection ports = sw.getPorts();
            for (Object p : ports) {
                if (((Port) p).isEgress()) { 
                	if (i!= 0)
                        result = result + " or\n";
                    result = result + "(" + switchField + "=" +
                        sw.getId().toString() + " and " + portField + "=" +
                        ((Port) p).getNumber() + ")";
                    i = 1;
                }
            }
        }
        return result;
    }

    public String toNetKAT(String arrow) {
        String result = "";
        int i = 0;

        for (Link link : this.links.values()) {
            boolean src = this.switches.containsKey(link.srcVertex.getId());
            boolean dst = this.switches.containsKey(link.getDstVertex().getId());
            if (src && dst) {
                if (i != 0) {
                    result += " | \n";
                }
                // Note: This assumes that links are bidirectional
                result = result + link.getSrcVertex().getId() + "@" + link.getSrcPort().getNumber() +
                    arrow + link.getDstVertex().getId() + "@" + link.getDstPort().getNumber();
                result += " | ";
                result = result + link.getDstVertex().getId() + "@" + link.getDstPort().getNumber() +
                    arrow + link.getSrcVertex().getId() + "@" + link.getSrcPort().getNumber();
                i = 1;
            }
        }

        if (result == "")
            return "drop";
        else
            return result;

    }
    
//    public void updateIngresses(Set<Port> ingressPorts) {
//    	for (Map.Entry<BigInteger, Switch> entry : this.switches.entrySet()) {
//            Switch sw = entry.getValue();
//            console.debug("Updating ingresses for switch " + sw.getId().toString());
//            for (Port p : sw.getPorts()) {
//                if (ingressPorts.contains(p)) {
//                	console.debug( String.format("Port #%d is ingress", p.getNumber()) );
//                    p.setIngress(true);
//                }
//                else {
//                	p.setIngress(false);
//                }
//            }
//            
//        }
//    }
//    
//    public void updateEgresses(Set<Port> egressPorts) {
//    	for (Map.Entry<BigInteger, Switch> entry : this.switches.entrySet()) {
//            Switch sw = entry.getValue();
//            console.debug("Updating egresses for switch " + sw.getId().toString());
//            
//            for (Port p : sw.getPorts()) {
//                if (egressPorts.contains(p))
//                    p.setEgress(true);
//                else
//                	p.setEgress(false);
//            }
//            
//        }
//    }
}
