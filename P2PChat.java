import java.util.Scanner;
import java.util.Hashtable;
import java.util.Set;
import java.io.*;
import java.net.*;

/**
 * Created by Kristoffer Schindele 3/1/2018.
 *
 */

 public class P2PChat
 {
   public static ServerSocket servSock;
   public static Scanner consoleIn;
   public static Hashtable<String,Address> addresses;
   public String ownName;
   public String ownIP;
   public int ownPort;
   public String friendName;
   public String friendIP;
   public int friendPort;
   public boolean connected;
   public boolean active;

   public P2PChat(String aName,
                  String anIP,
                  int aPort,
                  String knownName,
                  String knownIP,
                  int knownPort)
   {
     ownName = aName;
     ownIP = anIP;
     ownPort = aPort;
     friendName = knownName;
     friendIP = knownIP;
     friendPort = knownPort;
     servSock = null;
     consoleIn = null;
     addresses = new Hashtable<String,Address>();
     connected = false;
     active = true;
   }

   // main method
   public static void main(String args[]) throws Exception
   {
     // process command line args
     P2PChat user = new P2PChat(args[0],
                               args[1],
                               Integer.parseInt(args[2]),
                               args[3],
                               args[4],
                               Integer.parseInt(args[5]));
     // start server to receive messages
     try
     {
       System.out.println("Starting server . . .");
       // create a server socket
       P2PChat.servSock = new ServerSocket(user.ownPort);
       // listen for connections
       ConnectionListenerThread connectThread = new ConnectionListenerThread(user);
       Thread sThread = new Thread(connectThread, "Connection Thread");
       sThread.start();
       // server is now running and listening for connections
       System.out.println("Server started.");
     }
     catch (IOException e)
     {
         System.out.println("Unable to start server listening on port: " + Integer.toString(user.ownPort));
         System.exit(-1);
     }
     // server is now running, start listening for user input
     P2PChat.consoleIn = new Scanner(System.in);
     String userInput = null;
     while(user.active)
     {
       // handleUserInput returns false if the user input the 'quit' command
       user.active = user.handleUserInput();
     }
     // close the socket and scanner
     try
     {
       P2PChat.servSock.close();
       P2PChat.consoleIn.close();
     }
     catch (IOException e)
     {
       System.out.println("Unable to close server socket.");
       System.exit(-1);
     }
     // exit the program gracefully
     System.out.println("Now Exiting . . .");
     System.exit(0);
   }

   // handleUserInput processes input from console, and passes information to
   // messageDispatch
   // Returns false if the user input the
   public boolean handleUserInput()
   {
     // get the next line of input
     String input = P2PChat.consoleIn.nextLine();
     //System.out.println("Status: " + this.connected);
     // check for exit command
     if (input.equals("quit"))
     {
       return false; // exit case
     }
     // check if user is connected
     else if (this.connected)
     {
       // message is either text, or disconnect
       if (input.equals("disconnect"))
       {
         // disconnect this user
         this.disconnect();
       }
       else // plain message
       {
         String payload = this.ownName + ": " + input;
         this.message(payload);
       }
     }
     // check for connection protocol
     else if (input.equals("connect"))
     {
       this.connect();
       // this user is now connected
       this.connected = true;
     }
     else
     {
       System.out.println("Not connected, please type 'connect' before sending messages.");
     }
     return true;
   }

   // connects user to knownUser
   public void connect()
   {
     // initialize address list with self and known user
     //System.out.println(this.ownName + this.ownIP);
     Address ownAddress = new Address(this.ownName, this.ownIP, this.ownPort);
     P2PChat.addresses.put(this.ownName, ownAddress);
     //System.out.println(this.friendName + this.friendIP);
     //P2PChat.addresses.put(this.friendName, new Address(this.friendName, this.friendIP, this.friendPort));
     try
     {
       // create a socket with known user
       Socket tempSock = new Socket(this.friendIP, this.friendPort);
       // send addresses to known user
       ObjectOutputStream addressTunnel = new ObjectOutputStream(tempSock.getOutputStream());
       addressTunnel.writeObject(ownAddress);
       // close the writer
       addressTunnel.flush();
       addressTunnel.close();
       System.out.println("Successfully connected.");
     }
     catch (IOException e)
     {
       System.out.println("Unable to send addresses.");
     }
   }

   // disconnects user from connected peers
   public void disconnect()
   {
     if (this.connected)
     {
       // first, remove own address from list
       P2PChat.addresses.remove(this.ownName);
       // send own address to all connected users
       Set<String> keys = P2PChat.addresses.keySet();
       for(String key : keys)
       {
         try
         {
           // create a socket for this address
           String tempIP = P2PChat.addresses.get(key).ip;
           int tempPort = P2PChat.addresses.get(key).port;
           Socket tempSock = new Socket(tempIP, tempPort);
           Address ownAddress = new Address(this.ownName, this.ownIP, this.ownPort);
           Disconnect dcCall = new Disconnect(ownAddress);
           // send address through socket
           ObjectOutputStream disconnectTunnel = new ObjectOutputStream(tempSock.getOutputStream());
           disconnectTunnel.writeObject(dcCall);
           // user is now disconnected
           this.connected = false;
           disconnectTunnel.flush();
           disconnectTunnel.close();
           System.out.println("Disconnected.");
         }
         catch(IOException e)
         {
           System.out.println("Unable to send disconnect.");
         }
       }
     }
     else // not connected, do nothing
     {
       System.out.println("Cannot disconnect if not connected.");
       return;
     }
   }

   // sends text message to all connected peers
   public void message(String payload)
   {
     if(this.connected)
     {
       // send own address to all connected users
       Set<String> keys = P2PChat.addresses.keySet();
       for(String key : keys)
       {
         try
         {
           // create a socket for this address
           String tempIP = P2PChat.addresses.get(key).ip;
           int tempPort = P2PChat.addresses.get(key).port;
           Socket tempSock = new Socket(tempIP, tempPort);
           ObjectOutputStream msgTunnel = new ObjectOutputStream(tempSock.getOutputStream());
           Message out = new Message(payload);
           msgTunnel.writeObject(out);
           msgTunnel.close();
         }
         catch(IOException e)
         {
           System.out.println("Unable to send message.");
         }
       }
     }
     else // not connected, do nothing
     {
       System.out.println("Cannot send message if not connected.");
       return;
     }
   }
// end of class definition
}
