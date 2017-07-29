package spn.examples.PacketMapping;

import org.xml.sax.SAXException;
import spn.exception.InvalidMappingException;
import spn.exception.SwitchConnectionException;
import spn.exception.VNOException;
import spn.exception.WebKATException;
import spn.netkat.And;
import spn.netkat.Drop;
import spn.netkat.Filter;
import spn.netkat.IfThen;
import spn.netkat.IfThenElse;
import spn.netkat.Modification;
import spn.netkat.Policy;
import spn.netkat.Sequence;
import spn.netkat.Test;
import spn.netkat.Union;
import spn.netkat.WebKAT;
import spn.os.Mapping;
import spn.os.Messenger;
import spn.os.Tenant;
import spn.os.Util;
import spn.os.VNO;
import spn.os.controller.DefaultController;
import spn.os.controller.onos.ONOSController;
import spn.os.element.VirtualNetwork;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by vime on 7/15/17.
 */
public class PacketMappingTenant extends Tenant {

    public PacketMappingTenant(int id, Messenger messenger) {
        super(id, "PacketMappingTenant", messenger);
        loadVnoDefinitions();
        init();
    }

    private void loadVnoDefinitions() {
        for (PacketMappingTenantConfigLoader.VNOConfig vnoConfig : PacketMappingTenantConfigLoader.getLoader()
                .getVnoConfigs()) {
            String vnoName = vnoConfig.getName();
            String virtFileLoc = vnoConfig.getVirtFileLocation();
            VirtualNetwork vnoVirtualNw = Util.readVirtual(virtFileLoc);
            this.virtualNetworks.put(vnoName, vnoVirtualNw);
        }
    }

    private void init() {
        String netKatIpAddr = PacketMappingTenantConfigLoader.getLoader().getGlobalConfig().getNetkatIpAddress();
        String controllerIpAddr = PacketMappingTenantConfigLoader.getLoader().getGlobalConfig().getControllerIpAddress();
        int controllerPort = PacketMappingTenantConfigLoader.getLoader().getGlobalConfig().getControllerPort();
        String controllerType = PacketMappingTenantConfigLoader.getLoader().getGlobalConfig().getControllerType();

        this.webKAT = new WebKAT(String.format("http://%s:9000", netKatIpAddr));
        try {
            this.webKAT.removeDuplicates(false);
        } catch (WebKATException e) {
            console.error("An error occurred during WebKAT configuration",
                          e.getMessage());
            console.error("Is WebKAT Running?");
        }

        switch (controllerType.toLowerCase()) {
            case "onos":
                this.controller = new ONOSController(controllerIpAddr, controllerPort, webKAT);
                console.info("Using ONOS controller");
                break;
            default:
                console.info("Using Default controller");
                this.controller = new DefaultController(webKAT);
                console.info("Creating Controller Thread...");
                this.controllerThread = new Thread((DefaultController) controller);
                console.info("Start Controller Thread...");
                this.controllerThread.start();
        }

    }

    public void help() {
        System.out.println("Available commands:");
        console.menu("load", "vno-name");
        console.menu("reload", "vno-name");
        console.menu("unload", "vno-name");
        console.menu("activate", "vno-name");
        console.menu("deactivate", "vno-name");
        console.menu("remap", "vno-name", "mapping-id");
        console.menu("exit");
        console.menu("help");
    }

