package spn.os.element;

public class Location {
    protected Vertex vertex;
    protected Port port;
    protected boolean ingress;
    protected boolean egress;

    public Location(Vertex v, Port p) {
        this.vertex = v;
        this.port = p;
        this.ingress = p.isIngress();
        this.egress = p.isEgress();
    }

    public Location(Vertex v, Port p, boolean ingress, boolean egress) {
        this.vertex = v;
        this.port = p;
        this.ingress = ingress;
        this.egress = egress;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public Port getPort() {
        return port;
    }

    public void setPort(Port port) {
        this.port = port;
    }
}
