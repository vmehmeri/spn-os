package spn.os;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import spn.exception.InvalidConfigurationException;
import spn.exception.InvalidMappingException;
import spn.exception.VNOException;
import spn.exception.VNOMaxNumberReachedException;
import spn.exception.WebKATException;
import spn.os.ConfigLoader.TenantConfig;
import spn.os.ConfigLoader.TenantConfig.HostConfig;
import spn.os.ConfigLoader.TenantConfig.OpticalMappingConfig;
import spn.os.ConfigLoader.TenantConfig.VirtualMappingConfig;
import spn.os.element.ForwardingNode;
import spn.os.element.Host;
import spn.os.element.PhysicalNetwork;
import spn.os.element.VirtualNetwork;
import spn.os.jms.Server;

public class Arbiter implements MessageListener {

	private static boolean[] vnoIdBoolArray = new boolean[5000];
	private Map<Integer, VNO.State> vnos; // <vno-id,vno-state>
	private Map<Integer, List<Integer>> allocations; // <tenant-id,list of
														// vno-ids>
	private Map<Pair<Integer,String>,Integer> tenantVnoNameIdMap;
	private PhysicalNetwork physical;
	// private boolean opticallyMapped;
	private Server jmsServer;
	private String ipAddress;
	private HashMap<String, String> virtualMappingDict;
	private HashMap<String, String> opticalMappingDict;
	private String physicalNetworkJsonStr;
	private Set<String> controllerAddressList;
	private Map<Integer, Set<String>> tenantOvsdbManagerAddresses;
	// private Map<Integer, Set<String>> tenantHostMgmtIpAddresses;
	// private Map<Integer, Set<String>> tenantHostDpIpAddresses;
	private Map<Integer, Set<Host>> tenantHosts;
	private Console console = Console.getConsole(this);
	private Map<String, String> hostOvsdbAddresses;

	public Arbiter() throws IOException {
		this.vnos = new HashMap<Integer, VNO.State>();
		this.allocations = new HashMap<Integer, List<Integer>>();
		this.tenantVnoNameIdMap = new HashMap<>();
		this.controllerAddressList = new HashSet<String>();
		this.tenantOvsdbManagerAddresses = new HashMap<Integer, Set<String>>();
		this.ipAddress = ConfigLoader.getLoader().getGlobalConfig()
				.getArbiterIpAddress();
//		this.controllerAddressList.add(String.format("tcp:192.168.137.111:6633"));
//		this.controllerAddressList.add(String.format("tcp:192.168.137.160:6633"));
		this.hostOvsdbAddresses = new HashMap<>();

		initJMS();
		loadConfiguration();
	}

	public Arbiter(PhysicalNetwork physical) throws IOException {
		this();
		this.physical = physical;
		this.physicalNetworkJsonStr = physical.toJsonStr();

	}

	private int getNextAvailableVnoId() throws VNOMaxNumberReachedException {
		for (int id = 1; id < vnoIdBoolArray.length; id++) {
			boolean alreadyUsed = vnoIdBoolArray[id];

			if (!alreadyUsed) {
				vnoIdBoolArray[id] = true;
				return id;
			}
		}

		throw new VNOMaxNumberReachedException(
				"No IDs available for a new VNO. Max number of VNOs supported has been reached");
	}

