package spn.os.element;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spn.exception.InvalidConfigurationException;
import spn.exception.InvalidHopException;

public class OpticalPath {
    protected Location packetIn;
    protected Location packetOut;
    protected OpticalLocation ingress;
    protected OpticalLocation egress;
    protected List<Link> links;
    protected Short wavelength;
    protected boolean bidirectional;
    private Vertex last;

    public OpticalPath() {
        this.links = new ArrayList<Link>();
        this.wavelength = null;
        this.last = null;
        this.bidirectional = false;
    }

    public OpticalPath(Short wavelength) {
        this.links = new ArrayList<Link>();
        this.wavelength = wavelength;
        this.last = null;
        this.bidirectional = false;
    }

    public OpticalPath(int size) {
        this.links = new ArrayList<Link>(size);
        this.wavelength = null;
        this.last = null;
        this.bidirectional = false;
    }

    public OpticalPath(Short wavelength, int size, boolean dir) {
        this.links = new ArrayList<Link>(size);
        this.wavelength = wavelength;
        this.last = null;
        this.bidirectional = dir;
    }

    public Short getWavelength() {
        return this.wavelength;
    }

    public void setWavelength(Short wavelength) {
        this.wavelength = wavelength;
    }

    public void setPacketIn(Location loc) {
        this.packetIn = loc;
    }

    public Location getPacketIn() {
        return this.packetIn;
    }

    public void setPacketOut(Location loc) {
        this.packetOut = loc;
    }

    public Location getPacketOut() {
        return this.packetOut;
    }

    public List<Link> getLinks() {
        return this.links;
    }

    public OpticalLocation getStart() {
        return this.ingress;
    }

    public OpticalLocation getStop() {
        return this.egress;
    }

    public int size() {
        return this.links.size();
    }

    public boolean isBidirectional() {
        return this.bidirectional;
    }

    public void start(OpticalLocation loc) {
        this.ingress = loc;
        this.last = loc.getVertex();
    }

    public void stop(OpticalLocation loc) throws InvalidHopException {
        if (!(this.last.equals(loc.getVertex())))
            throw new InvalidHopException(
                "Stop location not same vertex as destination of last hop");
        this.egress = loc;
    }

    public void add(Link link) throws InvalidHopException {
        if (this.last == null) {
            throw new InvalidHopException(
                "OpticalPath must be started with a specific OpticalLocation");
        }

        if (!(this.last.equals(link.getSrcVertex())))
            throw new InvalidHopException(
                "Previous destination " + this.last.getName()
                + " not source of link " + link.toString());

        this.links.add(link);
        this.last = link.getDstVertex();
    }

    public static OpticalPath fromJson(JsonObject json, PhysicalNetwork network)
        throws InvalidConfigurationException, InvalidHopException {
        if (json == null || json.isJsonNull())
            throw new InvalidConfigurationException(
                "No definition for this optical path. Json: " + json);

        if (!json.has("start") || !json.has("startPort"))
            throw new InvalidConfigurationException(
                "Incomplete start location for this optical path. Json: " + json);
        Location packetIn = network.getLocation(
                new BigInteger(json.get("start").getAsString()),
                                                json.get("startPort").getAsInt());

        if (!json.has("stop") || !json.has("stopPort"))
            throw new InvalidConfigurationException(
                "Incomplete stop location for this optical path. Json: " + json);
        Location packetOut = network.getLocation(
                new BigInteger(json.get("stop").getAsString()),
                                                 json.get("stopPort").getAsInt());

        if (!json.has("wavelength"))
            throw new InvalidConfigurationException(
                "No wavelength for this optical path. Json: " + json);
        Short wavelength = json.get("wavelength").getAsShort();

        boolean direction;
        if (json.has("bidirectional"))
            direction = json.get("bidirectional").getAsBoolean();
        else
            direction = false;

        if (!json.has("path"))
            throw new InvalidConfigurationException(
                "No path for this optical path. Json: " + json);
        JsonArray links;
        try { links = json.get("path").getAsJsonArray(); }
        catch (IllegalStateException e) { throw new InvalidConfigurationException(
                "Path not a JSON Array for this optical path. Json: " + json); }

        int size = links.size();
        OpticalPath path = new OpticalPath(wavelength, size, direction);
        path.setPacketIn(packetIn);
        path.setPacketOut(packetOut);

        for (int i=0; i < size; i++) {
            JsonObject object;
            try { object = links.get(i).getAsJsonObject(); }
            catch (IllegalStateException e) { throw new InvalidConfigurationException(
                    "Path elements not JSON objects for this optical path. Json: " + json); }
            if (!object.has("src") || !object.has("srcPort"))
                throw new InvalidConfigurationException("Incomplete source. Json: " + object);
            if (!object.has("dst") || !object.has("dstPort"))
                throw new InvalidConfigurationException("Incomplete destination. Json: " + object);

            if (i == 0) {
                OpticalLocation start = network.getOpticalLocation(
                    new BigInteger(object.get("dst").getAsString()),
                    object.get("dstPort").getAsInt(),
                    wavelength);
                if (start == null)
                    throw new InvalidConfigurationException(
                        "Destination not present in network: " + object.toString());
                path.start(start);
                continue;
            }

            if (i == size-1) {
                OpticalLocation stop = network.getOpticalLocation(
                		new BigInteger(object.get("src").getAsString()),
                    object.get("srcPort").getAsInt(),
                    wavelength);
                if (stop == null)
                    throw new InvalidConfigurationException(
                        "Source not present in network: " + object.toString());
                path.stop(stop);
                continue;
            }

            Link link = network.getLink(
                    new BigInteger(object.get("src").getAsString()),
                                        object.get("srcPort").getAsInt(),
                                        new BigInteger(object.get("dst").getAsString()),
                                        object.get("dstPort").getAsInt());
            if (link == null)
                throw new InvalidConfigurationException(
                    "No physical link for given Json: " + object);
            try { path.add(link); }
            catch (InvalidHopException e) {
                throw new InvalidConfigurationException(
                    "Invalid optical link: " + e.toString());
            }
        }

        return path;
    }

    public OpticalPath getReverse() throws InvalidHopException {
        int linkId = 0;
        OpticalPath reverse = new OpticalPath(this.wavelength, this.links.size(),
                                              this.bidirectional);
        reverse.setPacketIn(this.packetOut);
        reverse.setPacketOut(this.packetIn);
        reverse.start(this.egress);
        for (int i=this.links.size()-1; i>=0; i--) {
            Link rev;
            try {
                rev = links.get(i).reverse(BigInteger.valueOf(linkId++));
                reverse.add(rev);
            } catch (InstantiationException | IllegalAccessException
                     | IllegalArgumentException | InvocationTargetException e) {
                // The link reversal uses reflection which can cause all sorts
                // of errors
                System.out.println("Unable to reverse link");
                e.printStackTrace();
            }
        }
        reverse.stop(this.ingress);
        return reverse;
    }
}
