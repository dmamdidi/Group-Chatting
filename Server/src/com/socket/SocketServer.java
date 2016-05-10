package com.socket;

import java.io.*;
import java.net.*;

class ServerThread extends Thread { 
	
    public SocketServer server = null;
    public Socket socket = null;
    public int ID = -1;
    public String username = "";
    public ObjectInputStream streamIn  =  null;
    public ObjectOutputStream streamOut = null;
    public ServerFrame ui;

    public ServerThread(SocketServer server_1, Socket socket_1){  
    	super();
        server = server_1;
        socket = socket_1;
        ID     = socket.getPort();
        ui = server_1.ui;
    }
    
    public void send(Message msg){
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } 
        catch (IOException ex) {
            System.out.println("Exception [SocketClient : send(...)]");
        }
    }
    //Returns the ID for thread
    public int getID(){  
	    return ID;
    }
   
  //  @SuppressWarnings("deprecation")
	public void run(){  
        //Showing the thread running status on the frame
    	ui.jTextArea1.append("\nServer Thread " + ID + " running.");
        while (true){  
    	    try{  
                Message msg = (Message) streamIn.readObject();
                // this read the incoming message and hand it to SocketServer
    	    	server.handle(ID, msg);
            }
            catch(Exception ioe){  
            	System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }
    
    public void open() throws IOException {  
        //cretes new objects for output and input stream
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        streamOut.flush();
        streamIn = new ObjectInputStream(socket.getInputStream());
    }
    
    public void close() throws IOException {  
    	if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null) streamOut.close();
    }
}





public class SocketServer implements Runnable {
    
