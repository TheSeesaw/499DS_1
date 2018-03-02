import java.io.Serializable;

/**
 * Created by Kristoffer Schindele 3/1/2018.
 *
 */
 
public class Address implements Serializable
{
  public String userName;
  public String ip;
  public int port;

  public Address(String aName, String anIP, int aPort)
  {
    userName = aName;
    ip = anIP;
    port = aPort;
  }
}
