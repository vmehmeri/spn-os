package spn.os;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spn.exception.InvalidMappingException;
import spn.exception.SwitchConnectionException;
import spn.exception.VNOException;
import spn.exception.WebKATException;
import spn.netkat.Policy;
import spn.netkat.WebKAT;
import spn.os.controller.Controller;
import spn.os.element.ForwardingNode.Domain;
import spn.os.element.Host;
import spn.os.element.Link;
import spn.os.element.OpticalPath;
import spn.os.element.OpticalSwitch;
import spn.os.element.PhysicalLink;
import spn.os.element.PhysicalNetwork;
import spn.os.element.Switch;
import spn.os.element.VirtualNetwork;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;

public class VNO extends Observable {

	public enum State {
		REQUESTED, LOADED, STAGED, ACTIVE
	}

	private int id;
	private String name;
	private static Map<Integer, String> idNameMap;
	private Messenger arbiter;
	private State state;
	private int ownerId; // tenant-id of tenant which possess this VNO
	private WebKAT webKAT;
	private Controller controller;
	private String tenantHostListJson;
	private String networkAddressSpace;

	// Virtual packet layer
	private Policy virtualPolicy;
	private Policy ingressPolicy;
	private String ingressString;
	private VirtualNetwork virtualNetwork;
	private Mapping currentMapping;
	private Set<Mapping> mappings;
	private PhysicalNetwork aggregateNetwork;
	private PhysicalNetwork physical;
	private int aggregateLinkId;
	private boolean opticallyMapped;

	// Packet layer
	private PhysicalNetwork packetNetwork;
	private Set<OpticalMapping> opticalMappings;
	private OpticalMapping currentOpticalMapping;

	// Optical layer
	private PhysicalNetwork physicalNetwork;
	private String ipAddress;

	private Console console; // console = Console.getConsole(this);

	static {
		idNameMap = new HashMap<>();
	}

	public VNO(int id, String name, Controller controller, WebKAT compiler,
			Messenger arbiter) {
		// super();
		this.id = id;
		this.name = name;
		idNameMap.put(id, name);
		this.controller = controller;
		this.webKAT = compiler;
		this.arbiter = arbiter;
		this.state = State.REQUESTED;
		this.mappings = new HashSet<Mapping>();
		this.opticalMappings = new HashSet<OpticalMapping>();
		this.aggregateNetwork = new PhysicalNetwork();
		this.opticallyMapped = false;
		this.aggregateLinkId = 0;
		this.console = Console.getConsole(this);

	}

	// Constructor based on virtual layer
	public VNO(int id, int ownerId, String name, Controller controller,
               WebKAT compiler, Messenger arbiter, PhysicalNetwork physical,
               VirtualNetwork network, String tenantHostListJson,
               String networkAddressSpace) {
		this(id, name, controller, compiler, arbiter);
		this.physical = physical;
		this.virtualNetwork = network;
		this.ownerId = ownerId;
		this.tenantHostListJson = tenantHostListJson;
		this.networkAddressSpace = networkAddressSpace;
		// generateOdlSfcConfig(tenantHostListJson);
	}


	// Constructor based on optical layer
	public VNO(int id, int ownerId, String name, Controller controller,
               WebKAT compiler, Messenger arbiter, PhysicalNetwork network,
               OpticalMapping mapping, String tenantHostListJson,
               String networkAddressSpace) {
		this(id, name, controller, compiler, arbiter);
		this.packetNetwork = network;
		this.opticalMappings.add(mapping);
		this.currentOpticalMapping = mapping;
		this.ownerId = ownerId;
		this.tenantHostListJson = tenantHostListJson;
		this.networkAddressSpace = networkAddressSpace;
		// generateOdlSfcConfig(tenantHostListJson);
	}


	public WebKAT getWebKATInstance() {
		return this.webKAT;
	}


	public int getOwnerId() {
		return ownerId;
	}

	public String getName() {
		return name;
	}

	public void setOwnerId(int tenantId) {
		this.ownerId = tenantId;
	}

	public void removeDuplicates(boolean option) throws WebKATException {
		this.webKAT.removeDuplicates(option);
	}

	public VirtualNetwork getVirtualNetwork() {
		return this.virtualNetwork;
	}

	public PhysicalNetwork getPacketNetwork() {
		return this.packetNetwork;
	}

	public int getId() {
		return this.id;
	}

	public String getControllerIpPort() {
		return this.controller.getIpAddress() + ":"
				+ Integer.toString(this.controller.getPort());
	}

	public State getState() {
		return this.state;
	}

	public Mapping getMapping() {
		return this.currentMapping;
	}

	public OpticalMapping getOpticalMapping() {
		return this.currentOpticalMapping;
	}