	/**
	 * Initialize JMS Server and register itself as listener
	 */
	public void initJMS() {
		this.jmsServer = new Server();
		try {
			this.jmsServer.registerMessageListener(this);
		} catch (JMSException e) {
			System.err.println("Error starting JMS server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This will initialize maps to contain the json content in string format
	 * for each mapping, given the name of the json file (without * .json
	 * extension). Also initializes list of hosts IP addresses for each tenant.
	 */
	public void loadConfiguration() {
		virtualMappingDict = new HashMap<String, String>();
		opticalMappingDict = new HashMap<String, String>();
		tenantHosts = new HashMap<Integer, Set<Host>>();

		ConfigLoader cfgLoader = ConfigLoader.getLoader();
		try {
			cfgLoader.loadConfiguration();
		} catch (ParserConfigurationException | SAXException e) {
			console.error("Failed to parse XML Configuration file",
					e.getMessage());
		} catch (IOException e) {
			console.error("Failed to open XML Configuration file",
					e.getMessage());
		}

		List<TenantConfig> mpCfgList = cfgLoader
				.getMappingTenantConfig();

		for (TenantConfig tnt : mpCfgList) {
			int tenantId = tnt.getId();
			Set<Host> hosts = new HashSet<Host>();

			for (HostConfig hCfg : tnt.getHostsConfig()) {
				Host h = new Host();
				h.setMgmtIpAddress(hCfg.getMgmtIpAddress());
				h.setDpIpAddress(hCfg.getDpIpAddress());

				h.setDomain(ForwardingNode.Domain.fromString(hCfg
						.getDomainName()));
				hosts.add(h);
			}

			tenantHosts.put(tenantId, hosts);

			for (OpticalMappingConfig omCfg : tnt.getOpticalMappings()) {
				int omId = omCfg.getId();
				String omFileLoc = omCfg.getFileLocation();
				String virtMappingName = String.format(
						"tenant%d/optical_mapping%d", tenantId, omId);
				addMappingFromFile(opticalMappingDict, virtMappingName,
						omFileLoc);
			}

			for (VirtualMappingConfig vmCfg : tnt.getVirtualMappings()) {
				int vmId = vmCfg.getId();
				String vmFileLoc = vmCfg.getFileLocation();
				String virtMappingName = String.format(
						"tenant%d/virtual_mapping%d", tenantId, vmId);
				addMappingFromFile(virtualMappingDict, virtMappingName,
						vmFileLoc);
			}
		}

	}

	public Set<String> getTenantHostAddresses(int tenantId) {
		Set<String> addresses = new HashSet<String>();
		for (Host h : tenantHosts.get(tenantId)) {
			addresses.add(h.getMgmtIpAddress());
		}
		return addresses;
	}

	/**
	 * Add item to map <mapping_name,json_string> This is a convenience method
	 * to retrieve the json string content of a file, given the root name of
	 * that file (i.e, the name of the file without the .json extension)
	 *
	 */
	private void addMappingFromFile(HashMap<String, String> map,
			String mappingName, String fileLocation) {
		try {
			map.put(mappingName, Util.jsonStringFromFilename(fileLocation));
		} catch (InvalidConfigurationException e) {
			console.error("Could not load mapping configuration",
					e.getMessage());
		}
	}

	/**
	 * Callback method for LOAD command from a tenant requesting a VNO. This
	 * will validate the VNO, generate a new ID for it, and return this ID.
	 * Following this method necessary information about the network must be
	 * sent back to the tenant.
	 * 
	 * @param tenantId
	 *            the requesting tenant's ID
	 * @param virtualNetworkJsonStr
	 *            JSON String of virtual network being requested
	 * @return
	 * @throws InvalidConfigurationException
	 * @throws VNOException
	 * @throws VNOMaxNumberReachedException 
	 */
	public int loadNewVNO(int tenantId, String vnoName,
			String virtualNetworkJsonStr) throws InvalidConfigurationException,
			VNOException, VNOMaxNumberReachedException {
		
		int vnoId = getNextAvailableVnoId();
		VirtualNetwork virt = VirtualNetwork.fromJson(Util
				.jsonFromString(virtualNetworkJsonStr));

		// TODO
		// Now the arbiter has the physical network object, the mappings,
		// and virt (the requested virtual network object). Verifications
		// and validations should be done at this point. If virtual network
		// can't be allocated for some reason, the method below should throw
		// VNOException, which should be handled appropriately.
		verify(virt, null); // TODO Actually pass the mapping instead of null
		
		this.addVNO(vnoId, tenantId);
		this.tenantVnoNameIdMap.put(new Pair<Integer,String>(tenantId, vnoName), vnoId);
		return vnoId;
	}

	public void reloadVNO(int vnoId, int tenantId, String vnoName,
			String virtualNetworkJsonStr) throws InvalidConfigurationException,
			VNOException {
		// currentVNOId++;
		VirtualNetwork virt = VirtualNetwork.fromJson(Util
				.jsonFromString(virtualNetworkJsonStr));

		// TODO
		// Now the arbiter has the physical network object, the mappings,
		// and virt (the requested virtual network object). Verifications
		// and validations should be done at this point. If virtual network
		// can't be allocated for some reason, the method below should throw
		// VNOException, which should be handled appropriately.
		verify(virt, null); // TODO Actually pass the mapping instead of null

		this.addVNO(vnoId, tenantId);
	}

	/**
	 * Add VNO to list of known VNOs, and allocate it to the corresponding
	 * tenant. 
	 * 
	 * @param vnoId
	 * @param tenantId
	 */
	private void addVNO(int vnoId, int tenantId) {
		vnos.put(vnoId, VNO.State.LOADED);

		// Add VNO to tenant's VNO list
		if (allocations.containsKey(tenantId)) {
			List<Integer> vnos = allocations.get(tenantId);
			if (!vnos.contains(vnoId))
				vnos.add(vnoId);
		} else {
			LinkedList<Integer> vnos = new LinkedList<Integer>();
			vnos.add(vnoId);
			allocations.put(tenantId, vnos);
		}
	}
	
	private void deleteVNO(int vnoId, int tenantId) {
		if (vnos.containsKey(vnoId)) {
			console.info(String.format("Removing VNO [%d]",
					vnoId));
			vnos.remove(vnoId);
		}

		Integer vnoIdObj = new Integer(vnoId);

		if (allocations.get(tenantId) != null
				&& allocations.get(tenantId).contains(vnoIdObj)) {
			console.info(String.format(
					"Removing VNO [%d] from Tenant [%d] allocations",
					vnoId, tenantId));
			allocations.get(tenantId).remove(vnoIdObj);
		}
		
		vnoIdBoolArray[vnoId] = false;
		
		for (Pair<Integer,String> tenantIdVnoName : tenantVnoNameIdMap.keySet()) {
			if (tenantVnoNameIdMap.get(tenantIdVnoName) == vnoId) {
				tenantVnoNameIdMap.remove(tenantIdVnoName);
				break;
			}
		}
	}

	public void verify(VirtualNetwork virt, Mapping mapping)
			throws VNOException {
		// TODO: Verification algorithms should go here
		// throw new VNOException("Comprehensive message here");
	}

	public boolean verify(VNO vno, Mapping mapping) throws VNOException {
		// TODO(basus) actual verify the VNO somehow
		return vno.addMapping(mapping);
	}

	/**
	 * JMS Message callback
	 */
	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage txtMsg = (TextMessage) message;

				// System.out.println("Received: \n" + messageText);
				TextMessage response = handleIncomingMessage(txtMsg);
				jmsServer.sendResponse(message, response);
			}

		} catch (JMSException e) {
			console.error(
					"An error occured while processing a received message",
					e.getMessage());
		}
	}
	
