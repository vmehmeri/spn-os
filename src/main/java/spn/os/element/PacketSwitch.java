package spn.os.element;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

import spn.exception.InvalidConfigurationException;

public class PacketSwitch extends Switch {

    public static PacketSwitch fromJson (JsonObject json)
        throws InvalidConfigurationException {
        PacketSwitch sw = new PacketSwitch();
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
            PacketPort port = PacketPort.fromJson(e.getAsJsonObject(), sw);
            ports.put(port.getNumber(), port);
        }

        sw.id = id;
        sw.name = name;
        sw.ports = ports;
        return sw;
    }

}