package spn.os.controller.onos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import spn.exception.InvalidActionException;
import spn.exception.InvalidConfigurationException;
import spn.netkat.WebKAT;
import spn.os.OFSwitch;
import spn.os.Util;
import spn.os.controller.Controller;
import spn.os.controller.onos.openflow.Criterion;
import spn.os.controller.onos.openflow.EthTypeCriterion;
import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.Instruction;
import spn.os.controller.onos.openflow.Ipv4DstCriterion;
import spn.os.controller.onos.openflow.Ipv4SrcCriterion;
import spn.os.controller.onos.openflow.LambdaCriterion;
import spn.os.controller.onos.openflow.LambdaModificationInstruction;
import spn.os.controller.onos.openflow.OutputInstruction;
import spn.os.controller.onos.openflow.VlanIdCriterion;
import spn.os.controller.onos.openflow.VlanModificationInstruction;
import spn.os.controller.onos.openflow.VlanPopInstruction;
import spn.os.controller.onos.openflow.VlanPushInstruction;
import spn.os.element.Switch;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic class for controllers
 *
 * @author Victor Mehmeri <vmehmeri@gmail.com>
 *
 */

public class ONOSController extends Controller {

	protected String _BASE_REST_URL = "http://%s:8181/onos/v1/";
	protected final String NETWORK_CONFIG_FILE_PREFIX = "/root/network_config_";
	protected Adapter adapter;
	protected String baseRestUrl;

	/**
	 * Constructor specifying IP Address and Port of ONOS instance
	 * @param ipAddress IP Address in String format
	 * @param port Openflow Port number
	 * @param wk WebKAT instance
	 */
	public ONOSController(String ipAddress, int port, WebKAT wk) {
		super(ipAddress, port, wk);
		this.baseRestUrl = String.format(_BASE_REST_URL,ipAddress);
		this.adapter = new Adapter();
		getTopology();
//		pushInitialNetworkConfiguration();
	}

	/**
	 * Constructor for Default IP address and Port (localhost:6633)
	 * @param wk WebKAT instance
	 */
	public ONOSController(WebKAT wk) {
		super(wk);
		this.baseRestUrl = String.format(_BASE_REST_URL,ipAddress);
		this.adapter = new Adapter();
		getTopology();
//		pushInitialNetworkConfiguration();

	}

	class Adapter {

		public Adapter() {

		}