	private void checkIfVNOExists(int tenantId, String vnoName) throws VNOException {
		Pair<Integer,String> tenantIdVnoNamePair = new Pair<>(tenantId, vnoName);
			
		if (tenantVnoNameIdMap.containsKey(tenantIdVnoNamePair)) {
			int existingVnoId = tenantVnoNameIdMap.get(tenantIdVnoNamePair);
			
			throw new VNOException (String.format(
					"VNO named '%s' from tenant #%d already exists (VNO Id #%d)."
					+ "\nUse reload command instead", 
					tenantIdVnoNamePair.second, tenantIdVnoNamePair.first, existingVnoId)
					);
		}
	}

	private List<String> getTenantVirtualMappings(int tenantId) {
		String tenantIdPrefix = String.format("tenant%d/", tenantId);
		List<String> virtualMappings = new ArrayList<>();

		for (String vMapping : virtualMappingDict.values())
		{
			virtualMappings.add(vMapping);
		}

		return virtualMappings;
	}

	private List<String> getTenantOpticalMappings(int tenantId) {
		String tenantIdPrefix = String.format("tenant%d/", tenantId);
		List<String> opticalMappings = new ArrayList<>();

		for (String oMapping : opticalMappingDict.values())
		{
			opticalMappings.add(oMapping);
		}

		return opticalMappings;
	}

	private TextMessage addMappingInfoToMessage(TextMessage txtMsg, int tenantId) throws JMSException {
		String tenantIdPrefix = String.format("tenant%d/", tenantId);
		// TODO: Send a list instead of a separate parameter for each
		// TODO: Right now hard-coding mapping1 and mapping2. Mappings should be
		// gotten through verification & some actual mapping generation.
		//TODO: Send a list, instead of individual parameters

		List<String> tenantVirtualMappings = getTenantVirtualMappings(tenantId);
		List<String> tenantOpticalMappings = getTenantOpticalMappings(tenantId);

		txtMsg.setObjectProperty("virtual_mappings", tenantVirtualMappings);
		txtMsg.setObjectProperty("optical_mappings", tenantOpticalMappings);

		return txtMsg;
	}
	
