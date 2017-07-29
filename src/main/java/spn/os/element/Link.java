package spn.os.element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;

public abstract class Link {
    protected BigInteger id;
    protected Vertex srcVertex;
    protected Vertex dstVertex;
    protected Port srcPort;
    protected Port dstPort;

    public Link() {
        this.id        = BigInteger.ZERO;
        this.srcVertex = null;
        this.srcPort   = null;
        this.dstVertex = null;
        this.dstPort   = null;
    }

    public Link(BigInteger id, Vertex src, Port srcPort, Vertex dst, Port dstPort) {
        this.id = id;
        this.srcVertex = src;
        this.srcPort   = srcPort;
        this.dstVertex = dst;
        this.dstPort   = dstPort;
    }

    public Link(BigInteger id, Vertex src, Vertex dst) {
        this.id = id;
        this.srcVertex = src;
        this.dstVertex = dst;
        this.srcPort = null;
        this.dstPort = null;
    }

    public BigInteger getId() {
        return this.id;
    }

    public Vertex getSrcVertex() {
        return this.srcVertex;
    }

    public Vertex getDstVertex() {
        return this.dstVertex;
    }

    public Port getSrcPort() {
        return this.srcPort;
    }

    public Port getDstPort() {
        return this.dstPort;
    }

    public String toString() {
        return this.srcVertex.getName() + ":" + this.srcPort + "->"
            + this.dstVertex.getName() + ":" + this.srcPort;
    }

    // Using this much reflection is probably a bad idea, but the only way to
    // get around it is to make the class concrete instead of abstract.
    @SuppressWarnings("rawtypes")
    public Link reverse(BigInteger id)
        throws InstantiationException, IllegalAccessException,
        IllegalArgumentException, InvocationTargetException {
    	Class<? extends Link> c = this.getClass();
    	Constructor[] ctors = c.getDeclaredConstructors();
    	Constructor ctor = null;
    	for (int i = 0; i < ctors.length; i++) {
    	    ctor = ctors[i];
    	    if (ctor.getParameterTypes().length == 5)
    		break;
    	}
    	Link l = (Link)ctor.newInstance(id, this.dstVertex, this.dstPort,
                                        this.srcVertex, this.srcPort);
    	return l;
    }
    
    public String prettyPrint() {
    	StringBuilder pretty = new StringBuilder().append("SRC Vertex:\n").append(srcVertex.prettyPrint());
    	pretty.append("\n").append("SRC Port: ").append(srcPort);
    	pretty.append("\n").append("DST Vertex:\n").append(dstVertex.prettyPrint());
    	pretty.append("\n").append("DST Port: ").append(dstPort);
    	
    	return pretty.toString();
    	
    }
}
