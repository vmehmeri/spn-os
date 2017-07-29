package spn.os.controller.onos;

import java.util.List;

import spn.os.Console;
import spn.os.controller.onos.openflow.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FlowBuilder {

	protected Flow flow;
	protected Selector sel;
	protected Treatment treat;
	private Gson gson;
	private Console console = Console.getConsole(this);

	public FlowBuilder() {
		flow = new Flow();
		sel = new Selector();
		treat = new Treatment();

		// Set default flow properties
		flow.setTimeout(0);
		flow.setPermanent(true);

		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	public FlowBuilder setDeviceId(String deviceId) {
		flow.setDeviceId(deviceId);
		return this;
	}
	
	public FlowBuilder setMatchCriteria(List<Criterion> criteria) {
		if (criteria != null) {
			for (Criterion c : criteria) {
				addMatchCriterion(c);
			}
		}
		
		return this;
	}
	
	public FlowBuilder setActionInstructions(List<Instruction> actions) {
		if (actions != null) {
			for (Instruction i : actions) {
				addActionInstruction(i);
			}
		}
		
		return this;
	}

	public FlowBuilder addMatchCriterion(Criterion c) {
		sel.addCriterion(c);
		return this;
	}

	public FlowBuilder addActionInstruction(Instruction i) {
		treat.addInstruction(i);
		return this;
	}

	public FlowBuilder setFlowTimeout(int timeout) {
		flow.setTimeout(timeout);
		return this;
	}

	public FlowBuilder setFlowPermanent(boolean isPermanent) {
		flow.setPermanent(isPermanent);
		return this;
	}
	
	public FlowBuilder setPriority(int priority) {
		flow.setPriority(priority);
		return this;
	}

	public Flow getFlow() {
		this.flow.setSelector(sel);
		this.flow.setTreatment(treat);
		return flow;
	}

	public String buildFlowJsonString() {
		String result = gson.toJson(getFlow());
//		console.json(result);
		return result;
	}

}
