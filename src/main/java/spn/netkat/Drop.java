package spn.netkat;

public class Drop implements Policy {
    public Drop () {}

    public String toString() {
        return " { \"type\" : \"filter\", \"pred\" : { \"type\" : \"false\" } }";
    }
}