	private TextMessage addHostInfoToMessage(TextMessage txtMsg, int tenantId) throws JMSException {
		//Utility object to jsonify Objects
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
				.create();
		
		Set<Host> tenantHostSet = tenantHosts.get(tenantId);
		List<Host> tenantHostList = new ArrayList<>(tenantHostSet);
		String tenantHostListJson = gson.toJson(tenantHostList);
		console.debug(tenantHostListJson);
		txtMsg.setStringProperty("host-list", tenantHostListJson);
		
		console.debug("Assigning Hosts:");
		for (Host host : this.tenantHosts.get(tenantId)) {
			console.debug(String.format("<%s>", host.getDpIpAddress()));
		}
		
		return txtMsg;
	}
	
	private TextMessage buildLoadReplyMessage(TextMessage txtMsg, int tenantId, int vnoId, String vnoName) throws JMSException {
		txtMsg.setIntProperty("vno-id", vnoId);
		txtMsg.setIntProperty("tenant-id", tenantId);
		txtMsg.setStringProperty("vno-name", vnoName);

		txtMsg.setStringProperty("physical-network",
				physicalNetworkJsonStr);
		txtMsg = addMappingInfoToMessage(txtMsg, tenantId);
		txtMsg = addHostInfoToMessage(txtMsg, tenantId);
		//Add the network address space belonging to the tenant
		for (TenantConfig tntCfg : ConfigLoader.getLoader()
				.getMappingTenantConfig()) {
			if (tntCfg.getId() == tenantId) {
				txtMsg.setStringProperty("network-address-space", tntCfg.getNetworkAddressSpace());
			}
		}
		return txtMsg;
	}
	
