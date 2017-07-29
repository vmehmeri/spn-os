package spn.os;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.projectfloodlight.openflow.protocol.OFMessage;

public class OFMessageEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel,
                            Object msg) throws Exception {
    	
    	ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
    	
    	if (msg instanceof Iterable) {
    		@SuppressWarnings("unchecked")
    		Iterable<OFMessage> msgList = (Iterable<OFMessage>)msg;

    		for (OFMessage ofm :  msgList) {
    			ofm.writeTo(buf);
    		}
    		return buf;
    	} else if (msg instanceof OFMessage) {
    		OFMessage ofm = (OFMessage)msg;
    		ofm.writeTo(buf);
    		return buf;
    	} else
    		return msg;
    }
}
