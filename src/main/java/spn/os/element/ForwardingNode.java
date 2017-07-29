package spn.os.element;

public class ForwardingNode {
	private String name;
	private Domain domain;
	private String image;
	private String vduName;
	
	public ForwardingNode() {
		this.domain = Domain.UNDEFINED;
		this.image = "None";
	}
	
	public String getName() {
		return name;
	}

	public ForwardingNode setName(String name) {
		this.name = name;
		return this;
	}

	public Domain getDomain() {
		return domain;
	}

	public ForwardingNode setDomain(Domain domain) {
		this.domain = domain;
		return this;
	}

	public String getImage() {
		return image;
	}

	public String getVduName() {
		return vduName;
	}

	public ForwardingNode setImage(String image) {
		this.image = image;
		return this;
	}

	public ForwardingNode setVduName(String vduName) {
		this.vduName = vduName;
		return this;
	}

	public enum Domain {
	    CLOUD1, CLOUD2, CLOUD3, CLOUD4, DC1, DC2, DC3, DC4, CPE, UNDEFINED ;
	    
	    public static Domain fromString(String domainName) {
	    	if (domainName.equalsIgnoreCase("cloud1"))
	    		return CLOUD1;
	    	else if (domainName.equalsIgnoreCase("cloud2"))
	    		return CLOUD2;
	    	else if (domainName.equalsIgnoreCase("cloud3"))
	    		return CLOUD3;
	    	else if (domainName.equalsIgnoreCase("cloud4"))
	    		return CLOUD4;
	    	else if (domainName.equalsIgnoreCase("dc1"))
	    		return DC1;
	    	else if (domainName.equalsIgnoreCase("dc2"))
	    		return DC2;
	    	else if (domainName.equalsIgnoreCase("dc3"))
	    		return DC3;
	    	else if (domainName.equalsIgnoreCase("dc4"))
	    		return DC4;
	    	else
	    		return UNDEFINED;
	    }
	}
	
	public void printToConsole() {
		System.out.println("..Node: " + this.name);
		System.out.println("..Domain: " + this.domain.toString());
		System.out.println("..Image: " + this.image);
		
	}
}
