package spn.os.controller.onos;

import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.LambdaModificationInstruction;
import spn.os.controller.onos.openflow.OutputInstruction;

public class LambdaModFlowBuilder extends FlowBuilder {

	public LambdaModFlowBuilder(short priority, String deviceId, int newLambdaId,
			int inPort, int outPort) {
		super();
		InPortCriterion inPortMatch = new InPortCriterion(
				Integer.toString(inPort));
		LambdaModificationInstruction lambdaAction = new LambdaModificationInstruction(
				newLambdaId);
		OutputInstruction outputAction = new OutputInstruction(
				Integer.toString(outPort));

		this.setPriority(priority).setDeviceId(deviceId)
				.addActionInstruction(lambdaAction)
				.addActionInstruction(outputAction);

		if (inPortMatch.getPort() != null)
			this.addMatchCriterion(inPortMatch);

		
	}
}