	/**
	 * Handler for incoming VNOs' requests.
	 * 
	 * @param txtMsg
	 *            - the incoming message
	 * @return reply message
	 * @throws JMSException
	 */
	public TextMessage handleIncomingMessage(TextMessage txtMsg)
			throws JMSException {
		console.info("<< Message Received ");

		String cmd;
		int tenantId;
		int vnoId;
		String msgContent;
		TextMessage response = this.jmsServer.createTextMessage();

		try {
			cmd = txtMsg.getStringProperty("command");
			msgContent = txtMsg.getText();

		} catch (JMSException e) {
			e.printStackTrace();
			response.setStringProperty("status-reply", "ERROR");
			return response;
		}

		if (cmd == null) {
			response.setStringProperty("status-reply", "ERROR");
			return response;
		}

		//Set the same command on the response body,
		//so that tenant knows what the reply is for
		response.setStringProperty("command", cmd);

		
		
		switch (cmd) {
		case "LOAD":
			try {
				// msgContent is the virtual network in JSON String format
				Integer newVnoId;
				String vnoName;
				String virtualNetworkJsonStr = null;

				tenantId = txtMsg.getIntProperty("tenant-id");
				vnoName = txtMsg.getStringProperty("vno-name");
	
				if (msgContent == null)
					console.error("Message content is null (Virtual Network JSON structure expected)");
				else
					virtualNetworkJsonStr = msgContent;

				try {
					checkIfVNOExists(tenantId, vnoName); //will throw exception if it does
					newVnoId = loadNewVNO(tenantId, vnoName,
							virtualNetworkJsonStr);
				} catch (VNOException | VNOMaxNumberReachedException e) {
					console.error("Could not create requested VNO",
							e.getMessage());
					
					response.setStringProperty("status-reply", "FAIL");
					response.setText(e.getMessage()); // send the error cause back
					return response;
				}

				buildLoadReplyMessage(response, tenantId, newVnoId, vnoName);
				
				response.setStringProperty("status-reply", "SUCCESS");
				console.info("VNO Request accepted");
				
				

				return response;
			} catch (InvalidConfigurationException ice) {
				ice.printStackTrace();
				response.setStringProperty("status-reply", "ERROR");
				response.setText(ice.getMessage());
				return response;
			}
			
		case "RELOAD":
			try {
				// msgContent is the virtual network in JSON String format
				int existingVnoId;
				String vnoName;
				String virtualNetworkJsonStr = null;

				tenantId = txtMsg.getIntProperty("tenant-id");
				vnoName = txtMsg.getStringProperty("vno-name");
				
				if (msgContent == null)
					console.error("Message content is null (Virtual Network JSON structure expected)");
				else
					virtualNetworkJsonStr = msgContent;
				
				try {
					Pair<Integer,String> mapKey = new Pair<>(tenantId, vnoName);
					if (!tenantVnoNameIdMap.containsKey(mapKey))
						throw new VNOException( String.format(
								"no VNO named '%s' from tenant #%d has been created", vnoName, tenantId) );
					
					existingVnoId = tenantVnoNameIdMap.get(mapKey);
					reloadVNO(existingVnoId, tenantId, vnoName,
							virtualNetworkJsonStr);
				} catch (VNOException e) {
					console.error("Could not reload VNO",
							e.getMessage());
					response.setStringProperty("status-reply", "FAIL");
					response.setText(e.getMessage()); // send the error cause
														// back to requester
					return response;

				}

				buildLoadReplyMessage(response, tenantId, existingVnoId, vnoName);

				response.setStringProperty("status-reply", "SUCCESS");
				console.info("VNO Reload Request accepted");
				
				return response;
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
				response.setStringProperty("status-reply", "ERROR");
				response.setText(e.getMessage());
				return response;
			}
		case "DELETE":
			String vnoName;
			int existingVnoId;
			
			try {
				existingVnoId = txtMsg.getIntProperty("vno-id");
				tenantId = txtMsg.getIntProperty("tenant-id");
				vnoName = txtMsg.getStringProperty("vno-name");
				

				if ( tenantVnoNameIdMap.remove(new Pair<Integer,String>(tenantId, vnoName)) == null) {
					throw new VNOException( String.format("No VNO named %s exists for this tenant", vnoName));
				}

				deleteVNO(existingVnoId, tenantId);

				// Send vno info back
				response.setIntProperty("vno-id", existingVnoId);
				response.setIntProperty("tenant-id", tenantId);
				response.setStringProperty("vno-name", vnoName);

				response.setStringProperty("status-reply", "SUCCESS");

				console.info("VNO Deleted");

				return response;
			} catch (Exception e) {
				e.printStackTrace();
				response.setStringProperty("status-reply", "ERROR");
				response.setText(e.getMessage());
				return response;
			}
		case "VNO_STATE_UPDATE":
			console.info("Received VNO State update");
			vnoId = txtMsg.getIntProperty("vno-id");
			VNO.State vnoState = VNO.State.valueOf(txtMsg
					.getStringProperty("new-vno-state"));
			
			if (vnos.containsKey(vnoId))
				vnos.put(vnoId, vnoState);

			response.setStringProperty("status-reply", "ACK");
			console.info("Acknowledge sent >>");
			return response;

		case "REGISTER_CONTROLLER":
//			vnoId = txtMsg.getIntProperty("vno-id");
//			tenantId = txtMsg.getIntProperty("tenant-id");
//			String controllerIpPort = txtMsg
//					.getStringProperty("controller-ip-port");
//			String managerIp = txtMsg.getStringProperty("sfc-manager-ip");
//			console.info("Received Request to Register Controller ",
//					controllerIpPort);
//
//			if (vnos.get(vnoId).equals(VNO.State.REQUESTED)) {
//				response.setStringProperty("status-reply", "FAIL");
//				response.setText("Request Denied because VNO is not loaded");
//				return response;
//			}
//
//			try {
//				updateOvsControllers(Util.fromStringSetToString(controllerAddressList));
//				addManagerToNetwork(tenantId, managerIp);
//			} catch (IOException e) {
//				response.setStringProperty("status-reply", "ERROR");
//				e.printStackTrace();
//				response.setText("Failed to add controller to switches");
//				return response;
//			}

			response.setStringProperty("status-reply", "SUCCESS");
			return response;

		default:
			response.setText("Unrecognized Command");
			response.setStringProperty("status-reply", "ERROR");
			return response;

		}

	}