	public Policy getVirtualPolicy() {
		return this.virtualPolicy;
	}

	public Policy getIngressPolicy() {
		return this.ingressPolicy;
	}

	public void setVirtualLayer(Policy policy, Policy ingressPolicy,
			Mapping mapping) throws VNOException {
		this.virtualPolicy = policy;
		this.ingressPolicy = ingressPolicy;
		this.addMapping(mapping);
		this.currentMapping = mapping;
		this.state = State.LOADED;
	}

	public void setVirtualLayer(Policy policy, String ingressString,
			Mapping mapping) throws VNOException {
		this.virtualPolicy = policy;
		this.ingressString = ingressString;
		this.addMapping(mapping);
		this.currentMapping = mapping;
		this.state = State.LOADED;
	}

	public void setPacketLayer(PhysicalNetwork network, OpticalMapping mapping)
			throws InvalidMappingException, VNOException {
		if (network == null)
			throw new VNOException("Physical Network object is null");
		this.packetNetwork = network;
		if (mapping == null)
			throw new InvalidMappingException("Mapping object is null");
		this.addOpticalMapping(mapping);
		this.currentOpticalMapping = mapping;
	}

	public void setState(State state) {
		this.state = state;
		setChanged();
		notifyObservers(state);
	}

	public void setVirtualPolicy(Policy policy) {
		this.virtualPolicy = policy;
	}

	public void setIngressPolicy(Policy policy) {
		this.ingressPolicy = policy;
	}

	public void setIngressString(String ingress) {
		this.ingressString = ingress;
		this.ingressPolicy = null;
	}

	public String getIngressString() {
		return this.ingressString;
	}

	public boolean addMapping(Mapping mapping) {
		return mappings.add(mapping);
	}

	public void removeAllMapping() {
		mappings.clear();
	}

	public void removeMapping(Mapping mapping) {
		if (mappings.contains(mapping))
			mappings.remove(mapping);
	}

	public void mapTo(Mapping m) throws VNOException {
		this.mappings.add(m);
		this.currentMapping = m;

		// if (this.state == State.ACTIVE)
		try {
			activatePacket();
		} catch (SwitchConnectionException e) {
			//TODO: Handle appropriately
			console.error("Unable to remap: lost connection with one or more switches");

		}
	}

	public void loadMap(Mapping m) throws VNOException {
		this.mappings.add(m);
		this.currentMapping = m;

	}

	public boolean addOpticalMapping(OpticalMapping opticalMapping) {
		return this.opticalMappings.add(opticalMapping);
	}

	public void removeOpticalMapping(OpticalMapping opticalMapping) {
		if (opticalMappings.contains(opticalMapping))
			opticalMappings.remove(opticalMapping);
	}

	public void removeAllOpticalMapping() {
		opticalMappings.clear();
	}

	public void opticalMapTo(OpticalMapping m) throws VNOException {
		for (Switch sw : this.currentOpticalMapping.getOpticalSwitches())
			sw.removePolicies(this);
		this.opticalMappings.add(m);
		this.currentOpticalMapping = m;
		try {
			this.setPacketLayer(this.physical, m);
		} catch (InvalidMappingException e) {
			throw new VNOException("Invalid mapping");
		}
		// if (this.state == State.ACTIVE)
		try {
			activateOptical();
		} catch (SwitchConnectionException e) {
			//TODO: Handle appropriately
			e.printStackTrace();
			console.error("Unable to remap optical: lost connection with one or more switches");
		}
	}

	public void loadOpticalMap(OpticalMapping m) throws VNOException {
		for (Switch sw : this.currentOpticalMapping.getOpticalSwitches())
			sw.removePolicies(this);
		this.opticalMappings.add(m);
		this.currentOpticalMapping = m;
		try {
			this.setPacketLayer(this.physical, m);
		} catch (InvalidMappingException e) {
			throw new VNOException("Invalid mapping");
		}
	}


	public void deactivate(Iterable<? extends Switch> switches)
			throws VNOException {
		for (Switch sw : switches)
			sw.removePolicies(this);
		recompile(switches);
		try {
			refresh(switches);
			this.state = State.LOADED;
		} catch (SwitchConnectionException e) {
			e.printStackTrace();
		}
	}

	// public void deactivateOptical() throws VNOException {
	// this.arbiter.deactivateOptical(this);
	// this.state = State.LOADED;
	// }

	public void activate() throws VNOException, SwitchConnectionException {
		if (webKAT == null)
			throw new VNOException("WebKAT is null.");

		if (!(getOpticalMapping() == null)) {
			try {
				activateOptical();
			} catch (SwitchConnectionException e) {
				//TODO handle appropriately
				e.printStackTrace();
			}
		}
		else
			console.error("Optical mapping is null");

		if (!(getMapping() == null)) {
			try {
				activatePacket();
			} catch (SwitchConnectionException e) {
				//TODO handle appropriately
				console.error("Unable to activate: lost connection with one or more switches");
				throw new SwitchConnectionException(e.getMessage());
			}
		}
		else
			console.error("Virtual mapping is null");

	}