    public void console() throws IOException, InvalidMappingException,
            InterruptedException, VNOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(
                System.in));
        System.out.println("Welcome to the SPN OS Tenant console");
        help();
        while (true) {
            System.out.print(">> ");
            String line = buffer.readLine();
            if (line == null) {
                continue;
            }
            String[] words = line.split(" ");
            String cmd = words[0].toLowerCase();

            if (words.length < 2 && !cmd.equalsIgnoreCase("help")
                    && !cmd.equalsIgnoreCase("exit")) {
                System.out.println("VNO name required.");
                help();
                continue;
            }

            if (cmd.equals("load")) {
                System.out.println(load(words[1]));
            } else if (cmd.equals("reload")) {
                System.out.println(reload(words[1]));
            } else if (cmd.equals("unload")) {
                System.out.println(unload(words[1]));
            } else if (cmd.equals("activate")) {
                System.out.println(activate(words[1]));
            } else if (cmd.equals("deactivate")) {
                System.out.println(deactivate(words[1]));
            } else if (cmd.equals("remap")) {
                try {
                    if (words.length < 4) {
                        System.out.println(remap(words[1], Integer.parseInt(words[2])));
                    } else {
                        System.out
                                .println("usage: remap <vno-name> <mapping-id>");
                    }
//					System.out.println(remap(words[1], words[2]));
                } catch (IndexOutOfBoundsException e) {
                    System.out
                            .println("usage: remap <vno-name> <mapping-id>");
                }
            } else if (cmd.equals("exit")) {
                exit();
            } else if (cmd.equals("help")) {
                help();
            } else {
                if (!cmd.equals("")) {
                    System.out.println("Unknown command: |" + cmd + "|");
                }
                help();
            }
        }
    }

    public String load(String vnoName) throws InvalidMappingException,
            VNOException {

        this.messenger.requestVNO(this, vnoName, virtualNetworks.get(vnoName));

        return "VNO requested";
    }

    public String reload(String vnoName) throws InvalidMappingException,
            VNOException {

        //Re-read Virtual Network json file
        for (PacketMappingTenantConfigLoader.VNOConfig cfg : PacketMappingTenantConfigLoader.getLoader().getVnoConfigs()) {
            if (cfg.getName().equalsIgnoreCase(vnoName)) {
                String virtFileLoc = cfg.getVirtFileLocation();
                console.info("Reloading virtual network from", virtFileLoc);
                VirtualNetwork vnoVirtualNw = Util.readVirtual(virtFileLoc);
                virtualNetworks.put(vnoName, vnoVirtualNw);
            }
        }

        VirtualNetwork vnoVirtualNw = this.virtualNetworks.get(vnoName);


        this.messenger.reloadVNO(this, vnoName, vnoVirtualNw);

        return "VNO Reload Request sent";
    }

    public String unload(String vnoName) throws InvalidMappingException,
            VNOException {

        VNO vno = this.getVNO(vnoName);
        if (vno != null) {
            this.messenger.deleteVNO(vno);
        } else {
            return String.format("No VNO with name '%s' found", vnoName);
        }

        return "VNO Unload Request sent";
    }

    public String activate(String vnoName) {
        VNO vno = getVNO(vnoName);
        try {
            if (vno != null) {
                vno.activate();
                return "VNO activated";
            } else {
                return "VNO not loaded. Please run `load` first.";
            }
        } catch (VNOException e1) {
            return e1.toString();
        } catch (SwitchConnectionException e2) {
            e2.printStackTrace();
            vno.setState(VNO.State.LOADED);
            return "Unable to activate: lost connection with one or more switches";
        }
    }

    public String deactivate(String vnoName) {
        VNO vno = getVNO(vnoName);
        try {
            if (vno != null) {
                vno.deactivate();
                return "VNO deactivated";
            } else {
                throw new VNOException("Could not find VNO with name "
                                               + vnoName);
            }
        } catch (VNOException e) {
            return e.toString();
        } catch (NullPointerException e) {
            return "Could not deactivate VNO. "
                    + "Please run `load` and `activate` first.";
        }
    }

    public String remap(String vnoName, int mappingId)
            throws InvalidMappingException, VNOException {
        VNO vno = getVNO(vnoName);
        try {
            vno.getWebKATInstance().removeDuplicates(false);
        } catch (WebKATException e) {
            console.error("Failed to set removeDuplicates in NetKAT");
        }

        if (vno == null) {
            return "Could not find VNO with name " + vnoName;
        }

        Mapping mapping;
        int mappingArrIndex = mappingId - 1;

        try {
            mapping = this.mappings.get(mappingArrIndex);
        } catch (IndexOutOfBoundsException e) {
            return "Mapping ID does not exist";
        }

        vno.loadMap(mapping);
        try {
            vno.activate();
            return "VNO remapped";
        } catch (SwitchConnectionException e) {
            e.printStackTrace();
            return "Unable to remap: lost connection with one or more switches";
        }



    }


    public void exit() throws InterruptedException {
        System.out.println("Deactivating VNOs");
        for (VNO vno : vnos.values()) {
            deactivate(vno.getName());
            this.messenger.deleteVNO(vno);
        }
        // Thread.sleep(2000);
        System.out.println("Exiting SPN. Goodbye!");
        System.exit(0);
    }

    /**
     * VNO Builder
     *
     * @param vnoId                  ID to be used for VNO
     * @param vnoName                Name to be used for VNO
     * @param jsonStrPhysical        JSON String definition of physical network
     * @param virtualMappingsJsonStr List of JSON String definitions of virtual-to-physical mappings
     * @param opticalMappingsJsonStr List of JSON String definition of optical mappings
     * @param tenantHostListJson     JSON List of Tenant's hosts
     * @param nwAddressSpace         Network Addres space definition
     */
    public void buildVNO(int vnoId, String vnoName, String jsonStrPhysical,
                         List<String> virtualMappingsJsonStr,
                         List<String> opticalMappingsJsonStr,
                         String tenantHostListJson, String nwAddressSpace) {
        this.physical = Util.buildPhysical(jsonStrPhysical);
        VirtualNetwork vnoVirtualNw = this.virtualNetworks.get(vnoName);

        for (String virtualMappingJsonStr : virtualMappingsJsonStr) {
            this.mappings.add(Util.buildMapping(virtualMappingJsonStr, vnoVirtualNw, physical));
        }

        VNO vno = new VNO(vnoId, id, vnoName, this.controller, this.webKAT,
                          this.messenger, this.physical, vnoVirtualNw, tenantHostListJson, nwAddressSpace);
        vno.addObserver(messenger);
        this.registerVNO(vno);

        try {
            vno.setVirtualLayer(policy, inPolicy, this.mappings.get(0));
            vno.setState(VNO.State.LOADED);
            console.info("VNO object built.");
        } catch (VNOException e) {
            console.error("Failed to load VNO", e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * VNO Update method
     *
     * @param vnoId                  ID of VNO to update
     * @param jsonStrPhysical        JSON String definition of physical network
     * @param virtualMappingsJsonStr List of JSON String definitions of virtual-to-physical mappings
     * @param opticalMappingsJsonStr List of JSON String definition of optical mappings
     * @param tenantHostListJson     JSON List of Tenant's hosts
     * @param nwAddressSpace         Network Addres space definition
     */
    public void updateVNO(int vnoId, String vnoName, String jsonStrPhysical,
                          List<String> virtualMappingsJsonStr,
                          List<String> opticalMappingsJsonStr,
                          String tenantHostListJson, String nwAddressSpace) {
        this.physical = Util.buildPhysical(jsonStrPhysical);
        VirtualNetwork vnoVirtualNw = virtualNetworks.get(vnoName);

        for (String virtualMappingJsonStr : virtualMappingsJsonStr) {
            this.mappings.add(Util.buildMapping(virtualMappingJsonStr, vnoVirtualNw, physical));
        }

        VNO vno = getVNO(vnoId);
        if (vno == null) {
            buildVNO(vnoId, vnoName, jsonStrPhysical, virtualMappingsJsonStr,
                     opticalMappingsJsonStr, tenantHostListJson, nwAddressSpace);
            vno = getVNO(vnoId);
        }

        vno.setNetworkAddressSpace(nwAddressSpace);

        this.registerVNO(vno);

        try {
            Mapping virtualMapping = vno.getMapping();
            vno.removeAllMapping();
            vno.setVirtualLayer(policy, inPolicy, virtualMapping);
            vno.setState(VNO.State.LOADED);
            console.info("VNO reloaded.");
        } catch (VNOException e) {
            console.error("Failed to reload VNO", e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Virtual policy for BigSwitch example.
     * Host h1 can ping host h3 and vice-versa
     * Host h2 can ping host h4 and vice-versa
     *
     * @return
     */
    @Override
    protected Policy getVirtualPolicy() {
        Policy vs1Uvs2;
        vs1Uvs2 = new Union(
                //vs1 policy
                new Sequence(new Filter(new Test("switch", "1")),
                           new Union(
                                   new Sequence(new Filter(new And(
                                           new Test("ipDst", "10.0.0.2"),
                                           new Test("ipSrc", "10.0.0.1"))),
                                                new Modification("port", "2")),
                                   new Sequence(new Filter(new And(
                                           new Test("ipDst", "10.0.0.1"),
                                           new Test("ipSrc", "10.0.0.2"))),
                                                new Modification("port", "1"))
                           )),
                //vs2 policy
                new Sequence(new Filter(new Test("switch", "2")),
                             new Union(
                                     new Sequence(new Filter(new And(
                                             new Test("ipDst", "10.0.0.1"),
                                             new Test("ipSrc", "10.0.0.2"))),
                                                  new Modification("port", "2")),
                                     new Sequence(new Filter(new And(
                                             new Test("ipDst", "10.0.0.2"),
                                             new Test("ipSrc", "10.0.0.1"))),
                                                  new Modification("port", "1"))
                             ))
                            );

        return vs1Uvs2;
    }

    /**
     * Virtual ingress policy for BigSwitch example
     * This defines the which physical endpoints will
     * map to the virtual ingress/egress of VNO
     *
     * @return
     */
    @Override
    protected Policy getVirtualIngressPolicy() {
        return new Sequence(
                new Union(
                        new Sequence(
                                new Modification("vport", "1"),
                                new IfThenElse(
                                        new And(
                                                new Test("switch", "1"),
                                                new Test("port", "1")),
                                        new Modification("vswitch", "1"),
                                        new IfThen(
                                                new And(
                                                        new Test("switch", "2"),
                                                        new Test("port", "1")),
                                                new Modification("vswitch", "2"))
                                )
                        )
                ));
    }

    public static void main(String[] args) throws IOException,
            InvalidMappingException, InterruptedException, VNOException,
            WebKATException {

        System.out.println("Starting SPN operating system");

        PacketMappingTenantConfigLoader bsConfigLoader = PacketMappingTenantConfigLoader
                .getLoader();
        try {
            bsConfigLoader.loadConfiguration();
        } catch (ParserConfigurationException | SAXException e) {
            System.err.println("Could not load XML Config file: " + e.getMessage());
        }

        int tenantId = bsConfigLoader.getGlobalConfig().getTenantId();

        Messenger messenger = new Messenger(bsConfigLoader
                                                    .getGlobalConfig().getArbiterIpAddress());
        PacketMappingTenant tenant = new PacketMappingTenant(tenantId, messenger);

        tenant.console();
    }

}
