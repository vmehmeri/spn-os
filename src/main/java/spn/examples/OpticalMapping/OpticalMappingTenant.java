package spn.examples.OpticalMapping;

import org.xml.sax.SAXException;
import spn.exception.InvalidMappingException;
import spn.exception.SwitchConnectionException;
import spn.exception.VNOException;
import spn.exception.WebKATException;
import spn.netkat.And;
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
import spn.os.OpticalMapping;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpticalMappingTenant extends Tenant {

    private Map<String, List<String>> vnoServiceTemplatesMap;

    public OpticalMappingTenant(int id, Messenger messenger) throws IOException,
            WebKATException {
        super(id, "Multiple Mapping Tenant", messenger);

        this.vnoServiceTemplatesMap = new HashMap<>();

        loadVnoDefinitions();
        init();

    }

    private void loadVnoDefinitions() {
        for (OpticalMappingTenantConfigLoader.VNOConfig vnoConfig : OpticalMappingTenantConfigLoader.getLoader()
                .getVnoConfigs()) {
            String vnoName = vnoConfig.getName();
            String virtFileLoc = vnoConfig.getVirtFileLocation();
            VirtualNetwork vnoVirtualNw = Util.readVirtual(virtFileLoc);
            this.virtualNetworks.put(vnoName, vnoVirtualNw);
        }
    }

    private void init() {
        String netKatIpAddr = OpticalMappingTenantConfigLoader.getLoader().getGlobalConfig().getNetkatIpAddress();
        String controllerIpAddr = OpticalMappingTenantConfigLoader.getLoader().getGlobalConfig().getControllerIpAddress();
        int controllerPort = OpticalMappingTenantConfigLoader.getLoader().getGlobalConfig().getControllerPort();
        String controllerType = OpticalMappingTenantConfigLoader.getLoader().getGlobalConfig().getControllerType();

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
                break;
            default:
                this.controller = new DefaultController(webKAT);
                this.controllerThread = new Thread((DefaultController) controller);
                this.controllerThread.start();
        }


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
    @Override
    public void buildVNO(int vnoId, String vnoName, String jsonStrPhysical,
                         List<String> virtualMappingsJsonStr,
                         List<String> opticalMappingsJsonStr,
                         String tenantHostListJson, String nwAddressSpace) {
        this.physical = Util.buildPhysical(jsonStrPhysical);
        VirtualNetwork vnoVirtualNw = this.virtualNetworks.get(vnoName);

        for (String virtualMappingJsonStr : virtualMappingsJsonStr) {
            this.mappings.add(Util.buildMapping(virtualMappingJsonStr, vnoVirtualNw, physical));
        }

        for (String opticalMappingJsonStr : opticalMappingsJsonStr) {
            this.opticalMappings.add(Util.buildOpticalMapping(opticalMappingJsonStr, physical));
        }


        VNO vno = new VNO(vnoId, id, vnoName, this.controller, this.webKAT,
                          this.messenger, this.physical, vnoVirtualNw, tenantHostListJson,
                          nwAddressSpace);
        vno.addObserver(messenger);
        // this.vnos.add(vno);
        this.registerVNO(vno);

        try {
            vno.setPacketLayer(this.physical, this.opticalMappings.get(0));
            vno.setVirtualLayer(policy, inPolicy, this.mappings.get(0));
            vno.setState(VNO.State.LOADED);
            console.info("VNO Loaded.");

        } catch (InvalidMappingException | VNOException e) {
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
    @Override
    public void updateVNO(int vnoId, String vnoName, String jsonStrPhysical,
                          List<String> virtualMappingsJsonStr,
                          List<String> opticalMappingsJsonStr,
                          String tenantHostListJson, String nwAddressSpace) {
        this.physical = Util.buildPhysical(jsonStrPhysical);
        VirtualNetwork vnoVirtualNw = virtualNetworks.get(vnoName);

        for (String virtualMappingJsonStr : virtualMappingsJsonStr) {
            this.mappings.add(Util.buildMapping(virtualMappingJsonStr, vnoVirtualNw, physical));
        }

        for (String opticalMappingJsonStr : opticalMappingsJsonStr) {
            this.opticalMappings.add(Util.buildOpticalMapping(opticalMappingJsonStr, physical));
        }

        VNO vno = getVNO(vnoId);
        if (vno == null) {
            buildVNO(vnoId, vnoName, jsonStrPhysical, virtualMappingsJsonStr,
                     opticalMappingsJsonStr, tenantHostListJson, nwAddressSpace);
            vno = getVNO(vnoId);
        }

        vno.setNetworkAddressSpace(nwAddressSpace);

//		if (vno == null) {
//			buildVNO(vnoId, vnoName, jsonStrPhysical, jsonStrMapping1,
//					jsonStrMapping2, jsonStrOptMapping1, jsonStrOptMapping2,
//					tenantHostListJson, nwAddressSpace);
//			console.info("VNO reloaded.");
//			return;
//		}

        this.registerVNO(vno);

        try {
            OpticalMapping currentOpticalMapping = vno.getOpticalMapping();
            Mapping virtualMapping = vno.getMapping();
            vno.removeAllOpticalMapping();
            vno.removeAllMapping();
            vno.setPacketLayer(this.physical, currentOpticalMapping);
            vno.setVirtualLayer(policy, inPolicy, virtualMapping);


            vno.setState(VNO.State.LOADED);
            console.info("VNO reloaded.");
        } catch (InvalidMappingException | VNOException e) {
            console.error("Failed to reload VNO", e.getMessage());
            e.printStackTrace();
        }
    }



    public void help() {
        System.out.println("\nAvailable commands:");
        console.menu("load", "vno-name");
        console.menu("reload", "vno-name");
        console.menu("unload", "vno-name");
        console.menu("activate", "vno-name");
        console.menu("deactivate", "vno-name");
        console.menu("remap", "vno-name");
        console.menu("exit");
        console.menu("help");
        System.out.print(">> ");
    }

    public void console() throws IOException, InvalidMappingException,
            InterruptedException, VNOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(
                System.in));
        System.out.println("Welcome to the SPN OS Tenant console");
        help();
        while (true) {

            String line = buffer.readLine();
            if (line == null) {
                continue;
            }
            String[] words = line.split(" ");
            String cmd = words[0].toLowerCase();

            if (words.length == 0) {
                help();
            }

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
                    if (words.length == 2) {
                        System.out.println(remap(words[1]));

                    }
//					System.out.println(remap(words[1], words[2]));
                } catch (IndexOutOfBoundsException e) {
                    System.out
                            .println("usage: remap  <vno-name>");
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
        for (OpticalMappingTenantConfigLoader.VNOConfig cfg : OpticalMappingTenantConfigLoader.getLoader().getVnoConfigs()) {
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

    public String remap(String layer, String vnoName)
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

        //TODO Here it is only toggling between mappings 1 and 2.
        //TODO The code must be changed so that the mapping ID is given
        //TODO from the console and then the corresponding mapping is selected.
        if (layer.equalsIgnoreCase("packet")) {
            if (vno.getMapping() == this.mappings.get(0)) {
                console.info("Selecting Mapping #2");
                vno.mapTo(this.mappings.get(1));
            } else {
                vno.mapTo(this.mappings.get(0));
                console.info("Selecting Mapping #1");
            }
            return "Remapped VNO packet layer";
        } else if (layer.equalsIgnoreCase("optical")) {
            if (vno.getOpticalMapping() == this.opticalMappings.get(0)) {
                vno.opticalMapTo(this.opticalMappings.get(1));
//				this.controller.pushNetworkConfiguration(vno.getId(), 1);
            } else {
                vno.opticalMapTo(this.opticalMappings.get(0));
//				this.controller.pushNetworkConfiguration(vno.getId(), 2);
            }
            return "Remapped VNO optical layer";
        } else {
            return "Unknown layer. Must be either `packet` or `optical`.";
        }
    }

    public String remap(String vnoName)
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


        if (vno.getOpticalMapping() == this.opticalMappings.get(0)) {
            vno.loadOpticalMap(this.opticalMappings.get(1));
        } else {
            vno.loadOpticalMap(this.opticalMappings.get(0));
        }

        try {
            vno.activate();
            return "VNO remapped";
        } catch (SwitchConnectionException e) {
            return "Unable to remap: lost connection with one or more switches";
        }

		/*if (vno.getMapping() == this.mapping1) {
			this.controller.pushNetworkConfiguration(vno.getId(), 1);
		} else {
			this.controller.pushNetworkConfiguration(vno.getId(), 2);
		}*/


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


    @Override
    protected Policy getVirtualPolicy() {
        Policy vs1, vs2;

        vs1 = new Sequence(new Filter(new Test("switch", "1")), new Union(
                new Sequence(new Filter(new And(new Test("ipDst",
                                                         "10.0.0.2"), new Test("ipSrc", "10.0.0.1"))),
                             new Modification("port", "2")), new Sequence(
                new Filter(new And(new Test("ipDst", "10.0.0.1"),
                                   new Test("ipSrc", "10.0.0.2"))),
                new Modification("port", "1"))));
        vs2 = new Sequence(new Filter(new Test("switch", "2")), new Union(
                new Sequence(new Filter(new And(new Test("ipDst",
                                                         "10.0.0.1"), new Test("ipSrc", "10.0.0.2"))),
                             new Modification("port", "2")), new Sequence(
                new Filter(new And(new Test("ipDst", "10.0.0.2"),
                                   new Test("ipSrc", "10.0.0.1"))),
                new Modification("port", "1"))));
        return new Union(vs1, vs2);

    }

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

        System.out.println("Starting SPN Operating System - Optical Mapping Tenant example");

        OpticalMappingTenantConfigLoader mpConfigLoader = OpticalMappingTenantConfigLoader
                .getLoader();
        try {
            mpConfigLoader.loadConfiguration();
        } catch (ParserConfigurationException | SAXException e) {
            System.err.println("Could not load XML Config file: " + e.getMessage());
        }

        int tenantId = mpConfigLoader.getGlobalConfig().getTenantId();

        Messenger messenger = new Messenger(mpConfigLoader
                                                    .getGlobalConfig().getArbiterIpAddress());
        OpticalMappingTenant tenant = new OpticalMappingTenant(tenantId, messenger);

        tenant.console();
    }


}
