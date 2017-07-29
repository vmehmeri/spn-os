package spn.os.controller.onos.openflow;

public class EthTypeCriterion extends Criterion {
	
	private String ethType;

	public EthTypeCriterion (String ethType) {
		super(Type.ETH_TYPE);
		//This is redundant but needed for GSON serialization:
		this.type = Type.ETH_TYPE;
		this.ethType = ethType;
	}

	public String getEthType() {
		return ethType;
	}


	public void setEthType(String ethType) {
		this.ethType = ethType;
	}
}
