package spn.os.controller;

import spn.exception.SwitchConnectionException;
import spn.netkat.WebKAT;
import spn.os.Console;
import spn.os.OFSwitch;
import spn.os.element.Switch;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vime on 7/4/17.
 */
public abstract class Controller {

    protected String ipAddress;
    protected int port;
    protected final String DEFAULT_IP_ADDRESS = "localhost";
    protected final int DEFAULT_PORT = 6633;

    protected Map<String, OFSwitch> switches;
    protected WebKAT webKAT;
    protected Console console = Console.getConsole(this);

    public Controller(String ipAddress, int port, WebKAT wk) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.webKAT = wk;
        this.switches = new HashMap<String, OFSwitch>();
    }

    public Controller(WebKAT wk) {
        this.ipAddress = DEFAULT_IP_ADDRESS;
        this.port = DEFAULT_PORT;
        this.webKAT = wk;
        this.switches = new HashMap<String, OFSwitch>();

    }

    public int getPort() {
        return port;
    }

    public String getIpAddress() {
        return ipAddress;
    }


    public abstract void refresh(Iterable<? extends Switch> switches) throws SwitchConnectionException;
    public abstract void clearFlowtable(Iterable<? extends Switch> switches);

}