	public void activateOptical() throws VNOException, SwitchConnectionException {
		OpticalMapping mapping = getOpticalMapping();
		Map<OpticalSwitch, Policy> policyMap = mapping.getOpticalPolicies();

		console.info("Activating optical layer");
		List<OpticalSwitch> switches = new ArrayList<OpticalSwitch>(
				policyMap.size());
		for (Map.Entry<OpticalSwitch, Policy> entry : policyMap.entrySet()) {
			Policy po = entry.getValue();
//			console.debug(String.format(
//					"Adding policy to optical switch %d", entry.getKey()
//							.getId().intValue()));
			entry.getKey().addPolicy(this, entry.getValue());
			switches.add(entry.getKey());
		}

		// Add links to aggregate packet topology
		for (Map.Entry<PhysicalLink, OpticalPath> entry : mapping.getMapping()
				.entrySet()) {
			OpticalPath path = entry.getValue();
			Link link = new PhysicalLink(BigInteger.valueOf(aggregateLinkId++),
					path.getPacketIn().getVertex(), path.getPacketIn()
							.getPort(), path.getPacketOut().getVertex(), path
							.getPacketOut().getPort());
			// console.debug(">>>>>>>>>>>>>> Adding Link to aggregate network");
			// console.debug(link.prettyPrint());
			this.aggregateNetwork.addLink(link);
		}


		this.opticallyMapped = true;
		controller.clearFlowtable(switches);

		recompile(switches);
		controller.refresh(switches);
	}

