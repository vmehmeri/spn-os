package spn.os.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import spn.exception.InvalidConfigurationException;

public class PhysicalNetwork extends Network {

	JsonObject json;
	
	public void setJsonObj(JsonObject json) {
		this.json = json;
	}
	
	public String toJsonStr() {
		if (json == null) return "";
		return this.json.toString();
	}
	
    public static PhysicalNetwork fromJson(JsonObject json)
        throws InvalidConfigurationException {
        PhysicalNetwork network = new PhysicalNetwork();
        network.setJsonObj(json);
        JsonArray switchesJson = json.get("switches").getAsJsonArray();
        for (JsonElement e : switchesJson) {
            JsonObject o = e.getAsJsonObject();
            if (o.has("type")) {
                String switchType = o.get("type").getAsString();
                if (switchType.equalsIgnoreCase("packet"))
                	network.addSwitch(PacketSwitch.fromJson(o));
                else if (switchType.equalsIgnoreCase("optical"))
                	network.addSwitch(OpticalSwitch.fromJson(o));
                else
                	throw new InvalidConfigurationException("Unknown switch type : "
                			+ switchType);
            } else 
            	throw new InvalidConfigurationException("Unknown switch type : " 
            			+ e.toString());
        }

        JsonArray hostsJson = json.get("hosts").getAsJsonArray();
        for (JsonElement e : hostsJson)
            network.addHost(PhysicalHost.fromJson(e.getAsJsonObject()));

        JsonArray linksJson = json.get("links").getAsJsonArray();
        for (JsonElement e : linksJson)
            network.addLink(PhysicalLink.fromJson(e.getAsJsonObject(), network));

        return network;
    }
}
