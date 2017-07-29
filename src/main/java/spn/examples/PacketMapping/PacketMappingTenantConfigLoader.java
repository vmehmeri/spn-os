package spn.examples.PacketMapping;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import spn.os.Console;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vime on 7/15/17.
 */
public class PacketMappingTenantConfigLoader {
    private boolean loaded = false;
    private static PacketMappingTenantConfigLoader instance = null;
    private PacketMappingTenantConfigLoader.GlobalConfig globalCfg;
    private List<PacketMappingTenantConfigLoader.VNOConfig> vnoConfigs;
    private Console console = Console.getConsole(this);

    protected PacketMappingTenantConfigLoader() {
        // Exists only to prevent instantiation
    }

    public static PacketMappingTenantConfigLoader getLoader() {
        if (instance == null) {
            instance = new PacketMappingTenantConfigLoader();
        }
        return instance;
    }

    public PacketMappingTenantConfigLoader.GlobalConfig getGlobalConfig() {
        return globalCfg;
    }

    public List<PacketMappingTenantConfigLoader.VNOConfig> getVnoConfigs() {
        return vnoConfigs;
    }

    public void loadConfiguration() throws ParserConfigurationException, SAXException, IOException {
        File fXmlFile = new File("/root/tenant_config.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        // optional, but recommended
        // read this -
        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        Element globalNode = (Element) doc.getElementsByTagName("global").item(0);
        NodeList vnoNodeList = doc.getElementsByTagName("vno");
        globalCfg = loadGlobals(globalNode);
        vnoConfigs = loadVnoConfig(vnoNodeList);
        loaded = true;
    }

    private PacketMappingTenantConfigLoader.GlobalConfig loadGlobals(Element root) {
        PacketMappingTenantConfigLoader.GlobalConfig globalCfg = new PacketMappingTenantConfigLoader.GlobalConfig();

        Element tenantId = (Element) root.getElementsByTagName("tenant-id").item(0);
        Element arbiterIpAddr = (Element) root.getElementsByTagName("arbiter-ip-address").item(0);
        Element controllerIpAddr = (Element) root.getElementsByTagName("controller-ip-address").item(0);
        Element controllerPort = (Element) root.getElementsByTagName("controller-port").item(0);
        Element controllerType = (Element) root.getElementsByTagName("controller-type").item(0);
        Element netkatIpAddr = (Element) root.getElementsByTagName("netkat-ip-address").item(0);

        globalCfg.setTenantId(Integer.parseInt(tenantId.getTextContent()));
        globalCfg.setArbiterIpAddress(arbiterIpAddr.getTextContent());
        globalCfg.setControllerIpAddress(controllerIpAddr.getTextContent());
        globalCfg.setControllerPort(Integer.parseInt(controllerPort.getTextContent()));
        globalCfg.setControllerType(controllerType.getTextContent());
        globalCfg.setNetkatIpAddress(netkatIpAddr.getTextContent());

        return globalCfg;

    }

    private List<PacketMappingTenantConfigLoader.VNOConfig> loadVnoConfig(NodeList vnoNodeList) {
        List<PacketMappingTenantConfigLoader.VNOConfig> vnoConfigs = new ArrayList<>();


        for (int temp = 0; temp < vnoNodeList.getLength(); temp++) {
            Node vnoNode = vnoNodeList.item(temp);


            if (vnoNode.getNodeType() == Node.ELEMENT_NODE) {
                Element vnoEl = (Element) vnoNode;
                Element vnoNameEl = (Element) vnoEl.getElementsByTagName("name").item(0);
                String vnoName = vnoNameEl.getTextContent();
                console.info("Loading config for " , vnoName);
                String virtFileLoc;

                Element virtFileLocEl = (Element) vnoEl.getElementsByTagName("virtual-file-location").item(0);
                virtFileLoc = virtFileLocEl.getTextContent();

                vnoConfigs.add(new PacketMappingTenantConfigLoader.VNOConfig(vnoName, virtFileLoc));
            }
        }

        return vnoConfigs;

    }

    public class GlobalConfig {
        private int tenantId;
        private String arbiterIpAddress;
        private String controllerIpAddress;
        private int controllerPort;
        private String controllerType;
        private String netkatIpAddress;
//		private String virtFileLocation;


        private GlobalConfig() {

        }

        private void setArbiterIpAddress(String ipAddr) {
            this.arbiterIpAddress = ipAddr;
        }

        private void setControllerIpAddress(String ipAddr) {
            this.controllerIpAddress = ipAddr;
        }

        private void setControllerPort(int port) {
            this.controllerPort = port;
        }

        private void setControllerType(String controllerType) {
            this.controllerType = controllerType;
        }

        private void setNetkatIpAddress(String ipAddr) {
            this.netkatIpAddress = ipAddr;
        }

        private void setTenantId(int id) {
            this.tenantId = id;
        }

        public String getArbiterIpAddress() { return arbiterIpAddress; }
        public String getControllerIpAddress() { return controllerIpAddress; }
        public int getControllerPort() { return controllerPort; }
        public String getControllerType() { return controllerType; }
        public String getNetkatIpAddress() { return netkatIpAddress; }
        public int getTenantId() { return tenantId; }
    }

    public class VNOConfig {
        private String name;
        private String virtFileLocation;

        private VNOConfig() {

        }

        private VNOConfig(String vnoName, String virtFileLocation) {
            this.virtFileLocation = virtFileLocation;
            this.name = vnoName;
        }

        private void setVirtFileLocation(String fileLoc) {
            this.virtFileLocation = fileLoc;
        }

        private void setName(String vnoName) {
            this.name = vnoName;
        }

        public String getVirtFileLocation() { return virtFileLocation; }
        public String getName() { return name; }
    }

}
