package spn.os.element;

public class OpticalLocation extends Location {

    protected Short wavelength;

    public OpticalLocation(Vertex vertex, Port port, Short wavelength) {
        super(vertex, port);
        this.wavelength = wavelength;
    }

    public OpticalLocation(Vertex vertex, Port port, Short wavelength,
                           boolean ingress, boolean egress) {
        super(vertex, port, ingress, egress);
        this.wavelength = wavelength;
    }

    public Short getWavelength() {
        return this.wavelength;
    }

    public void setWavelength(Short wavelength) {
        this.wavelength = wavelength;
    }
}
