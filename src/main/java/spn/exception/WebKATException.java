package spn.exception;

public class WebKATException extends Exception {

	public String request;
	public String url;
	public String body;

	public WebKATException(final String msg) {
        super(msg);
    }
	
	public WebKATException(final String msg, WebKATException e) {
		super(msg);
		this.request = e.request;
		this.body = e.body;
		this.url = e.url;
	}
	
	public WebKATException(String r, String u) {
		super(makeMessage(r,u,""));
		this.request = r;
		this.url = u;
		this.body = "";
	}

	public WebKATException(String r, String u, String b) {
		super(makeMessage(r,u,b));
		this.request = r;
		this.url = u;
		this.body = b;
	}
	
	private static String makeMessage(String request, String url, String body) {
		String msg = "Unable to " + request + " : " + url;
		if (body.equalsIgnoreCase(""))
			return msg;
		else
			return msg + " -> " + body;
	}
}
