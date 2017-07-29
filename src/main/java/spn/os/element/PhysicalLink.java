package spn.os.element;

import java.math.BigInteger;
import com.google.gson.JsonObject;

import spn.os.element.Network;
import spn.exception.InvalidConfigurationException;
import spn.exception.InvalidPortNumberException;

public class PhysicalLink extends Link {

    public PhysicalLink() {
        super();
    }

    public PhysicalLink(BigInteger id, Vertex src, Port srcPort, Vertex dst, Port dstPort) {
    	super(id, src, srcPort,dst, dstPort);
    }

    public static PhysicalLink fromJson(JsonObject json, Network network)
        throws InvalidConfigurationException {
        PhysicalLink link = new PhysicalLink();
        if(json == null || json.isJsonNull())
            throw new InvalidConfigurationException(
                "No definition for this PhysicalLink. Json: " + json);

        if(!json.has("id"))
            throw new InvalidConfigurationException(
                "No id for this link. Json: " + json);
        link.id = new BigInteger(json.get("id").getAsString());

        // load vertex info
        if(!json.has("src"))
            throw new InvalidConfigurationException("No source vertex. Json: " + json);
        if(!json.has("dst"))
            throw new InvalidConfigurationException("No destination vertex. Json: " + json);

        BigInteger srcJson = new BigInteger(json.get("src").getAsString());
        Vertex src = network.getVertex(srcJson);
        BigInteger dstJson = new BigInteger(json.get("dst").getAsString());
        Vertex dst = network.getVertex(dstJson);

        if (src == null) {
            throw new InvalidConfigurationException(
                "Unknown source vertex. Json: " + srcJson);
        } else {
            link.srcVertex = src;
        }

        if (dst == null) {
            throw new InvalidConfigurationException(
                "Unknown destination vertex. Json: " + dstJson);
        } else {
            link.dstVertex = dst;
        }

        // load ports info
        if(!json.has("srcPort"))
            throw new InvalidConfigurationException("No source port. Json: " + json);
        if(!json.has("dstPort"))
            throw new InvalidConfigurationException("No destination port. Json: " + json);

        int srcPortJson = json.get("srcPort").getAsInt();
        try {
            Port srcPort = src.getPort(srcPortJson);
            link.srcPort = srcPort;
        } catch (InvalidPortNumberException e) {
            throw new InvalidConfigurationException(
                "Unknown source port. Json: " + Integer.toString(srcPortJson)
                + " for vertex " + src.getName());
        }

        int dstPortJson = json.get("dstPort").getAsInt();
        try {
            Port dstPort = dst.getPort(dstPortJson);
            link.dstPort = dstPort;
        } catch (InvalidPortNumberException e) {
            throw new InvalidConfigurationException(
                "Unknown destination port. Json: " + Integer.toString(dstPortJson)
                + " for vertex " + dst.getName());
        }

        return link;

    }

}
