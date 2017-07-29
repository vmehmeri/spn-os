package spn.os.controller.onos;

import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.Ipv4DstCriterion;
import spn.os.controller.onos.openflow.Ipv4SrcCriterion;
import spn.os.controller.onos.openflow.OutputInstruction;
import spn.os.controller.onos.openflow.VlanIdCriterion;

public class IpVlanMatchFlowBuilder extends FlowBuilder {

	public IpVlanMatchFlowBuilder(short priority, String deviceId, String srcIp,
			String dstIp, int vlanId, int inPort, int outPort) {
		super();
		InPortCriterion inPortMatch = new InPortCriterion(Integer.toString(inPort));
		VlanIdCriterion lambdaMatch = new VlanIdCriterion(Integer.toString(vlanId));
		Ipv4SrcCriterion ipSrcMatch = new Ipv4SrcCriterion(srcIp);
		Ipv4DstCriterion ipDstMatch = new Ipv4DstCriterion(dstIp);
		OutputInstruction outputAction = new OutputInstruction(
				Integer.toString(outPort));

		this.setPriority(priority).setDeviceId(deviceId)
				.addMatchCriterion(inPortMatch).addMatchCriterion(lambdaMatch)
				.addActionInstruction(outputAction);
		
		if (ipSrcMatch.getIp() != null) {
			this.addMatchCriterion(ipSrcMatch);
		}
		
		if (ipDstMatch.getIp() != null) {
			this.addMatchCriterion(ipDstMatch);
		}
	}

}
