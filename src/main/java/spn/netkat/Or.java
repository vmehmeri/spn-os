package spn.netkat;

import java.util.List;
import java.util.ArrayList;

public class Or implements Predicate {
    private List<Predicate> preds = new ArrayList<Predicate>();

    public Or(Predicate... preds) { 
        for (int i=0; i < preds.length; i++) { 
	    this.preds.add(preds[i]);
	}
    }

    public String toString() {
        String result = "{ \"type\" : \"or\", \"preds\" : [ ";
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