package spn.os;

import spn.netkat.Policy;
import spn.netkat.WebKAT;
import spn.os.controller.Controller;
import spn.os.element.PhysicalNetwork;
import spn.os.element.VirtualNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Tenant {
    protected final int id;
    protected final String name;
    protected Messenger messenger;
    protected Map<Integer,VNO> vnos;
    protected List<Mapping> mappings;
    protected List<OpticalMapping> opticalMappings;
    protected Policy policy;
    protected Policy inPolicy;
    protected PhysicalNetwork physical;
    protected Map<String,VirtualNetwork> virtualNetworks;
    protected Thread controllerThread;
    protected Controller controller;
    protected WebKAT webKAT;
    protected Console console = Console.getConsole(this);

    public Tenant(int id, String name, Messenger messenger) {
    	this.id = id; 
        this.name = name;
        this.messenger = messenger;
        this.vnos = new HashMap<Integer, VNO>();
        this.virtualNetworks = new HashMap<>();
        this.mappings = new ArrayList<Mapping>();
        this.opticalMappings = new ArrayList<OpticalMapping>();
        this.policy = getVirtualPolicy();
        this.inPolicy = getVirtualIngressPolicy();
    }

    public int getId() {
        return this.id;
    }
    
    public String getName() {
        return this.name;
    }

    /**
     * Add a VNO to this tenant's possession
     */
    public boolean registerVNO(VNO vno)
    {
        vnos.put(vno.getId(), vno);
        return true;
    }

    /**
     * Remove a VNO from this tenant's possession
     */
    public boolean unregisterVNO(VNO vno)
    {
        vnos.remove(vno.getId());
        return true;
    }

    /**
     * @return the VNO with the id or null if the id doesn't exist
     */
    public VNO getVNO(int id) {
        return vnos.containsKey(id) ? vnos.get(id) : null;
    }
    
    /**
     * Get VNO by name
     * @param name VNO's name
     * @return VNO object with requested name
     */
    public VNO getVNO(String name) {
    	for (VNO vno : vnos.values()) {
    		if (vno.getName().equalsIgnoreCase(name))
    			return vno;
    	}
        return null;
    }


    protected abstract Policy getVirtualPolicy();
    protected abstract Policy getVirtualIngressPolicy();
    protected abstract void buildVNO(int vnoId, String vnoName, String jsonStrPhysical,
                                    List<String> virtualMappingsJsonStr,
                                    List<String> opticalMappingsJsonStr,
                                    String tenantHostListJson, String nwAddressSpace);
    protected abstract void updateVNO(int vnoId, String vnoName, String jsonStrPhysical,
                                      List<String> virtualMappingsJsonStr,
                                      List<String> opticalMappingsJsonStr,
                                      String tenantHostListJson, String nwAddressSpace);

}
