package spn.os.element;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import spn.os.element.MACAddress;
import spn.exception.InvalidConfigurationException;
import spn.exception.InvalidChannelNumberException;

public class OpticalPort extends Port {

    protected int numChannels;
    protected Map<Integer, Short> channels;

    public OpticalPort() {
        super();
    }

    public OpticalPort(int number, MACAddress mac, Vertex vertex) {
        super(number, mac, vertex);
        this.numChannels = 1;
        this.channels = new HashMap<Integer, Short>();
    }

    public OpticalPort(int number, MACAddress mac, Vertex vertex, int channels) {
        super(number, mac, vertex);
        this.numChannels = channels;
        this.channels = new HashMap<Integer, Short>(channels);
    }

    public boolean isValidChannel(int number) {
        return number < this.numChannels;
    }

    public int getNumChannels() {
        return this.numChannels;
    }

    public void setNumChannels(int number) {
        this.numChannels = number;
    }

    public Short getWavelength(int number) {
        if (isValidChannel(number))
            return this.channels.get(number);
        else
            throw new InvalidChannelNumberException(number);
    }

    public void setWavelength(int number, short lambda) {
        if (isValidChannel(number))
            this.channels.put(number, lambda);
        else
            throw new InvalidChannelNumberException(number);
    }

    public static OpticalPort fromJson(JsonObject json)
        throws InvalidConfigurationException
    {
        OpticalPort port = new OpticalPort();

        if (json == null)
            throw new InvalidConfigurationException(
                "No definition for this OpticalPort. Json: " + json);

        if (!json.has("number"))
            throw new InvalidConfigurationException(
                            "No port number for this OpticalPort. Json: " + json);
        else
        	port.number = json.get("number").getAsInt();

        if (!json.has("mac"))
            throw new InvalidConfigurationException(
                        "No mac address for this OpticalPort. Json: " + json);
        else
             port.mac = new MACAddress(json.get("mac").getAsString());

        if (!json.has("ingress"))
            port.ingress = false;
        else
        	port.ingress = json.get("ingress").getAsBoolean();

        if (!json.has("egress"))
            port.egress = false;
        else
            port.egress = json.get("egress").getAsBoolean();
        
        if (port.ingress)
        	System.out.println( String.format("OpticalPort #%d is ingress", port.number) );

        if (port.egress)
        	System.out.println( String.format("OpticalPort #%d is egress", port.number) );

        if (json.has("numChannels"))
            port.numChannels = json.get("numChannels").getAsInt();

        if (json.has("channels")) {
            JsonArray jsonChannels;
            try {
                jsonChannels = json.get("channels").getAsJsonArray();
            }
            catch (IllegalStateException e) {
                throw new InvalidConfigurationException(
                    "`channels` must be an array: " + e.toString());
            }
            port.numChannels = jsonChannels.size();
            for(JsonElement e : jsonChannels) {
                try {
                    JsonObject o = e.getAsJsonObject();
                    int num = o.get("number").getAsInt();
                    short lambda = o.get("wavelength").getAsShort();
                    port.setWavelength(num, lambda);
                } catch (IllegalStateException i) {
                    throw new InvalidConfigurationException(
                        "Invalid channel configuration: " + i.toString());
                }
            }
        }

        return port;

    }

    public static OpticalPort fromJson(JsonObject json, Vertex sw)
        throws InvalidConfigurationException {
        OpticalPort port = fromJson(json);
        port.vertex = sw;
        return port;
    }

}
