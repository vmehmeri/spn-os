package spn.os.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFFeaturesRequest;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFGroupDelete;
import org.projectfloodlight.openflow.protocol.OFGroupType;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.Match.Builder;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.CircuitSignalID;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFGroup;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.VlanVid;
import spn.exception.InvalidActionException;
import spn.exception.SwitchConnectionException;
import spn.netkat.WebKAT;
import spn.os.OFMessageDecoder;
import spn.os.OFMessageEncoder;
import spn.os.OFSwitch;
import spn.os.element.Switch;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


public class DefaultController extends Controller  implements Runnable  {

    private Handler handler;

    /**
     * Constructor for default IP Address and Port (localhost:6633)
     * @param wk WebKAT instance
     */
    public DefaultController(WebKAT wk) {
        super(wk);
        this.handler = new Handler();
    }

    class Handler extends SimpleChannelHandler {

        private Map<Channel, OFFactory> factories;
        protected OFFactory factory13;
        public Handler() {
            super();
            this.factory13 = OFFactories.getFactory(OFVersion.OF_13);
            this.factories = new HashMap<Channel, OFFactory>();
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx,
                                     ChannelStateEvent event) {
            Channel ch = event.getChannel();
            sendHello(ch);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx,
                                    MessageEvent event) {
            if (event.getMessage() instanceof List) {
                @SuppressWarnings("unchecked")
                List<OFMessage> messages = (List<OFMessage>)event.getMessage();
                for (OFMessage msg : messages) {
                    processMessage(msg, event.getChannel());
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws SwitchConnectionException {
            //System.err.println("Lost connection, closing channel...");
            //ctx.getChannel().close();
            return;

        }

        private void processMessage(OFMessage message, Channel channel) {
            switch(message.getType()) {
                case HELLO:
                    processHello((OFHello)message, channel);
                    break;
                case FEATURES_REPLY:
                    processFeaturesReply((OFFeaturesReply)message, channel);
                    break;
                default:
                    break;
            }
        }

        private void processHello(OFHello hello, Channel channel) {
            // Currently only use the switch's version, don't look at bitmaps
            // for now
            OFFactory factory = OFFactories.getFactory(hello.getVersion());
            this.factories.put(channel, factory);
            sendFeaturesRequest(channel);
        }

        private void processFeaturesReply(OFFeaturesReply reply, Channel channel) {
            DatapathId dpid = reply.getDatapathId();
            OFFactory factory = factories.get(channel);
            OFSwitch sw = new OFSwitch(dpid, channel, factory);
            String swDpId = BigInteger.valueOf(dpid.getLong()).toString();
            switches.put(swDpId, sw);
            switches.put(dpid.toString(), sw);
        }

        private void sendHello(Channel channel) {
            OFHello hello = factory13.buildHello().build();
            channel.write(hello);
        }

        private void sendFeaturesRequest(Channel channel) {
            OFFactory factory = this.factories.get(channel);
            OFFeaturesRequest request = factory.buildFeaturesRequest().build();
            channel.write(request);
        }

        private void sendDeleteAll(OFSwitch sw) {
            try {
            /*
             * No tables for OF1.0, so omit that field for flow deletion.
             */
                if (sw.getFactory().getVersion().compareTo(OFVersion.OF_10) == 0) {
                    OFFlowDelete deleteFlows = sw.getFactory().buildFlowDelete()
                            .build();
                    sw.getChannel().write(Collections.singletonList(deleteFlows));
                } else { /* All other OFVersions support multiple tables and groups. */
                    OFFlowDelete deleteFlows = sw.getFactory().buildFlowDelete()
                            .setTableId(TableId.ALL)
                            .build();
                    sw.getChannel().write(Collections.singletonList(deleteFlows));

                /*
                 * Clear all groups.
                 * We have to do this for all types manually as of Loxi 0.9.0.apping
                 */
                    OFGroupDelete delgroup = sw.getFactory().buildGroupDelete()
                            .setGroup(OFGroup.ALL)
                            .setGroupType(OFGroupType.ALL)
                            .build();
                    sw.getChannel().write(delgroup);
                    delgroup.createBuilder()
                            .setGroupType(OFGroupType.FF)
                            .build();
                    sw.getChannel().write(delgroup);
                    delgroup.createBuilder()
                            .setGroupType(OFGroupType.INDIRECT)
                            .build();
                    sw.getChannel().write(delgroup);
                    delgroup.createBuilder()
                            .setGroupType(OFGroupType.SELECT)
                            .build();
                    sw.getChannel().write(delgroup);
                }
            } catch (RuntimeException e) {
                // TODO(basus)
                // This is a bit of a hack. Getting here means that the switch has disconnected.
                // This code assumes that the switch has reconnected already and there is a new
                // switch object registered for the datapath id, but this might not always be
                // the case. Ideally, we should retry a finite number of times (in a separate
                // thread?) and then signal an error.
                if (sw != null && switches != null) {
                    OFSwitch ofSwitch;
                    ofSwitch = switches.get(BigInteger.valueOf(sw.getDatapathId()).toString());
                    if (ofSwitch == null) {
                        ofSwitch = switches.get(String.format("00:00:00:00:00:00:00:%02d", sw.getDatapathId()));
                    }
                    handler.sendDeleteAll(ofSwitch);
                }
            }
        }

        public void refresh(OFSwitch sw, JsonArray flowTable) throws SwitchConnectionException {
            //sendDeleteAll(sw);
            if (flowTable == null) {
                console.error("Failed to obtain flowTable from the NetKAT server");
                console.error("Please check if server is running");
                return;
            }

            if (sw == null) {
                console.error("SW is null");

            }

//            console.debug("--> Pushing flowtable to switch " + sw.getDatapathId());
//            console.json(flowTable.toString());

            for (JsonElement flowEntry : flowTable) {
                try {
                    OFFlowAdd flowMod = flowModFromJson(flowEntry, sw.getFactory());
                    sw.getChannel().write(Collections.singletonList(flowMod));
                } catch (InvalidActionException e) {
                    console.error("Invalid flow entry : " + e.toString());
                } catch (RuntimeException e) {
                    // TODO(basus)
                    // This is a bit of a hack. Getting here means that the switch has disconnected.
                    // This code assumes that the switch has reconnected already and there is a new
                    // switch object registered for the datapath id, but this might not always be
                    // the case. Ideally, we should retry a finite number of times (in a separate
                    // thread?) and then signal an error.
//                    console.error("Lost switch connection. Try again");
                    //OFSwitch ofSwitch = switches.get(BigInteger.valueOf(sw.getDatapathId()).toString());
                    //handler.refresh(sw, flowTable);
                }
            }
        }

        private OFFlowAdd flowModFromJson(JsonElement flowEntry, OFFactory factory)
                throws InvalidActionException {
            Short priority = flowEntry.getAsJsonObject().get("priority").getAsShort();
            JsonObject pattern = flowEntry.getAsJsonObject().get("pattern").getAsJsonObject();
            JsonArray actionArray = flowEntry.getAsJsonObject().get("action").getAsJsonArray();

            Match match = matchFromJson(pattern, factory);
            List<OFAction> actionList = actionsFromJson(actionArray, factory);
            return factory.buildFlowAdd()
                    .setBufferId(OFBufferId.NO_BUFFER)
                    .setHardTimeout(3600)
                    .setPriority(priority)
                    .setMatch(match)
                    .setActions(actionList)
                    .build();

        }

        private List<OFAction> actionsFromJson(JsonArray actionArray, OFFactory factory)
                throws InvalidActionException {
            ArrayList<OFAction> actionList = new ArrayList<OFAction>();
            OFActions actions = factory.actions();

            for (JsonElement actionEntry : actionArray) {
                for (JsonElement actionEntry2 : actionEntry.getAsJsonArray()) {
                    JsonArray action = actionEntry2.getAsJsonArray();
                    String tag = action.get(0).getAsString();

                    if (tag.equalsIgnoreCase("Output")) {
                        Short outport;
                        JsonObject output = action.get(1).getAsJsonObject();
                        String outputType = output.get("type").getAsString();

                        if (outputType.equals("inport"))
                            outport = 0xfffffff8;
                        else if (outputType.equals("physical"))
                            outport = output.get("port").getAsShort();
                        else
                            throw new InvalidActionException(
                                    "Invalid Action : " + tag);

                        OFActionOutput outputAction = actions.buildOutput()
                                .setMaxLen(0)
                                .setPort(OFPort.ofShort(outport))
                                .build();
                        actionList.add(outputAction);
                    } else if (tag.equalsIgnoreCase("Modify")) {
                        JsonArray mod = action.get(1).getAsJsonArray();
                        String modType = mod.get(0).getAsString();

                        if (modType.equals("SetVlan")) {
                            Short vlan = mod.get(1).getAsShort();
                            // VLAN 65535 as a short wraps around to -1
                            if (vlan == -1) {
                                // TODO(basus): this should be strip for OF1.0 and pop for 1.3
                                actionList.add(actions.stripVlan());
                                //actionList.add(actions.popVlan());
                            } else
                                actionList.add(actions.setVlanVid(VlanVid.ofVlan(vlan)));
                        } else if (modType.equals("SetWavelength")) {
                            Short lambda = mod.get(1).getAsShort();
                            CircuitSignalID sigid = new CircuitSignalID(
                                    (byte) 1, (byte) 2, (short) lambda, (short) 1);
                            OFAction opticalAction = actions.circuit(
                                    factory.oxms().ochSigidBasic(sigid));
                            actionList.add(opticalAction);
                        } else
                            throw new InvalidActionException(
                                    "Invalid Modification : " + modType);
                    }
                }
            }

            return actionList;
        }

        private Match matchFromJson(JsonObject pattern, OFFactory factory) {
            Builder mb = factory.buildMatch();
            if (!pattern.get("inPort").isJsonNull()) {
                Short inPort = pattern.get("inPort").getAsShort();
                mb = mb.setExact(MatchField.IN_PORT,
                                 OFPort.ofShort(inPort));
            }

            if (!pattern.get("dlTyp").isJsonNull()) {
                mb = mb.setExact(MatchField.ETH_TYPE,
                                 EthType.of(pattern.get("dlTyp").getAsInt()));
            }

            if (!pattern.get("nwSrc").isJsonNull()) {
                mb = mb.setExact(MatchField.IPV4_SRC,
                                 IPv4Address.of(pattern.get("nwSrc").getAsString()));
            }

            if (!pattern.get("nwDst").isJsonNull()) {
                mb = mb.setExact(MatchField.IPV4_DST,
                                 IPv4Address.of(pattern.get("nwDst").getAsString()));
            }

            if (!pattern.get("dlVlan").isJsonNull()) {
                mb = mb.setExact(MatchField.VLAN_VID,
                                 OFVlanVidMatch.ofRawVid(pattern.get("dlVlan").getAsShort()));
            }

            if (pattern.has("wavelength") && !pattern.get("wavelength").isJsonNull()) {
                Short lambda = pattern.get("wavelength").getAsShort();
                CircuitSignalID sigid = new CircuitSignalID((byte) 1, (byte) 2,
                                                            lambda, (short) 1);
                mb = mb.setExact(MatchField.OCH_SIGID, sigid);
            }

            return mb.build();

        }
    }

    public void run() {
//        console.info("Bootstrapping controller server");

        // Netty boilerplate
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("Decoder",
                                 new OFMessageDecoder());
                pipeline.addLast("Encoder",
                                 new OFMessageEncoder());
                pipeline.addLast("handler",
                                 handler);
                return pipeline;
            }
        });

