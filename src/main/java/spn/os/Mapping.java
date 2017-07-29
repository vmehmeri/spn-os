package spn.os;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import spn.exception.InvalidConfigurationException;
import spn.os.element.*;

public class Mapping {

    protected HashMap<Location, Location> map;

    
    public Mapping() {
        this.map = new HashMap<Location,Location>();
    }

    public static Mapping fromJson(JsonElement json, VirtualNetwork virt,
                                   PhysicalNetwork phys) throws InvalidConfigurationException {
        Console console = Console.getConsole(new Mapping());
        Mapping mapping = new Mapping();
        JsonArray mappings;
//        Set<Port> physicalPortsInUse = new HashSet<Port>();
        
        try {
            mappings = json.getAsJsonArray();
        } catch (Exception e) {
            console.info("Could not get mapping from JSON:" + json.toString());
            return null;
        }
        for (JsonElement e : mappings) {
            try {
                JsonObject o   = e.getAsJsonObject();
                int from       = o.get("from").getAsInt();
                int to         = o.get("to").getAsInt();
                int fromPort   = o.get("fromPort").getAsInt();
                int toPort     = o.get("toPort").getAsInt();
                console.info(String.format("vsw%d : vpt%d -> sw%d : pt%d", from, fromPort, to, toPort));
                Vertex fromV   = virt.getVertex(BigInteger.valueOf(from));
                Vertex toV     = phys.getVertex(BigInteger.valueOf(to));
                Port fromP     = fromV.getPort(fromPort); //virtual ports
                Port toP       = toV.getPort(toPort); //physical ports
//                physicalPortsInUse.add(toP);
                mapping.put(new Location(fromV, fromP), new Location(toV, toP));

                	
            } catch (Exception ex) {
                String err = String.format("Could not parse mapping %s : %s", e.toString(), ex.getMessage());
                throw new InvalidConfigurationException(err);
            }
        }

        return mapping;
   }

    public Location put(Location from, Location to) {
        return map.put(from, to);
    }
    
    public int size() {
        return this.map.size();
    }

    public String toNetKAT() {
        StringBuffer result = new StringBuffer();
        int i = 0;
        for (Map.Entry<Location, Location> e : this.map.entrySet()) {
            Location from = e.getKey();
            Location to = e.getValue();
            result.append("((vswitch=");
            result.append( from.getVertex().getId().toString() );
            result.append(" and vport=");
            result.append(from.getPort().getNumber());
            result.append(") and (switch=");
            result.append( to.getVertex().getId().toString() );
            result.append(" and port=");
            result.append(to.getPort().getNumber() );
            result.append("))");
            if (i < this.map.size()-1)
                result.append(" or \n");
            i++;
        }
        return result.toString();
    }
}
