package spn.os;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import spn.exception.InvalidConfigurationException;
import spn.exception.InvalidMappingException;
import spn.exception.InvalidHopException;
import spn.netkat.And;
import spn.netkat.Filter;
import spn.netkat.Modification;
import spn.netkat.Policy;
import spn.netkat.Sequence;
import spn.netkat.Test;
import spn.netkat.Union;
import spn.os.element.Link;
import spn.os.element.OpticalPath;
import spn.os.element.OpticalSwitch;
import spn.os.element.PhysicalLink;
import spn.os.element.PhysicalNetwork;
import spn.os.element.Port;
import spn.os.element.Vertex;

public class OpticalMapping {
    private Map<PhysicalLink, OpticalPath> map;
    private Set<OpticalSwitch> opticalSwitches;

    public OpticalMapping() {
        this.map = new HashMap<PhysicalLink, OpticalPath>();
        this.opticalSwitches = new HashSet<OpticalSwitch>();
    }

    public OpticalPath get(PhysicalLink link) {
        return this.map.get(link);
    }

    public void put(PhysicalLink link, OpticalPath path) {
        this.map.put(link, path);
        for (Link optLink : path.getLinks()) {
            if (optLink.getSrcVertex() instanceof OpticalSwitch)
                this.opticalSwitches.add((OpticalSwitch)optLink.getSrcVertex());
            if (optLink.getDstVertex() instanceof OpticalSwitch)
                this.opticalSwitches.add((OpticalSwitch)optLink.getDstVertex());
        }
    }

    public Map<PhysicalLink, OpticalPath> getMapping() {
        return this.map;
    }

    public Set<OpticalSwitch> getOpticalSwitches() {
        return this.opticalSwitches;
    }

    public Map<OpticalSwitch, Policy> getOpticalPolicies() {
        Map<OpticalSwitch,Policy> map = new HashMap<OpticalSwitch, Policy>();
        for (Map.Entry<PhysicalLink, OpticalPath> entry : this.map.entrySet()) {
            this.updatePolicies(entry.getValue(), map);
        }
        return map;
    }

    public static OpticalMapping fromJson(JsonArray json,
                                          PhysicalNetwork physical)
        throws InvalidMappingException {
        OpticalMapping mapping = new OpticalMapping();
        int linkId = 0;
        for (JsonElement element : json ) {
            try {
                JsonObject object = element.getAsJsonObject();
                OpticalPath path = OpticalPath.fromJson(object, physical);
                PhysicalLink link = new PhysicalLink(BigInteger.valueOf(linkId++),
                                             path.getPacketIn().getVertex(),
                                             path.getPacketIn().getPort(),
                                             path.getPacketOut().getVertex(),
                                             path.getPacketOut().getPort());
                mapping.put(link, path);

                if (path.isBidirectional()) {
                    OpticalPath reverse = path.getReverse();
                    PhysicalLink rev = new PhysicalLink(BigInteger.valueOf(linkId++),
                                                        reverse.getPacketIn().getVertex(),
                                                        reverse.getPacketIn().getPort(),
                                                        reverse.getPacketOut().getVertex(),
                                                        reverse.getPacketOut().getPort());
                    mapping.put(rev, reverse);
                }

            } catch (IllegalStateException e) {
                throw new InvalidMappingException(
                    "Mapping entries must be JSON objects: " + element.toString());
            } catch (InvalidConfigurationException e) {
                throw new InvalidMappingException(
                    "Could not parse JSON configuration: " + e.toString());
            } catch (InvalidHopException e) {
            	throw new InvalidMappingException(
                        "Invalide optical link: " + e.toString());
            }
        }
        return mapping;
    }

    private void updatePolicies(OpticalPath path, Map<OpticalSwitch, Policy> map) {
        Short wavelength = path.getWavelength();
        List<Link> links = path.getLinks();
        Link last = null;
        int size = links.size();
        for (int i=0; i<size; i++) {
            Link link = links.get(i);
            if (i == 0) {
                Vertex vertex = path.getStart().getVertex();
                Port ingress = path.getStart().getPort();
                Port egress = link.getSrcPort();
                updatePolicy(map, vertex, ingressPolicy(ingress, egress, wavelength));
                last = link;
                continue;
            }

            if (i == size-1) {
                Vertex vertex = path.getStop().getVertex();
                Port ingress = link.getDstPort();
                Port egress = path.getStop().getPort();
                updatePolicy(map, vertex, forwardPolicy(ingress, egress, wavelength));
            }

            Vertex vertex = link.getSrcVertex();
            Port ingress = last.getDstPort();
            Port egress = link.getSrcPort();
            updatePolicy(map, vertex, forwardPolicy(ingress, egress, wavelength));
            last = link;
        }

    }

    private Policy ingressPolicy(Port ingress, Port egress, Short wavelength) {
        return new Sequence(
            new Filter(new Test("port", ingress.toString())),
            new Modification("wavelength", Integer.toString(wavelength)),
            new Modification("port", egress.toString()));
    }

    private Policy forwardPolicy(Port ingress, Port egress, Short wavelength) {
        return new Sequence(
            new Filter(new And(new Test("port", ingress.toString()),
                               new Test("wavelength", Integer.toString(wavelength)))),
            new Modification("port", egress.toString()));
    }

    private void updatePolicy(Map<OpticalSwitch, Policy> map, Vertex vertex, Policy policy) {
        // TODO(basus) : Add methods for expanding unions
    	OpticalSwitch sw = (OpticalSwitch)vertex;
        Policy oldPolicy = map.get(sw);
        if (oldPolicy == null)
            map.put(sw, policy);
        else if (oldPolicy instanceof Union)
            ((Union)oldPolicy).add(policy);
        else
            map.put(sw, new Union(oldPolicy, policy));
    }

}