    public ServerThread clients[];
    public ServerSocket server = null;
    public Thread       thread = null;
    public int clientCount = 0, port = 13000;
    public ServerFrame ui;
    public Database db;
    //Frame displays the message with the IP address and port number
    public SocketServer(ServerFrame frame){
        
        clients = new ServerThread[50];
        ui = frame;
        //contains the path of the xml file
        db = new Database(ui.filePath);
        
	try{  
	    server = new ServerSocket(port);
            port = server.getLocalPort();
            //displays the message with IP address where the server is running
	    ui.jTextArea1.append("Server started. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
	    start(); 
        }
	catch(IOException ioe){  
            ui.jTextArea1.append("Can not bind to port : " + port + "\nRetrying"); 
            //ui.RetryStart(0);
	}
    }
    
    public SocketServer(ServerFrame frame, int Port){
       
        clients = new ServerThread[50];
        ui = frame;
        port = Port;
        //The file uploaded will be assigned
        db = new Database(ui.filePath);
        
	try{  
	    server = new ServerSocket(port);
            port = server.getLocalPort();
            //displays the message with IP address where the server is running
	    ui.jTextArea1.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
	    start(); 
        }
	catch(IOException ioe){  
            //prints exceptions whenever the servers re-runs or not able to run
            ui.jTextArea1.append("\nCan not bind to port " + port + ": " + ioe.getMessage()); 
	}
    }
	
    public void run(){  
	while (thread != null){  
            try{  
                //server accepts the client request with differnet threads
		ui.jTextArea1.append("\nWaiting for a client ..."); 
	        addThread(server.accept()); 
	    }
	    catch(Exception ioe){ 
                ui.jTextArea1.append("\nServer accept error: \n");
                //ui.RetryStart(0);
	    }
        }
    }
	
    public void start(){  
    	if (thread == null){  
            thread = new Thread(this); 
	    thread.start();
	}
    }
    
   
    public void stop(){  
        if (thread != null){  
            thread.stop(); 
	    thread = null;
	}
    }
    //this returns the position of the client ID which matches the current ID passed
    private int findClient(int ID){  
    	for (int i = 0; i < clientCount; i++){
        	if (clients[i].getID() == ID){
                    return i;
                }
	}
	return -1;
    }
    //Synchronized guarantees that changes to the state of the object are visible to all threads
    // In SocketServer process the messages based on their type
    public synchronized void handle(int ID, Message msg){  
	if (msg.content.equals("bye")){
            Announce("signout", "SERVER", msg.sender);
            remove(ID); 
	}
        //msg will contain in this format {type='test', sender='SERVER', content='OK', recipient='testUser'}
	else{
            if(msg.type.equals("login")){
                
                if(findUserThread(msg.sender) == null){
                    //this calls the method from Database class which returns boolean value
                    if(db.checkLogin(msg.sender, msg.content)){
                        clients[findClient(ID)].username = msg.sender;
                        clients[findClient(ID)].send(new Message("login", "SERVER", "TRUE", msg.sender));
                        Announce("newuser", "SERVER", msg.sender);
                        SendUserList(msg.sender);
                    }
                    else{
                        //Finds thread of recipient and forwards content to him
                        clients[findClient(ID)].send(new Message("login", "SERVER", "FALSE", msg.sender));
                    } 
                }
                else{
                    clients[findClient(ID)].send(new Message("login", "SERVER", "FALSE", msg.sender));
                }
            }
            else if(msg.type.equals("message")){
                if(msg.recipient.equals("All")){
                    Announce("message", msg.sender, msg.content);
                }
                else{
                    findUserThread(msg.recipient).send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
                    clients[findClient(ID)].send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
                }
            }
            else if(msg.type.equals("test")){
                clients[findClient(ID)].send(new Message("test", "SERVER", "OK", msg.sender));
            }
            else if(msg.type.equals("signup")){
                if(findUserThread(msg.sender) == null){
                    //New user is added when the user is not present in DB
                    if(!db.userExists(msg.sender)){
                        db.addUser(msg.sender, msg.content);
                        clients[findClient(ID)].username = msg.sender;
                        clients[findClient(ID)].send(new Message("signup", "SERVER", "TRUE", msg.sender));
                        clients[findClient(ID)].send(new Message("login", "SERVER", "TRUE", msg.sender));
                        Announce("newuser", "SERVER", msg.sender);
                        SendUserList(msg.sender);
                    }
                    else{
                        clients[findClient(ID)].send(new Message("signup", "SERVER", "FALSE", msg.sender));
                    }
                }
                else{
                    clients[findClient(ID)].send(new Message("signup", "SERVER", "FALSE", msg.sender));
                }
            }
            
            else if(msg.type.equals("upload_req")){
                if(msg.recipient.equals("All")){
                    //clients[findClient(ID)].send(new Message("message", "SERVER", "Uploading to 'All' forbidden", msg.sender));
                    //sends file to all the users available in the server connection
                    Announce("upload_req", msg.sender, msg.content);
                }
                else{
                    //sends file to a specific user selected
                    findUserThread(msg.recipient).send(new Message("upload_req", msg.sender, msg.content, msg.recipient));
                }
            }
            
            else if(msg.type.equals("upload_res")){
                if(!msg.content.equals("NO")){
                    String IP = findUserThread(msg.sender).socket.getInetAddress().getHostAddress();
                    findUserThread(msg.recipient).send(new Message("upload_res", IP, msg.content, msg.recipient));
                }
                else{
                    findUserThread(msg.recipient).send(new Message("upload_res", msg.sender, msg.content, msg.recipient));
                }
            }
                    
	}
    }
    //This broadcasts the message to all the clients whoever logins
    public void Announce(String type, String sender, String content){
        Message msg = new Message(type, sender, content, "All");
        for(int i = 0; i < clientCount; i++){
            clients[i].send(msg);
        }
    }
    
    public void SendUserList(String toWhom){
        for(int i = 0; i < clientCount; i++){
            findUserThread(toWhom).send(new Message("newuser", "SERVER", clients[i].username, toWhom));
        }
    }
    
    public ServerThread findUserThread(String usr){
        for(int i = 0; i < clientCount; i++){
            if(clients[i].username.equals(usr)){
                return clients[i];
            }
        }
        return null;
    }
	
    
    //When ever the client closes the console, thread will be removed 
    public synchronized void remove(int ID){  
    int pos = findClient(ID);
        if (pos >= 0){  
            ServerThread toTerminate = clients[pos];
            ui.jTextArea1.append("\nRemoving client thread " + ID + " at " + pos);
	    if (pos < clientCount-1){
                for (int i = pos+1; i < clientCount; i++){
                    clients[i-1] = clients[i];
	        }
	    }
	    clientCount--;
	    try{  
	      	toTerminate.close(); 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError closing thread: " + ioe); 
	    }
	    toTerminate.stop(); 
	}
    }
    //when a new client requests a connection and new thread will be added
    private void addThread(Socket socket){  
	if (clientCount < clients.length){  
            ui.jTextArea1.append("\nClient accepted: " + socket);
	    clients[clientCount] = new ServerThread(this, socket);
	    try{  
	      	clients[clientCount].open(); 
	        clients[clientCount].start();  
	        clientCount++; 
	    }
	    catch(IOException ioe){  
	      	ui.jTextArea1.append("\nError opening thread: " + ioe); 
	    } 
	}
	else{
            ui.jTextArea1.append("\nClient refused: maximum " + clients.length + " reached.");
	}
    }
}
