import java.io.Serializable;

/**
 * Created by Kristoffer Schindele 3/1/2018.
 *
 */

public class Disconnect implements Serializable
{
  public Address disconnector;

  public Disconnect(Address aDisconnector)
  {
    disconnector = aDisconnector;
  }
}
