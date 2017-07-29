package spn.os.element;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;

import spn.netkat.Drop;
import spn.netkat.Policy;
import spn.netkat.Union;
import spn.os.VNO;

public class Switch extends Vertex {

    protected List<Policy> policies;
    protected JsonArray flowTable;
    protected Map<VNO, List<Policy>> ownedPolicies;

    public Switch() {
        super();
        this.policies = new LinkedList<Policy>();
        this.ownedPolicies = new HashMap<VNO, List<Policy>>();
    }

    public Switch(BigInteger id, String name)
    {
        super(id, name);
        this.policies = new LinkedList<Policy>();
        this.ownedPolicies = new HashMap<VNO, List<Policy>>();
    }

    public void addPolicy(VNO vno, Policy policy) {
        List<Policy> policies = this.ownedPolicies.get(vno);
        if (policies == null) {
            policies = new LinkedList<Policy>();
            policies.add(policy);
            this.ownedPolicies.put(vno, policies);
        } else {
            policies.add(policy);
        }
        this.policies.add(policy);
    }

    public void removePolicies(VNO vno) {
        if (this.ownedPolicies.remove(vno) != null)
            refreshPolicies();
    }

    public Policy getPolicy() {
    	if (this.policies.isEmpty())
            return new Drop();
    	else
            return new Union(this.policies);
    }

    private void refreshPolicies() {
        List<Policy> newPolicies = new LinkedList<Policy>();
        for (Map.Entry<VNO, List<Policy>> entry : this.ownedPolicies.entrySet())
            newPolicies.addAll(entry.getValue());

        this.policies = newPolicies;
    }

    public JsonArray getFlowTable() {
        return this.flowTable;
    }

    public void setFlowTable(JsonArray table) {
        this.flowTable = table;
    }

}
