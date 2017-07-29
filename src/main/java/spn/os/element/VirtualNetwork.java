package spn.os.element;

import java.util.HashMap;
import java.util.Set;
import java.math.BigInteger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import spn.exception.InvalidConfigurationException;

public class VirtualNetwork extends Network {

	protected int id;

	public VirtualNetwork() {
		this.id = 0;
		this.hosts = new HashMap<BigInteger, Host>();
		this.switches = new HashMap<BigInteger, Switch>();
		this.links = new HashMap<BigInteger, Link>();
	}

	public VirtualNetwork(int id, Set<VirtualHost> hosts,
			Set<VirtualSwitch> switches, Set<VirtualLink> links) {
		this.id = id;
		this.hosts = new HashMap<BigInteger, Host>();
		for (VirtualHost host : hosts) {
			this.addHost(host);
		}
		;
		this.switches = new HashMap<BigInteger, Switch>();
		for (VirtualSwitch sw : switches) {
			this.addSwitch(sw);
		}
		this.links = new HashMap<BigInteger, Link>();
		for (VirtualLink l : links) {
			this.addLink(l);
		}
	}

	public static VirtualNetwork fromJson(JsonObject json)
			throws InvalidConfigurationException {
		VirtualNetwork network = new VirtualNetwork();
		network.setJsonStr(json.toString());
		JsonArray switchesJson = json.get("switches").getAsJsonArray();
		for (JsonElement e : switchesJson)
			network.addSwitch(VirtualSwitch.fromJson(e.getAsJsonObject()));

		JsonArray hostsJson = json.get("hosts").getAsJsonArray();
		for (JsonElement e : hostsJson)
			network.addHost(VirtualHost.fromJson(e.getAsJsonObject()));

		JsonArray linksJson = json.get("links").getAsJsonArray();
		for (JsonElement e : linksJson)
			network.addLink(VirtualLink.fromJson(e.getAsJsonObject(), network));

		return network;
	}

	public static VirtualNetwork fromJsonStr(String jsonStr)
			throws InvalidConfigurationException {
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(jsonStr);

		VirtualNetwork network = VirtualNetwork.fromJson(json);
		network.setJsonStr(jsonStr);

		return network;
	}

	public String toJsonString() {
		return jsonStr;
	}

}
