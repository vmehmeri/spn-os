package spn.os.element;

import com.google.gson.JsonObject;

import spn.os.Console;
import spn.os.element.MACAddress;
import spn.exception.InvalidConfigurationException;

public class PacketPort extends Port {
	
    public PacketPort() {
        super();
    }

    public PacketPort(int number, MACAddress mac, Vertex vertex) {
        super(number, mac, vertex);
    }

    public PacketPort(int number, MACAddress mac, Vertex vertex, boolean ingress, boolean egress) {
        super(number, mac, vertex, ingress, egress);
    }

    public static PacketPort fromJson(JsonObject json)
        throws InvalidConfigurationException
    {
        PacketPort port = new PacketPort();
        MACAddress addr;
        if(json == null)
            throw new InvalidConfigurationException(
                "No definition for this PhysicalPort. Json: " + json);

        if(!json.has("number"))
            throw new InvalidConfigurationException(
                "No port number for this PhysicalPort. Json: " + json);
        int number = json.get("number").getAsInt();

        if(!json.has("mac"))
            throw new InvalidConfigurationException(
                "No mac address for this PhysicalPort. Json: " + json);
        else
            addr = new MACAddress(json.get("mac").getAsString());

        if(!json.has("ingress")) {
            port.ingress = false;
        }
        else {
            port.ingress = json.get("ingress").getAsBoolean();
        }

        if(!json.has("egress"))
            port.egress = false;
        else
            port.egress = json.get("egress").getAsBoolean();
        
//        if (port.ingress)
//        	System.out.println( String.format("PacketPort #%d is ingress", number) );
//
//        if (port.egress)
//        	System.out.println( String.format("PacketPort #%d is egress", number) );
        
        port.number = number;
        port.mac = addr;
        
        
        
        return port;
    }

    public static PacketPort fromJson(JsonObject json, Vertex sw)
        throws InvalidConfigurationException {
        PacketPort port = fromJson(json);
        
        port.vertex = sw;
        return port;
    }

    public PacketPort copy(Vertex v) {
        PacketPort p = new PacketPort(this.number, this.mac, vertex,
                          this.ingress, this.egress);
        return p;
    }
}
