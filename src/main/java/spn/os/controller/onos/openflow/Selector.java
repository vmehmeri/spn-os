package spn.os.controller.onos.openflow;

import java.util.ArrayList;
import java.util.List;

public class Selector {

	private List<Criterion> criteria;
	
	public Selector(List<Criterion> criteria) {
		this.criteria = criteria;
	}
	
	public Selector() {
		this.criteria = new ArrayList<Criterion>();
	}
	
	public void addCriterion(Criterion c) {
		this.criteria.add(c);
	}
}
