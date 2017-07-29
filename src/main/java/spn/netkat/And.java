package spn.netkat;

import java.util.List;
import java.util.ArrayList;

public class And implements Predicate {
    private List<Predicate> preds = new ArrayList<Predicate>();

    public And(Predicate... preds) {        
        for (int i=0; i < preds.length; i++) { 
	    this.preds.add(preds[i]);
	}
    }
    
    public And(Predicate left, Predicate right) {
        this.preds.add(left);
        this.preds.add(right);
    }
    
    public String toString() {
        String result = "{ \"type\" : \"and\", \"preds\" : [ ";
	for (int i = 0; i < preds.size(); i++) { 
	    if (i != 0) { 
		result += ", ";
	    }
	    result += preds.get(i).toString();
	}
	result += "] }";
	return result;
    }
}
