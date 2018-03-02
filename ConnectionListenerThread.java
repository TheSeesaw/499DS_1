import java.io.*;
import java.net.*;

/**
 * Created by Kristoffer Schindele on 3/1/2018.
 */
public class ConnectionListenerThread implements Runnable
{
    // thread variables
    P2PChat theUser;

    // constructor
    public ConnectionListenerThread(P2PChat aUser)
    {
      theUser = aUser;
    }

    public void run()
    {
        while(true)
        {
            try
            {
              // create a new handler thread to process message
              MessageHandlerThread handler = new MessageHandlerThread(P2PChat.servSock.accept(), theUser);
              Thread mThread = new Thread(handler, "Handler Thread");
              mThread.start();
              //System.out.println("New message received");
              // continue listening for more connections
              // disconnect if all other connections are terminated
              mThread.join();
            } catch (InterruptedException ie)
            {
              System.out.println("Failed to join thread.");
              System.exit(-1);
            } catch (IOException e) {
              System.out.println("Failed to accept connection or server was closed.");
              System.exit(-1);
            }
        }
    }
}
