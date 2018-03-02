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
           //System.out.println(P2PChat.addresses);
           Message msg = (Message)raw;
           // print the message to screen
           System.out.println(msg.contents);
         }
       }
       else if (raw.getClass() == Address.class) // connection request
       {
         Address newAddress = (Address)raw;
         P2PChat.addresses.putIfAbsent(newAddress.userName, newAddress);
         // send back addresses
         //System.out.println(P2PChat.addresses);
         Set<String> keys = P2PChat.addresses.keySet();
         for (String key : keys)
         {
           Address updateAddress = P2PChat.addresses.get(key);
           Socket cbSock = new Socket(updateAddress.ip, updateAddress.port);
           addressTunnel = new ObjectOutputStream(cbSock.getOutputStream());
           addressTunnel.writeObject(P2PChat.addresses);
           addressTunnel.flush();
           addressTunnel.close();
         }
       }
       else if (raw.getClass() == Hashtable.class) // connection callback
       {
         Hashtable<String,Address> newAddresses = (Hashtable<String,Address>)raw;
         //System.out.println(newAddresses);
         // add new addresses to your own
         Set<String> keys = newAddresses.keySet();
         for (String key : keys)
         {
           Address newAddress = newAddresses.get(key);
           P2PChat.addresses.putIfAbsent(key, newAddress);
         }
         System.out.println("A user has connected.");
       }
       else if (raw.getClass() == Disconnect.class) // disconnect address
       {
         // remove the address from this user's address table
         Disconnect dc = (Disconnect)raw;
         Address removed = (Address)dc.disconnector;
         P2PChat.addresses.remove(removed.userName);
         System.out.println("A user has disconnected.");
       }
     }
     catch (ClassNotFoundException c)
     {
       System.out.println("Glitch in the matrix.");
       System.exit(-1);
     } catch (IOException e) {
       // do nothing
     }
   }
 }