        bootstrap.setOption("reuseAddr", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        try {
            bootstrap.bind(new InetSocketAddress(port));
        } catch (ChannelException e) {
            console.error("Unable to bind to socket: ",e.getMessage());
        }
    }

    /*public void refreshAll() throws WebKATException {
        for (OFSwitch sw : this.switches.values()) {
            JsonArray flowTable = webKAT.getFlowTable(sw.getDatapathId());
            handler.refresh(sw, flowTable);
        }
    }*/


   /* public void refresh(BigInteger id, JsonArray flowTable) {
        OFSwitch sw = this.switches.get(id);
        handler.refresh(sw, flowTable);
    }*/

    /*public void refresh(PhysicalNetwork network) throws WebKATException {
        for(Switch sw : network.getSwitches()) {
            OFSwitch ofSwitch = this.switches.get(sw.getId());
            JsonArray flowTable = webKAT.getFlowTable(ofSwitch.getDatapathId());
            handler.refresh(ofSwitch, flowTable);
        }
    }*/

    public void refresh(Iterable<? extends Switch> switches) throws SwitchConnectionException  {
        if (switches == null) {
            console.error("Received a null list of switches");
        }
        for (Switch sw : switches) {
            OFSwitch ofSwitch;
            ofSwitch = this.switches.get(sw.getId().toString());

            if (ofSwitch == null) {
                ofSwitch = this.switches.get(String.format("00:00:00:00:00:00:00:%02d", sw.getId().longValue()));
            }

            if (ofSwitch == null) {
                console.error("Switch not found. The controller might have lost connection to the switch");
                return;
            }

            JsonArray flowTable = sw.getFlowTable();
            console.debug(">> Refreshing switch " + sw.getId());
            handler.refresh(ofSwitch, flowTable);
        }
    }

    public void clearFlowtable(Iterable<? extends Switch> switches) {
        for (Switch sw : switches) {
            OFSwitch ofSwitch = this.switches.get(sw.getId().toString());
            if (ofSwitch == null) {
                ofSwitch = this.switches.get(String.format("00:00:00:00:00:00:00:%02d", sw.getId().longValue()));
            }
//            console.debug(">> Clearing switch " + sw.getId());
            handler.sendDeleteAll(ofSwitch);
        }
    }

}
