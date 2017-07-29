package spn.os.element;

public class OpticalChannel {
	// The ingress and egress locations are the transponder ports where 
	// packet traffic enters and leaves the optical network
	protected Location ingress;
	protected Location egress;
	protected OpticalPath path;
	protected Short wavelength;
	
	public OpticalChannel() {
		this.ingress = null;
		this.egress = null;
		this.wavelength = null;
		this.path = new OpticalPath();
	}
	
	public OpticalChannel(Location ingress, Location egress, 
			OpticalPath path, Short wavelength) {
		this.ingress = ingress;
		this.egress = egress;
	}
}
