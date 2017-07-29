package spn.os.controller.onos.openflow;


public class LambdaModificationInstruction extends Instruction {
	
	private final String gridType = "DWDM";
	private final String channelSpacing = "CHL_50GHZ";
	private final int slotGranularity = 1;
	private int spacingMultiplier;
	
	public LambdaModificationInstruction(int lambda) {
		super(Type.L0MODIFICATION, Subtype.OCH);
		//This is redundant but needed for GSON serialization:
		this.type = Type.L0MODIFICATION;
		this.subtype = Subtype.OCH;
		this.spacingMultiplier = lambda;
	}

	public int getSpacingMultiplier() {
		return spacingMultiplier;
	}

	public void setSpacingMultiplier(int spacingMultiplier) {
		this.spacingMultiplier = spacingMultiplier;
	}

	public String getGridType() {
		return gridType;
	}

	public String getChannelSpacing() {
		return channelSpacing;
	}

	public int getSlotGranularity() {
		return slotGranularity;
	}
}
