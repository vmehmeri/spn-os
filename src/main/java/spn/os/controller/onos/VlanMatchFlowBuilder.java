package spn.os.controller.onos;

import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.OutputInstruction;
import spn.os.controller.onos.openflow.VlanIdCriterion;

public class VlanMatchFlowBuilder extends FlowBuilder {

	public VlanMatchFlowBuilder(short priority, String deviceId, int vlanId, int inPort,
			int outPort) {
		super();
		InPortCriterion inPortMatch = new InPortCriterion(Integer.toString(inPort));
		VlanIdCriterion vlanMatch = new VlanIdCriterion(Integer.toString(vlanId));
		OutputInstruction outputAction = new OutputInstruction(
				Integer.toString(outPort));
		
		this.setPriority(priority).setDeviceId(deviceId)
				.addMatchCriterion(inPortMatch).addMatchCriterion(vlanMatch)
				.addActionInstruction(outputAction);
	}
	
}
