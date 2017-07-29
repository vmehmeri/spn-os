package spn.os.element;

import com.google.gson.JsonObject;

import spn.os.element.MACAddress;
import spn.exception.InvalidConfigurationException;

public class VirtualPort extends Port {

    protected PacketPort physical;

    public VirtualPort() {
        super();
    }

    public VirtualPort(int number, MACAddress mac, Vertex vertex) {
        this.number = number;
        this.mac = mac;
        this.vertex = vertex;
        this.physical = null;
    }

    public VirtualPort(int number, MACAddress mac, Vertex vertex, PacketPort phys) {
        this.number = number;
        this.mac = mac;
        this.vertex = vertex;
        this.physical = phys;
    }

    public static VirtualPort fromJson(JsonObject json)
        throws InvalidConfigurationException
    {
        VirtualPort port = new VirtualPort();
        MACAddress addr;
        if(json == null)
            throw new InvalidConfigurationException(
                            "No definition for this VirtualPort. Json: " + json);

        if(!json.has("number"))
            throw new InvalidConfigurationException(
                            "No port number for this VirtualPort. Json: " + json);
        int number = json.get("number").getAsInt();

        if(!json.has("mac"))
            throw new InvalidConfigurationException(
                        "No mac address for this VirtualPort. Json: " + json);
        else
             addr = new MACAddress(json.get("mac").getAsString());

        if(!json.has("ingress"))
            port.ingress = false;
        else 
        	port.ingress = json.get("ingress").getAsBoolean();

        if(!json.has("egress"))
            port.egress = false;
        else
            port.egress = json.get("egress").getAsBoolean();

        port.number = number;
        port.mac = addr;

        return port;
   }

    public static VirtualPort fromJson(JsonObject json, Vertex sw)
        throws InvalidConfigurationException {
        VirtualPort port = fromJson(json);
        port.vertex = sw;
        return port;
    }

    public PacketPort getPhysical() {
        return this.physical;
    }

    public void setPhysical(PacketPort physical) {
        this.physical = physical;
    }


}
