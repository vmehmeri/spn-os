package spn.os.controller.onos;

import spn.os.controller.onos.openflow.InPortCriterion;
import spn.os.controller.onos.openflow.Ipv4DstCriterion;
import spn.os.controller.onos.openflow.Ipv4SrcCriterion;
import spn.os.controller.onos.openflow.OutputInstruction;

public class IpMatchFlowBuilder extends FlowBuilder {
	
	public IpMatchFlowBuilder(short priority, String deviceId, String srcIp,
			String dstIp, int inPort, int outPort) {
		super();
		InPortCriterion inPortMatch = new InPortCriterion(Integer.toString(inPort));
		Ipv4SrcCriterion ipSrcMatch = new Ipv4SrcCriterion(srcIp);
		Ipv4DstCriterion ipDstMatch = new Ipv4DstCriterion(dstIp);
		OutputInstruction outputAction = new OutputInstruction(
				Integer.toString(outPort));

		this.setPriority(priority).setDeviceId(deviceId)
				.addActionInstruction(outputAction);
		
		if (ipSrcMatch.getIp() != null) {
			this.addMatchCriterion(ipSrcMatch);
		}
		
		if (ipDstMatch.getIp() != null) {
			this.addMatchCriterion(ipDstMatch);
		}
		
		if (inPortMatch.getPort() != null) {
			this.addMatchCriterion(inPortMatch);
		}
	}

}
