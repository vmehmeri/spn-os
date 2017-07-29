package spn.os;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Interface for a callback object for the Server
 * 
 * @author Cong Chen <Cong.Chen@us.fujitsu.com>
 *
 */
public interface EventHandler
{
    void handleEvent(SelectionKey key) throws IOException;
}