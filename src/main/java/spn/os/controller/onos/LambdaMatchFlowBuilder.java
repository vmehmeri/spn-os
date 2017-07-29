package spn.os.controller.onos;

import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.LambdaCriterion;
import spn.os.controller.onos.openflow.OutputInstruction;

public class LambdaMatchFlowBuilder extends FlowBuilder {
	
	public LambdaMatchFlowBuilder(short priority, String deviceId, int lambdaId, int inPort,
			int outPort) {
		super();
		InPortCriterion inPortMatch = new InPortCriterion(Integer.toString(inPort));
		LambdaCriterion lambdaMatch = new LambdaCriterion(lambdaId);
		OutputInstruction outputAction = new OutputInstruction(
				Integer.toString(outPort));
		
		this.setPriority(priority).setDeviceId(deviceId)
				.addMatchCriterion(inPortMatch).addMatchCriterion(lambdaMatch)
				.addActionInstruction(outputAction);
	}

}