	/**
	 * Wrapper method to a script that remotely configure (via SSH) managed
	 * Openvswitches to set their controllers' IP addresses. It is a requirement
	 * that an ssh key-pair must exist between Arbiter's server, and each OVS
	 * server. TODO: Path to script location is
	 * hard-coded. Script should be added as a resource and be embedded in the
	 * java application. Or maybe use a Java SSH library. TODO: Controllers for
	 * LINC-OE switches are hard-coded in sys.config file. I'm not sure it's
	 * possible to dynamically add them. Check that.
	 * 
	 * @param ipPort
	 * @throws IOException
	 */
	@Deprecated
	public void addControllerToNetwork(String ipPort) throws IOException {
		
		this.controllerAddressList.add("tcp:" + ipPort);
		// Convert set of controller's addresses to a single
		// String to be given as input to the script
		String controllerListStr = Util
				.fromStringSetToString(this.controllerAddressList);
		updateOvsControllers(controllerListStr);
		

	}

	@Deprecated
	private void updateOvsControllers(String controllerListStr) throws IOException {
		String[] cmd = new String[] { "/bin/sh",
				"../../../scripts/remote-add-controller.sh", controllerListStr };
		Process pr = Runtime.getRuntime().exec(cmd);
	}

	/**
	 * Wrapper method to a script that remotely configure (via SSH) managed
	 * Openvswitches to set their OVSDB managers' IP addresses. It is a
	 * requirement that an ssh key-pair must exist between Arbiter's server, and
	 * each OVS server. TODO: Path to script location is hard-coded. Script
	 * should be added as a resource and be embedded in the java application. Or
	 * maybe use a Java SSH library. TODO: Controllers for LINC-OE switches are
	 * hard-coded in sys.config file. I'm not sure it's possible to dynamically
	 * add them. Check that.
	 *
	 * @throws IOException
	 */
	@Deprecated
	public void addManagerToNetwork(int tenantId, String ipAdress)
			throws IOException {
		String[] cmd;
		Process pr;

		Set<String> tenantHostAddrs = this.getTenantHostAddresses(tenantId);
		Set<String> tenantManagerAddrs;

		if (this.tenantOvsdbManagerAddresses == null)
			this.tenantOvsdbManagerAddresses = new HashMap<Integer, Set<String>>();
		
		if (tenantOvsdbManagerAddresses.containsKey(tenantId))
			tenantManagerAddrs = tenantOvsdbManagerAddresses.get(tenantId);
		else
			tenantManagerAddrs = new HashSet<String>();
		
		// Add new IP address to existing
		tenantManagerAddrs.add("tcp:" + ipAdress + ":6640");

		this.tenantOvsdbManagerAddresses.put(tenantId, tenantManagerAddrs);
		// Convert set of controller's addresses to a single
		// String to be given as input to the script
		String managerListStr = Util
				.fromStringSetToString(this.tenantOvsdbManagerAddresses
						.get(tenantId));

		
		
		StringBuilder allManagers = new StringBuilder();
		allManagers.append(managerListStr);
		
		for (String hostAddr : tenantHostAddrs) {
			// Get existing manager addresses of this host
			if (hostOvsdbAddresses.containsKey(hostAddr)) {
				allManagers.append(hostOvsdbAddresses.get(hostAddr));
			} else {
				hostOvsdbAddresses.put(hostAddr,managerListStr);
			}
			
			console.debug( "Manager list for OVSDB at", hostAddr, allManagers.toString()  );
			cmd = new String[] { "/bin/sh", "../../../scripts/remote-add-manager.sh",
					hostAddr, allManagers.toString() };
			pr = Runtime.getRuntime().exec(cmd);
		}

	}

	public static void main(String[] args) throws IOException,
			InvalidMappingException, InterruptedException, VNOException,
			WebKATException {

		System.out.println("Starting SPN operating system");
		ConfigLoader cfgLoader = ConfigLoader.getLoader();
		cfgLoader.printGlobalConfiguration();
		cfgLoader.printMappingTenantConfiguration();

		String physFilename = cfgLoader.getGlobalConfig()
				.getPhysicalFileLocation();
		PhysicalNetwork physical = null;
		try {
			physical = PhysicalNetwork.fromJson(Util
					.jsonFromFilename(physFilename));

			System.out.println(">> Read physical topology from " + physFilename);
		} catch (InvalidConfigurationException e) {
			System.out.println(">> Could not read physical topology file."
					+ e.toString());
			return;
		}

		Arbiter arbiter = new Arbiter(physical);
		// Run forever
		Thread.currentThread().join();
	}

}
