package spn.os.controller.onos.openflow;

public class LambdaCriterion extends Criterion {
	public int lambda;
	
	public LambdaCriterion(int lambda) {
		super(Type.OCH_SIGID);
		//This is redundant but needed for GSON serialization:
		this.type = Type.OCH_SIGID;
		this.lambda = lambda;
	}

	public int getLambda() {
		return lambda;
	}

	public void setLambda(int lambda) {
		this.lambda = lambda;
	}
}
