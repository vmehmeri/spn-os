package spn.os.element;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import spn.exception.InvalidConfigurationException;

public class OpticalSwitch extends Switch {

    public static OpticalSwitch fromJson (JsonObject json)
        throws InvalidConfigurationException {
        OpticalSwitch sw = new OpticalSwitch();
        if(!json.has("id"))
            throw new InvalidConfigurationException(
                "No datapath id for this switch. Json: " + json);

//        BigInteger id = new BigInteger(json.get("id").getAsString(), 16);
        BigInteger id = new BigInteger(json.get("id").getAsString());
        
        if(!json.has("name"))
            throw new InvalidConfigurationException(
                "No name for this switch. Json: " + json);
        String name = json.get("name").getAsString();

        Map<Integer, Port> ports = new HashMap<Integer, Port>();
        if(!json.has("ports"))
            throw new InvalidConfigurationException(
                "No ports for this vertex. Json: " + json);
        for(JsonElement e: json.get("ports").getAsJsonArray()) {
        	JsonObject o = e.getAsJsonObject();
        	if (o.has("type")) {
        		String portType = o.get("type").getAsString();
        		if (portType.equalsIgnoreCase("optical")) {
                    OpticalPort port = OpticalPort.fromJson(o, sw);
                    ports.put(port.getNumber(), port);
        		} else if (portType.equalsIgnoreCase("packet")) {
        			PacketPort port = PacketPort.fromJson(o, sw);
                    ports.put(port.getNumber(), port);
        		} else
        			throw new InvalidConfigurationException("Invalid port type : " 
        					+ portType);
        	} else
        		throw new InvalidConfigurationException("Unknown port type : "
        				+ o.toString());
        }

        sw.id = id;
        sw.name = name;
        sw.ports = ports;
        return sw;
    }


}
