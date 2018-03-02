import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Kristoffer on 3/1/2018.
 */

 public class MessageHandlerThread implements Runnable
 {
   // thread variables
   Socket msgSock;
   P2PChat theUser;
   ObjectInputStream msgTunnel;
   ObjectOutputStream addressTunnel;

   // constructor
   public MessageHandlerThread(Socket aSocket, P2PChat aUser)
   {
     msgSock = aSocket;
     theUser = aUser;
     msgTunnel = null;
     addressTunnel = null;
   }

   public void run()
   {
     try
     {
       //System.out.println("Handle message.");
       // create object reader for socket
       msgTunnel = new ObjectInputStream(msgSock.getInputStream());
       // read information from the socket
       Object raw = msgTunnel.readObject();
       //System.out.println("Received: " + raw.getClass());
       // check for raw data type, then cast
       if (raw.getClass() == Message.class) // normal message
       {
         if (theUser.connected)
         {
           Message msg = (Message)raw;
           // print the message to screen
           System.out.println(msg.contents);
         }
       }
       else if (raw.getClass() == Hashtable.class) // connection addresses
       {
         Hashtable<String,Address> newAddresses = (Hashtable<String,Address>)raw;
         //System.out.println(newAddresses);
         if (theUser.connected) // add connecting user and send back new list
         {
           // since you can only be connected to by unconnected users,
           // remove your own address to isolate the sender's
           newAddresses.remove(theUser.ownName);
           // iterate through the received addresses (one)
           Set<String> keys = newAddresses.keySet();
           Socket callbackSock = null;
           for(String key : keys)
           {
             // add new address
             Address newAddress = newAddresses.get(key);
             P2PChat.addresses.putIfAbsent(key, newAddress);
             // create a socket from that address back to sender
             callbackSock = new Socket(newAddress.ip,newAddress.port);
           }
           // now send updated list back
           addressTunnel = new ObjectOutputStream(callbackSock.getOutputStream());
           addressTunnel.writeObject(P2PChat.addresses);
           // close the writer
           addressTunnel.flush();
           addressTunnel.close();
         }
         else // set own list to received list, and set connected flag
         {
           P2PChat.addresses = newAddresses;
           //theUser.connected = true;
           System.out.println("Connected to.");
         }
       }
       else if (raw.getClass() == Address.class) // disconnect address
       {
         // remove the address from this user's address table
         Address removed = (Address)raw;
         P2PChat.addresses.remove(removed.userName);
       }
     }
     catch (ClassNotFoundException c)
     {
       System.out.println("Glitch in the matrix.");
       System.exit(-1);
     } catch (IOException e) {
       System.out.println("Failed to create connection.");
       System.exit(-1);
     }
   }
 }
