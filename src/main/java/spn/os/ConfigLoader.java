package spn.os;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import spn.os.ConfigLoader.TenantConfig.HostConfig;
import spn.os.ConfigLoader.TenantConfig.OpticalMappingConfig;
import spn.os.ConfigLoader.TenantConfig.VirtualMappingConfig;
import spn.os.element.ForwardingPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

	public final static String CONFIG_FILE_PATH = "arbiter_config.xml";
	

	private static ConfigLoader instance = null;
	private boolean loaded = false;
	private Console console = Console.getConsole(this);
	private List<TenantConfig> mappingTenantsConfig;
	private GlobalConfig globalConfig;

	protected ConfigLoader() {
		// Exists only to prevent instantiation.
	}

	public static ConfigLoader getLoader() {
		if (instance == null) {
			instance = new ConfigLoader();
		}
		return instance;
	}

	public void loadConfiguration() throws ParserConfigurationException,
			SAXException, IOException {

//		ClassLoader classLoader = getClass().getClassLoader();
		File fXmlFile = new File(CONFIG_FILE_PATH);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		// optional, but recommended
		// read this -
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
		Element globalNode = (Element) doc.getElementsByTagName("global").item(
				0);
		Element tenantsNode = (Element) doc.getElementsByTagName("tenants").item(
				0);

		this.globalConfig = loadGlobals(globalNode);

		mappingTenantsConfig = loadMappingTenantConfig(tenantsNode);

		

		loaded = true;

	}

	

	public GlobalConfig getGlobalConfig() {
		if (!loaded) {
			try {
				loadConfiguration();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return globalConfig;
	}

	public List<TenantConfig> getMappingTenantConfig() {
		if (!loaded) {
			try {
				loadConfiguration();
			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mappingTenantsConfig;
	}

	private GlobalConfig loadGlobals(Element root) {
		Node arbiterIpAddrNode = root
				.getElementsByTagName("arbiter-ip-address").item(0);
		Node physicalFileLocation = root.getElementsByTagName(
				"physical-file-location").item(0);

		GlobalConfig globalCfg = new GlobalConfig();

		Element arbiterIpAddr = (Element) arbiterIpAddrNode;
		Element phyFileLoc = (Element) physicalFileLocation;
		globalCfg.setArbiterIpAddress(arbiterIpAddr.getTextContent());
		globalCfg.setPhysicalFileLocation(phyFileLoc.getTextContent());

		return globalCfg;
	}

	private List<TenantConfig> loadMappingTenantConfig(Element root) {
		NodeList tenants = root.getElementsByTagName("tenant");
		List<TenantConfig> mapTenantsConfig = new ArrayList<TenantConfig>();

		for (int temp = 0; temp < tenants.getLength(); temp++) {

			Node tenantNode = tenants.item(temp);

			if (tenantNode.getNodeType() == Node.ELEMENT_NODE) {

				Element tenant = (Element) tenantNode;
				String tenantId = tenant.getAttribute("id");


				Node networkNode = tenant.getElementsByTagName("network").item(0);
				String networkName = ((Element) networkNode).getElementsByTagName("name").item(0).getTextContent();
				String addressSpace = ((Element) networkNode).getElementsByTagName("address-space").item(0).getTextContent();
				
				TenantConfig mappingTenant = instance.new TenantConfig(
						Integer.parseInt(tenantId)).
						setNetworkName(networkName).
						setNetworkAddressSpace(addressSpace);
				
				NodeList hosts = tenant.getElementsByTagName("host");
				NodeList opticalMappings = tenant
						.getElementsByTagName("optical-mapping");
				NodeList virtualMappings = tenant
						.getElementsByTagName("virtual-mapping");

				int item_index;

				for (item_index = 0; item_index < hosts.getLength(); item_index++) {
					Node hostNode = hosts.item(item_index);

					if (hostNode.getNodeType() == Node.ELEMENT_NODE) {

						Element host = (Element) hostNode;
						String hostId = host.getAttribute("id");
						String hostIpAddress = host
								.getElementsByTagName("mgmt-ip-address")
								.item(0).getTextContent();
						String hostDpIpAddress = host
								.getElementsByTagName("dp-ip-address").item(0)
								.getTextContent();
						String hostDomainName = host
								.getElementsByTagName("domain").item(0)
								.getTextContent();
						HostConfig hostConf = mappingTenant.new HostConfig(
								Integer.parseInt(hostId), hostIpAddress,
								hostDpIpAddress, hostDomainName);
						mappingTenant.addHost(hostConf);
					}
				}

				if (opticalMappings != null) {
					for (item_index = 0; item_index < opticalMappings.getLength(); item_index++) {
						Node omNode = opticalMappings.item(item_index);

						if (omNode.getNodeType() == Node.ELEMENT_NODE) {

							Element opticalMapping = (Element) omNode;
							String omId = opticalMapping.getAttribute("id");
							String fileLocation = opticalMapping
									.getElementsByTagName("file-location").item(0)
									.getTextContent();
							OpticalMappingConfig omConf = mappingTenant.new OpticalMappingConfig(
									Integer.parseInt(omId), fileLocation);
							mappingTenant.addOpticalMapping(omConf);
						}
					}
				}

				for (item_index = 0; item_index < virtualMappings.getLength(); item_index++) {
					Node vmNode = virtualMappings.item(item_index);

					if (vmNode.getNodeType() == Node.ELEMENT_NODE) {

						Element virtualMapping = (Element) vmNode;
						String vmId = virtualMapping.getAttribute("id");
						String fileLocation = virtualMapping
								.getElementsByTagName("file-location").item(0)
								.getTextContent();
						VirtualMappingConfig vmConf = mappingTenant.new VirtualMappingConfig(
								Integer.parseInt(vmId), fileLocation);
						mappingTenant.addVirtualMapping(vmConf);
					}
				}

				mapTenantsConfig.add(mappingTenant);

			}
		}

		return mapTenantsConfig;
	}

	public class TenantConfig {
		private int id;
		private String networkName;
		private String networkAddressSpace;
		private List<HostConfig> hosts;
		private List<OpticalMappingConfig> opticalMappings;
		private List<VirtualMappingConfig> virtualMappings;

		private TenantConfig(int id) {
			this.id = id;
			hosts = new ArrayList<HostConfig>();
			opticalMappings = new ArrayList<OpticalMappingConfig>();
			virtualMappings = new ArrayList<VirtualMappingConfig>();
		}

		public int getId() {
			return id;
		}

		public List<HostConfig> getHostsConfig() {
			return hosts;
		}

		public List<OpticalMappingConfig> getOpticalMappings() {
			return opticalMappings;
		}

		public List<VirtualMappingConfig> getVirtualMappings() {
			return virtualMappings;
		}
		
		public String getNetworkName() {
			return networkName;
		}
		
		public String getNetworkAddressSpace() {
			return networkAddressSpace;
		}
		
		private TenantConfig setNetworkName(String name) {
			this.networkName = name;
			return this;
		}
		
		private TenantConfig setNetworkAddressSpace(String addressSpace) {
			this.networkAddressSpace = addressSpace;
			return this;
		}

		private void addHost(HostConfig host) {
			hosts.add(host);
		}

		private void addOpticalMapping(OpticalMappingConfig opticalMapping) {
			opticalMappings.add(opticalMapping);
		}

		private void addVirtualMapping(VirtualMappingConfig virtualMapping) {
			virtualMappings.add(virtualMapping);
		}

		class HostConfig {
			private int id;
			private String ipAddress;
			private String dpIpAddress;
			private String domainName;

			private HostConfig(int id, String ipAddress, String dpIpAddress, String domainName) {
				this.id = id;
				this.ipAddress = ipAddress;
				this.dpIpAddress = dpIpAddress;
				this.domainName = domainName;
			}

			public int getId() {
				return id;
			}

			public String getMgmtIpAddress() {
				return ipAddress;
			}

			public String getDpIpAddress() {
				return dpIpAddress;
			}
			
			public String getDomainName() {
				return domainName;
			}
		}

		class OpticalMappingConfig {
			private int id;
			private String fileLocation;

			private OpticalMappingConfig(int id, String fileLocation) {
				this.id = id;
				this.fileLocation = fileLocation;
			}

			public int getId() {
				return id;
			}

			public String getFileLocation() {
				return fileLocation;
			}
		}

		class VirtualMappingConfig {
			private int id;
			private String fileLocation;

			private VirtualMappingConfig(int id, String fileLocation) {
				this.id = id;
				this.fileLocation = fileLocation;
			}

			public int getId() {
				return id;
			}

			public String getFileLocation() {
				return fileLocation;
			}
		}
	}

	class GlobalConfig {
		private String arbiterIpAddress;
		private String physicalFileLocation;

		private GlobalConfig() {

		}

		private void setArbiterIpAddress(String ipAddr) {
			this.arbiterIpAddress = ipAddr;
		}

		private void setPhysicalFileLocation(String fileLoc) {
			this.physicalFileLocation = fileLoc;
		}

		public String getArbiterIpAddress() {
			return arbiterIpAddress;
		}

		public String getPhysicalFileLocation() {
			return physicalFileLocation;
		}
	}


	public void printMappingTenantConfiguration() {
		ConfigLoader cfgLoader = ConfigLoader.getLoader();

		List<TenantConfig> mpCfgList = cfgLoader
				.getMappingTenantConfig();

		for (TenantConfig cfg : mpCfgList) {
			console.info("--------------------------------");
			console.info( String.format("Tenant ID: %d",cfg.getId()) );
			console.info( String.format("Network: %s (%s)", cfg.getNetworkName(), cfg.getNetworkAddressSpace()) );
			console.info("--------------------------------");
			/*System.out.println("Hosts:");

			for (HostConfig hCfg : cfg.getHostsConfig()) {
				System.out.println(">>ID: " + hCfg.getId());
				System.out.println(">>Ip Address: " + hCfg.getMgmtIpAddress());
			}

			System.out.println("Optical Mappings:");
			for (OpticalMappingConfig omCfg : cfg.getOpticalMappings()) {
				System.out.println(">>ID: " + omCfg.getId());
				System.out.println(">>File Location: "
						+ omCfg.getFileLocation());
			}

			System.out.println("Virtual Mappings:");
			for (VirtualMappingConfig vmCfg : cfg.getVirtualMappings()) {
				System.out.println(">>ID: " + vmCfg.getId());
				System.out.println(">>File Location: "
						+ vmCfg.getFileLocation());
			}*/
		}
	}

	public void printGlobalConfiguration() {
		ConfigLoader cfgLoader = ConfigLoader.getLoader();

		GlobalConfig globalCfg = cfgLoader.getGlobalConfig();

		console.info("Using Following Global Configuration:");
		console.info("--------------------------------");
		console.info("Arbiter IP Address", globalCfg.getArbiterIpAddress());
		console.info("Physical Network File Location", globalCfg.getPhysicalFileLocation());
		console.info("--------------------------------");

	}
}
