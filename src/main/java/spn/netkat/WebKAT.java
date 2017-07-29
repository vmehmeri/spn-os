package spn.netkat;

import java.util.Iterator;
import java.util.List;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import spn.exception.WebKATException;
import spn.os.Console;
import spn.os.VNO;
import spn.os.element.PhysicalNetwork;

/** Simple wrapper for the NetKAT compile server API */
public class WebKAT
{
    private String serverUrl;
    private Console console = Console.getConsole(this);

    public WebKAT(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public WebKAT() {
        this.serverUrl = new String("http://localhost:9000");
    }


    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public boolean isRunning() {
    	try {
    		this.get("/");
    		return true;
    	} catch (WebKATException we) {
    		return false;
    	}
    }

    private String get(String path) throws WebKATException {
        try {
        	String prefix = "--> GET " + path;
        	String result = Request.Get(serverUrl + path)
                    .execute().returnContent().asString();
        	console.jsonToFile(prefix,"",result,"/root/json_out");
            return result;
        } catch (Exception e) {
            throw new WebKATException("GET", serverUrl + path);
        }
    }

    private String post(String path, String body) throws WebKATException {
        try {
        	String prefix = "--> POST " + path;
        	String returnContent = Request.Post(serverUrl + path)
                    .bodyString(body, ContentType.DEFAULT_TEXT)
                    .execute().returnContent().toString();
        	console.jsonToFile(prefix,body,returnContent,"/root/json_out");
            return returnContent;
        } catch (Exception e) {
            throw new WebKATException("POST", serverUrl + path, body);
        }
    }

    public String add(int id) throws WebKATException {
        return this.get("/add-vno/" + id);
    }

    public String remove(int id) throws WebKATException {
        return this.get("/remove-vno/" + id);
    }

    // TODO(basus: add comments to differentiate the different compile methods

    // TODO(basus): add comments for function arguments
    public String compile() throws WebKATException {
        return this.get("/compile");
    }

    // TODO(basus): add comments for function arguments
    public String compile(Iterable<VNO> vnos) throws WebKATException {
        VNO vno;
        StringBuffer buffer = new StringBuffer();
        Iterator<VNO> it = vnos.iterator();
        while (it.hasNext()) {
            vno = (VNO)it.next();
            buffer.append(vno.getId());
            if (it.hasNext())
                buffer.append(";");
        }
        String response = this.post("/compile-selective/", buffer.toString());
        if (response.contains("Unknown"))
            throw new WebKATException(response);
        else
            return response;
    }

    // TODO(basus): add comments for function arguments
    public JsonArray compile (int id, String policy) throws WebKATException {
        String response = this.post("/compile-local/" + id, policy);
        if (response.equalsIgnoreCase("Parse Error"))
            throw new WebKATException("Parse error on local policy : " + policy);

        try {
            JsonParser parser = new JsonParser();
            return parser.parse(response).getAsJsonArray();
        } catch (Exception e) {
            console.error("Could not get flow table", e.toString());
            return null;
        }
    }

    public void stageVirtual(VNO vno) throws WebKATException {
        int id = vno.getId();
        String policy = vno.getVirtualPolicy().toString();
        String network = vno.getVirtualNetwork().toNetKAT("=>>");
        String ingresses = vno.getVirtualNetwork().getIngressPredicate("vswitch", "vport");
        String egresses = vno.getVirtualNetwork().getEgressPredicate("vswitch", "vport");
        String mapping = vno.getMapping().toNetKAT();

        String ipolicy;
        if (vno.getIngressPolicy() == null)
                ipolicy = vno.getIngressString();
        else
                ipolicy = vno.getIngressPolicy().toString();

//        console.info("Virtual ingress predicate: \n" + ingresses);
//        console.info("Virtual egress predicate: \n" + egresses);
//        console.info("Virtual Policy:");
//        console.json(policy);
//        console.info("Virtual ingress policy:");
//        console.json(ipolicy);
//        console.info("Virtual topology: \n"+ network);
//        console.info("Virtual mapping: \n" + mapping);

        this.post("/virtual-policy/" + id, policy);
        this.post("/virtual-topology/" + id, network);
        console.debug(this.post("/virtual-ingress-policy/" + id, ipolicy));
        this.post("/virtual-ingress-predicate/" + id, ingresses);
        this.post("/virtual-egress-predicate/" + id, egresses);
        this.post("/virtual-relation/" + id, mapping);
    }

    public void stagePhysical(PhysicalNetwork network) throws WebKATException {
        String topology = network.toNetKAT("=>");
        String ingresses = network.getIngressPredicate("switch", "port");
        String egresses = network.getEgressPredicate("switch", "port");

        console.info("Physical topology:\n" + topology);
        console.info("Physical ingress predicates:\n" + ingresses);
        console.info("Physical egress predicates:\n" + egresses);

        this.post("/physical-topology", topology);
        this.post("/physical-ingress-predicate/", ingresses);
        this.post("/physical-egress-predicate/", egresses);
    }

    public JsonArray getFlowTable(long switchId) throws WebKATException {
        try {
        	String prefix = "--> GET /get-flowtable/" + String.valueOf(switchId);
            String response = Request
                .Get(serverUrl + "/get-flowtable/" + switchId)
                .execute().returnContent().asString();
            console.jsonToFile(prefix,"",response,"/root/json_out");
            if (response.equalsIgnoreCase("None"))
                return new JsonArray();
            JsonParser parser = new JsonParser();
            return parser.parse(response).getAsJsonArray();
        } catch (Exception e) {
            throw new WebKATException("Could not get flow table : " + e.toString());
        }
    }
    
    public void removeDuplicates(boolean rd) throws WebKATException {
    	this.get("/remove-duplicates/" + rd);
    }
}