		private String post(String path, String body) {
			try {
				String prefix = "--> POST " + path;

				CredentialsProvider provider = new BasicCredentialsProvider();
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
						"onos", "rocks");
				provider.setCredentials(AuthScope.ANY, credentials);

				HttpClient client = HttpClientBuilder.create()
						.setDefaultCredentialsProvider(provider).build();
				HttpPost postReq = new HttpPost(baseRestUrl + path);
				postReq.setEntity(new StringEntity(body));
				postReq.setHeader("Content-type", "application/json");
				HttpResponse response = client.execute(postReq);
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode < 200 || statusCode > 299) {
					console.error("POST failed:");
					console.json(body);
//					console.debug("Reply: " + statusCode + " "
//							+ response.toString());
				}
				return response.toString();
			} catch (Exception e) {
				String errMsg = e.getMessage();
				console.error("POST failed", errMsg);
				return errMsg;
			}
		}

		private String get(String path) {
			try {
				String prefix = "--> GET " + path;

				CredentialsProvider provider = new BasicCredentialsProvider();
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
						"onos", "rocks");
				provider.setCredentials(AuthScope.ANY, credentials);

				HttpClient client = HttpClientBuilder.create()
						.setDefaultCredentialsProvider(provider).build();
				HttpResponse response = client.execute(new HttpGet(
						baseRestUrl + path));
				int statusCode = response.getStatusLine().getStatusCode();
				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));

				String line = "";
				StringBuilder result = new StringBuilder();
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				return result.toString();
			} catch (Exception e) {
				String errMsg = e.getMessage();
				console.error("GET failed", errMsg);
				return errMsg;
			}
		}

		private String delete(String path) {
			try {
				String prefix = "--> DELETE " + path;
				// String result = Request.Get(BASE_REST_URL + path).execute()
				// .returnContent().asString();
				// console.jsonToFile(prefix, "", result, "/root/json_out");
				// return result;
				CredentialsProvider provider = new BasicCredentialsProvider();
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
						"onos", "rocks");
				provider.setCredentials(AuthScope.ANY, credentials);

				HttpClient client = HttpClientBuilder.create()
						.setDefaultCredentialsProvider(provider).build();
				HttpResponse response = client.execute(new HttpDelete(
						baseRestUrl + path));
				int statusCode = response.getStatusLine().getStatusCode();

				String result = Integer.toString(statusCode);
//				console.debug("Reply: " + result);
				return result;
			} catch (Exception e) {
				String errMsg = e.getMessage();
				console.error("DELETE failed", errMsg);
				return errMsg;
			}
		}

		private void sendDeleteAll(OFSwitch sw) {
			// Delete all flows of switch
			String deviceFlowPathUrl = "flows/" + sw.getDeviceId();
			String flows = get("flows/" + sw.getDeviceId());
			// console.json(devices);

			JsonElement flowsEl = new JsonParser().parse(flows);
			JsonArray flowsArr = flowsEl.getAsJsonObject().getAsJsonArray(
					"flows");

			if (flowsArr == null) return;
			
			for (JsonElement flowEl : flowsArr) {
				JsonObject flow = flowEl.getAsJsonObject();
				String flowId = flow.get("id").toString();
				delete(deviceFlowPathUrl + "/"
						+ flowId.substring(1, flowId.length() - 1));
			}
		}

		// Installs new flow table for switch
		public void refresh(OFSwitch sw, JsonArray flowTable) {
			// sendDeleteAll(sw);
			if (flowTable == null) {
				console.error("Failed to obtain flowTable from the NetKAT server");
				console.error("Please check if server is running");
				return;
			}

			if (sw == null) {
				console.debug("switch object is null. Is Mininet running?");
				return;
			}

			console.info("--> Pushing flowtable to switch " + sw.getDeviceId());
			// console.json(flowTable.toString());

			for (JsonElement flowEntry : flowTable) {
				try {
					// console.debug(String.format(
					// "Adding flow entry to switch %s",
					// sw.getDeviceId()));
					String deviceId = sw.getDeviceId();
					// console.debug("NetKAT flow entry:");
					// console.json(flowEntry);
					String flowModJsonStr = flowModFromJson(flowEntry, deviceId);
					// SEND TO ONOS
					this.post("flows/" + deviceId, flowModJsonStr);
				} catch (InvalidActionException e) {
					console.error("Invalid flow entry : " + e.toString());
				}
			}
		}

		private String flowModFromJson(JsonElement flowEntry, String deviceId)
				throws InvalidActionException {
			// Short priority = flowEntry.getAsJsonObject().get("priority")
			// .getAsShort();
			int priority = flowEntry.getAsJsonObject().get("priority")
					.getAsInt();
			JsonObject pattern = flowEntry.getAsJsonObject().get("pattern")
					.getAsJsonObject();
			JsonArray actionArray = flowEntry.getAsJsonObject().get("action")
					.getAsJsonArray();

			List<Criterion> criteria = matchFromJson(pattern);
			List<Instruction> actionList = actionsFromJson(actionArray);

			FlowBuilder flowBuilder = new FlowBuilder().setMatchCriteria(
					criteria).setActionInstructions(actionList);
			flowBuilder.setPriority(priority);
			flowBuilder.setDeviceId(deviceId);

			return flowBuilder.buildFlowJsonString();

		}

		private List<Instruction> actionsFromJson(JsonArray actionArray)
				throws InvalidActionException {
			List<Instruction> actionList = new ArrayList<Instruction>();

			for (JsonElement actionEntry : actionArray) {
				for (JsonElement actionEntry2 : actionEntry.getAsJsonArray()) {
					JsonArray action = actionEntry2.getAsJsonArray();
					String tag = action.get(0).getAsString();

					if (tag.equalsIgnoreCase("Output")) {
						String outport;
						JsonObject output = action.get(1).getAsJsonObject();
						String outputType = output.get("type").getAsString();

						if (outputType.equals("inport"))
							// outport = 0xfffffff8;
							outport = "0xfffffff8";
						else if (outputType.equals("physical"))
							outport = Integer.toString(output.get("port")
									.getAsInt());
						else
							throw new InvalidActionException(
									"Invalid Action : " + tag);

						actionList.add(new OutputInstruction(outport));
					} else if (tag.equalsIgnoreCase("Modify")) {
						JsonArray mod = action.get(1).getAsJsonArray();
						String modType = mod.get(0).getAsString();

						if (modType.equals("SetVlan")) {
							Short vlan = mod.get(1).getAsShort();
							// VLAN 65535 as a short wraps around to -1
							if (vlan == -1) {
								// TODO(basus): this should be strip for OF1.0
								// and pop for 1.3
								actionList.add(new VlanPopInstruction());
								// actionList.add(actions.popVlan());
							} else {
								actionList.add(new VlanPushInstruction());
								actionList.add(new VlanModificationInstruction(
										vlan));
							}
							// actionList.add(actions.setVlanVid(VlanVid.ofVlan(vlan)));
						} else if (modType.equals("SetWavelength")) {
							Short lambda = mod.get(1).getAsShort();
							actionList.add(new LambdaModificationInstruction(
									lambda));
						} else
							throw new InvalidActionException(
									"Invalid Modification : " + modType);
					}
				}
			}

			return actionList;
		}

		private List<Criterion> matchFromJson(JsonObject pattern) {
			// int inPort = -1;
			String inPort = null;
			int ethType = -1;
			String ipSrc = null;
			String ipDst = null;
			short vlanId = -2;
			short lambda = -1;

			List<Criterion> criteria = new ArrayList<Criterion>();

			if (!pattern.get("inPort").isJsonNull()) {
				inPort = Integer.toString(pattern.get("inPort").getAsInt());
				// mb = mb.setExact(MatchField.IN_PORT,
				// OFPort.ofShort(inPort));
			}

			if (!pattern.get("dlTyp").isJsonNull()) {
				ethType = pattern.get("dlTyp").getAsInt();
				// mb = mb.setExact(MatchField.ETH_TYPE,
				// EthType.of(pattern.get("dlTyp").getAsInt()));
			}

			if (!pattern.get("nwSrc").isJsonNull()) {
				ipSrc = pattern.get("nwSrc").getAsString() + "/32";
				// mb = mb.setExact(MatchField.IPV4_SRC,
				// IPv4Address.of(pattern.get("nwSrc").getAsString()));
			}

			if (!pattern.get("nwDst").isJsonNull()) {
				ipDst = pattern.get("nwDst").getAsString() + "/32";
				// mb = mb.setExact(MatchField.IPV4_DST,
				// IPv4Address.of(pattern.get("nwDst").getAsString()));

			}

			if (!pattern.get("dlVlan").isJsonNull()) {
				vlanId = pattern.get("dlVlan").getAsShort();
				// mb = mb.setExact(MatchField.VLAN_VID,
				// OFVlanVidMatch.ofRawVid(pattern.get("dlVlan").getAsShort()));
			}

			if (pattern.has("wavelength")
					&& !pattern.get("wavelength").isJsonNull()) {
				lambda = pattern.get("wavelength").getAsShort();
				// CircuitSignalID sigid = new CircuitSignalID((byte) 1, (byte)
				// 2,
				// lambda, (short) 1);
				// mb = mb.setExact(MatchField.OCH_SIGID, sigid);
			}

			if (vlanId != -2) {
				criteria.add(new VlanIdCriterion(Integer.toString(vlanId)));
			}

			if (inPort != null) {
				criteria.add(new InPortCriterion(inPort));
			}

			if (ethType != -1) {
				criteria.add(new EthTypeCriterion(Integer.toString(ethType)));
			}

			if (ipSrc != null) {
				criteria.add(new Ipv4SrcCriterion(ipSrc));
			}

			if (ipDst != null) {
				criteria.add(new Ipv4DstCriterion(ipDst));
			}

			if (lambda != -1) {
				criteria.add(new LambdaCriterion(lambda));
			}

			return criteria;

		}
	}

	/**
	 * Get network topology from ONOS and populate switches list
	 * TODO: There should be a thread running this method every few
	 * TODO: seconds to guarantee up-to-date topology information
	 */
	public void getTopology() {
		try {
			String devices = this.adapter.get("devices");
			// console.json(devices);

			JsonElement devicesEl = new JsonParser().parse(devices);
			JsonArray devicesArr = devicesEl.getAsJsonObject().getAsJsonArray(
					"devices");

			for (JsonElement deviceEl : devicesArr) {
				JsonObject device = deviceEl.getAsJsonObject();
				String deviceId = device.get("id").toString();
				String deviceType = device.get("type").toString();
				String deviceIdWithoutQuotes = deviceId.substring(1,
						deviceId.length() - 1);
				//console.debug("Found device #" + deviceIdWithoutQuotes);
				if (this.switches == null)
					this.switches = new HashMap<>();
				this.switches.put(deviceIdWithoutQuotes, new OFSwitch(
						deviceIdWithoutQuotes));
			}
		} catch (Exception e) {
			console.debug("Could not get topology");
		}
	}

	@Deprecated
	public int findOutCurrentVnoMapping(int vnoId) {
		String networkConfig = this.adapter.get("network/configuration");

		JsonElement rootEl = new JsonParser().parse(networkConfig);
		JsonObject linkObj = rootEl.getAsJsonObject().getAsJsonObject("links");

		if (vnoId == 1) {
			JsonArray vnoIdArr = linkObj.getAsJsonObject("of:0000000000000001/2-of:0000000000000011/1")
					.getAsJsonObject("basic").getAsJsonArray("vno-ids");
			
			if (vnoIdArr.get(0) != null && vnoIdArr.get(0).getAsInt() == vnoId)
				return 1;
			else
				return 2;
			
		} else {
			JsonArray vnoIdArr = linkObj.getAsJsonObject("of:0000000000000002/2-of:0000000000000013/1")
					.getAsJsonObject("basic").getAsJsonArray("vno-ids");
			
			if (vnoIdArr.get(0) != null && vnoIdArr.get(0).getAsInt() == vnoId)
				return 1;
			else
				return 2;
		}

	}

	@Deprecated
	public void pushNetworkConfiguration(int vnoId, int mappingId) {
		String suffix;
		if (vnoId == 1) {
			suffix = String.format("vno1-%d_vno2-%d.json",
					mappingId, findOutCurrentVnoMapping(2));
		} else {
			suffix = String.format("vno1-%d_vno2-%d.json",
					findOutCurrentVnoMapping(1), mappingId);
		}
		// hard-coded stuff
		String networkConfigJson;
		try {
			networkConfigJson = Util
					.jsonStringFromFilename(NETWORK_CONFIG_FILE_PREFIX + suffix);
		} catch (InvalidConfigurationException e) {
			console.error("Could not push network configuration",
					e.getMessage());
			return;
		}
		if (this.adapter != null)
			this.adapter.post("network/configuration", networkConfigJson);
		else
			console.error("Handler has not been initialized");
	}

	@Deprecated
	protected void pushInitialNetworkConfiguration() {
		String suffix = "vno1-1_vno2-1.json";
		
		String networkConfigJson;
		try {
			networkConfigJson = Util
					.jsonStringFromFilename(NETWORK_CONFIG_FILE_PREFIX + suffix);
		} catch (InvalidConfigurationException e) {
			console.error("Could not push network configuration",
					e.getMessage());
			return;
		}
		if (this.adapter != null)
			this.adapter.post("network/configuration", networkConfigJson);
		else
			console.error("Handler has not been initialized");
	}
	

	public void refresh(Iterable<? extends Switch> switches) {
		for (Switch sw : switches) {
			if (this.switches == null || this.switches.isEmpty()) {
				getTopology();
			}
			OFSwitch ofSwitch = this.switches.get(sw.getDeviceId());
			if (ofSwitch == null) {
				console.error("Switch not found. The controller might have lost connection to the switch");
				return;
//				console.error("Switch not found!");
//				console.debug("deviceId",sw.getDeviceId());
//				if (this.switches.isEmpty())
//					console.debug("List of switches is empty!");
//				for (OFSwitch swtch : this.switches.values()) {
//					console.debug("Found switch", swtch.getDeviceId());
//				}
			}
			JsonArray flowTable = sw.getFlowTable();
//			console.debug(">> Refreshing switch " + sw.getId() + " | "
//					+ sw.getDeviceId());
			if (adapter == null) {
				console.error("Handler null");
			}
			adapter.refresh(ofSwitch, flowTable);
		}
	}

	public void clearFlowtable(Iterable<? extends Switch> switches) {
//		console.info("List of switches in controller's map:");
//		for (String devId : this.switches.keySet()) {
//			console.info(devId);
//		}

//		console.info("List of switches to be cleared:");
		for (Switch sw : switches) {
//			console.info(sw.getDeviceId());
			OFSwitch ofSwitch = this.switches.get(sw.getDeviceId());
			 console.debug(">> Clearing switch " + sw.getId());
			adapter.sendDeleteAll(ofSwitch);
		}
	}

}
