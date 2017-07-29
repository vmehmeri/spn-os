package spn.os;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFMessageReader;
import org.projectfloodlight.openflow.protocol.OFVersion;

public class OFMessageDecoder extends FrameDecoder {

    private OFMessageReader<OFMessage> reader;

    public OFMessageDecoder() {
        reader = OFFactories.getGenericReader();
    }

    public OFMessageDecoder(OFVersion version) {
        setVersion(version);
    }

    public void setVersion(OFVersion version) {
        OFFactory factory = OFFactories.getFactory(version);
        this.reader = factory.getReader();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer) throws Exception {
        if (!channel.isConnected()) {
            return null;
        }

        List<OFMessage> messageList = new ArrayList<OFMessage>();
        while(true) {
            OFMessage message = reader.readFrom(buffer);
            if (message == null)
                break;
            messageList.add(message);
        }
        return messageList.isEmpty() ? null : messageList;
    }

    @Override
    protected Object decodeLast(ChannelHandlerContext ctx, Channel channel,
                            ChannelBuffer buffer) throws Exception {
        return null;
    }
}