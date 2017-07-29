package spn.os.controller.onos;

import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.Ipv4DstCriterion;
import spn.os.controller.onos.openflow.Ipv4SrcCriterion;
import spn.os.controller.onos.openflow.OutputInstruction;
import spn.os.controller.onos.openflow.VlanModificationInstruction;

public class VlanModFlowBuilder extends FlowBuilder {
	
	public VlanModFlowBuilder(short priority, String deviceId, String srcIp,
			String dstIp, int newVlanId, int inPort, int outPort) {
		super();
		InPortCriterion inPortMatch = new InPortCriterion(Integer.toString(inPort));
		Ipv4SrcCriterion ipSrcMatch = new Ipv4SrcCriterion(srcIp);
		Ipv4DstCriterion ipDstMatch = new Ipv4DstCriterion(dstIp);
		VlanModificationInstruction vlanAction = new VlanModificationInstruction(newVlanId);
		OutputInstruction outputAction = new OutputInstruction(
				Integer.toString(outPort));

		this.setPriority(priority).setDeviceId(deviceId)
				.addActionInstruction(vlanAction)
				.addActionInstruction(outputAction);
		
		if (inPortMatch.getPort() != null)
			this.addMatchCriterion(inPortMatch);
		
		if (ipSrcMatch.getIp() != null) {
			this.addMatchCriterion(ipSrcMatch);
		}
		
		if (ipDstMatch.getIp() != null) {
			this.addMatchCriterion(ipDstMatch);
		}
	}

}
