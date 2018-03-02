import java.io.Serializable;

/**
 * Created by Kristoffer Schindele 3/1/2018.
 *
 */

public class Message implements Serializable
{
  public String contents;

  public Message(String someContents)
  {
    contents = someContents;
  }
}
