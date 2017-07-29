package spn.os;

import org.jboss.netty.channel.Channel;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.types.DatapathId;

/**
 * Encapsulate a virtual switch, which corresponds to a physical switch
 *
 * @author Shrutarshi Basu <shrutarshi,basu@us.fujitsu.com>
 *
 */
public class OFSwitch
{
    protected final Channel channel;
    protected final DatapathId datapathId;
    protected final OFFactory factory;
    protected final String deviceId;
    
    public OFSwitch(DatapathId dpid, Channel ch, OFFactory factory) {
        this.datapathId = dpid;
        this.channel = ch;
        this.factory = factory;
        this.deviceId = dpid.toString();
    }
    
    public OFSwitch(String deviceId) {
    	this.deviceId = deviceId;
    	this.datapathId = null;
        this.channel = null;
        this.factory = null;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public long getDatapathId() {
        return this.datapathId.getLong();
    }

    public OFFactory getFactory() {
        return this.factory;
    }

	public String getDeviceId() {
		return this.deviceId;
	}

}