	public void activatePacket() throws VNOException, SwitchConnectionException {
		switch (getState()) {
		case LOADED:
		case ACTIVE:
		case STAGED:
		default:
			try {
				webKAT.stageVirtual(this);
			} catch (WebKATException e) {
				throw new VNOException("Unable to stage VNO : " + e.toString());
			}
			setState(VNO.State.STAGED);
			break;
		case REQUESTED:
			throw new VNOException("Unloaded VNO " + this.id);
			// default:
			// throw new VNOException("Unknown state for VNO " + this.id);

		}

		Iterable<? extends Switch> switches = recompile();
		try {
			controller.refresh(switches);
			setState(VNO.State.ACTIVE);
		} catch (SwitchConnectionException e) {
			console.info("Lost connection with switch. Retrying...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			controller.refresh(switches);

		}

	}

	public void deactivate() throws VNOException {
		if (!(getMapping() == null)) {
			try {
				this.deactivatePacket();
			} catch (SwitchConnectionException e) {
				//TODO handle appropriately
				e.printStackTrace();
			}
		}

		if (!(getOpticalMapping() == null)) {
			try {
				this.deactivateOptical();
			} catch (SwitchConnectionException e) {
				//TODO handle appropriately
				e.printStackTrace();
			}
		}
	}

	public void deactivateOptical() throws VNOException, SwitchConnectionException {
		OpticalMapping mapping = getOpticalMapping();
		for (OpticalSwitch sw : mapping.getOpticalSwitches())
			sw.removePolicies(this);

		recompile(mapping.getOpticalSwitches());
		controller.refresh(mapping.getOpticalSwitches());
	}

	public void deactivatePacket() throws VNOException, SwitchConnectionException {
		if (this.webKAT == null)
			throw new VNOException("WebKAT is null");

		if (getState() != VNO.State.ACTIVE)
			// This VNO was not active, so this deactivate is a noop
			return;

		try {
			webKAT.remove(this.id);
		} catch (WebKATException e) {
			throw new VNOException("Unable to deactivate VNO : " + e.toString());
		}
		Iterable<? extends Switch> switches = recompile();
		controller.refresh(switches);
	}

	private JsonArray removeEmptyPatterns(JsonArray flowTable) {
		JsonArray modifiedFlowTable = new JsonArray();

		for (JsonElement el : flowTable) {
			JsonObject flow = el.getAsJsonObject();
			JsonObject pattern = flow.get("pattern").getAsJsonObject();
			if (!pattern.get("inPort").isJsonNull() || !pattern.get("dlVlan").isJsonNull() ||
					!pattern.get("dlTyp").isJsonNull() || !pattern.get("nwSrc").isJsonNull() ||
					!pattern.get("nwDst").isJsonNull()
					) {
				modifiedFlowTable.add(el);
			} else {
				//Ignore
				continue;
			}


		}

		return modifiedFlowTable;
	}

	public void recompile(Iterable<? extends Switch> switches)
			throws VNOException {
//		console.debug("Recompiling switches...");
		for (Switch sw : switches) {
			try {
				console.info("--> Sending to NetKAT for compilation...");
				JsonArray flowTable = webKAT.compile(sw.getId().intValue(), sw
						.getPolicy().toString());

				JsonArray modifiedFlowTable = removeEmptyPatterns(flowTable);

				
				sw.setFlowTable(modifiedFlowTable);
				console.info("<-- Got flowtable for switch " + sw.getId()
						+ " : ");
				try {
					console.jsonToFile(sw.getFlowTable().toString(),
							"/root/flow_table_" + sw.getId().toString());
				} catch (Exception e) {
					console.error("Failed to write flowtable to file.",
							e.toString());
				}
			} catch (WebKATException e) {
				throw new VNOException(
						"Unable to compile flowtable for switch " + sw.getId()
								+ " : " + e.toString());
			}
		}
	}

	public void refresh(Iterable<? extends Switch> switches) throws SwitchConnectionException {
		controller.refresh(switches);
	}

	private Iterable<? extends Switch> recompile() throws VNOException {
		PhysicalNetwork network;
		if (this.opticallyMapped) {
//			console.debug("Adding ingresses and egresses to aggregate physical network");
			this.aggregateNetwork.addIngresses(this.physical);
			this.aggregateNetwork.addEgresses(this.physical);

			network = this.aggregateNetwork;

		} else {
			network = this.physical;
		}

		try {
//			console.debug("Staging aggregate physical network");
			webKAT.stagePhysical(network);
		} catch (WebKATException e) {
			throw new VNOException("Unable to stage packet network :"
					+ e.toString());
		}

		try {
//			console.debug("Sending to NetKAT for compilation...");
			webKAT.compile();
		} catch (WebKATException e) {
			setState(State.LOADED);
			throw new VNOException("Unable to compile VNO : " + e.toString());
		}

		//TODO: If multiple VNOs are supported, this should only clear flowtables for this VNO only!
		controller.clearFlowtable(network.getSwitches());
		for (Switch sw : network.getSwitches()) {
			try {
				JsonArray flowTable = webKAT.getFlowTable(sw.getId()
						.longValue());
				JsonArray modifiedFlowTable = removeEmptyPatterns(flowTable);
				sw.setFlowTable(modifiedFlowTable);
				console.info("<-- Got flowtable for switch " + sw.getId());
				try {
					console.jsonToFile(sw.getFlowTable().toString(),
							"/root/recompile_all_flow_table_"
									+ sw.getId().toString());
				} catch (Exception e) {
					console.error("Failed to write flowtable to file.",
							e.toString());
				}

			} catch (WebKATException e) {
				throw new VNOException("Unable to get flowtable for switch "
						+ sw.getId() + " : " + e.toString());
			}
		}

		return network.getSwitches();
	}



	public String getNetworkAddressSpace() {
		return networkAddressSpace;
	}

	public void setNetworkAddressSpace(String networkAddressSpace) {
		this.networkAddressSpace = networkAddressSpace;
	}

	private Set<Host> getHostSetFromJson(String jsonStr) {
		if (jsonStr == null)
			console.debug("jsonStr null!");
		Set<Host> hostSet = new HashSet<Host>();
		// console.debug(jsonStr);

		JsonParser parser = new JsonParser();

		try {
			JsonArray jsonArr = (JsonArray) parser.parse(jsonStr);
			// JsonArray jsonArr = jsonObj.getAsJsonArray();

			for (JsonElement el : jsonArr) {
				String mgmtIpAddr = el.getAsJsonObject().get("mgmt-ip-address")
						.getAsString();
				String dpIpAddr = el.getAsJsonObject().get("dp-ip-address")
						.getAsString();
				String domain = el.getAsJsonObject().get("domain")
						.getAsString();

//				console.debug("mgmt-ip-address", mgmtIpAddr);
//				console.debug("dp-ip-address", dpIpAddr);
//				console.debug("domain", domain);

				Host h = new Host();
				h.setMgmtIpAddress(mgmtIpAddr);
				h.setDpIpAddress(dpIpAddr);
				h.setDomain(Domain.fromString(domain));
				hostSet.add(h);

			}
		} catch (Exception e) {
			console.error("Failed to get Host information", e.getMessage());
		}

		return hostSet;
	}

	public static String getVnoNameById(int vnoId) {
		return idNameMap.get(vnoId);
	}

	public static Integer getVnoIdByName(String vnoName) {
		for (Entry<Integer, String> idName : idNameMap.entrySet()) {
			if (idName.getValue().equals(vnoName))
				return idName.getKey();
		}
		return null;
	}

}
