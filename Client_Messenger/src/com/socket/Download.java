package com.socket;

import com.ui.ChatFrame;
import java.io.*;

import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Download implements Runnable{
    
    public ServerSocket server;
    public Socket socket;
    public int port;
    public String saveTo = "";
    public InputStream In;
    public FileOutputStream Out;
    public ChatFrame ui;
    
    //Downloading the file which the client sends
    public Download(String saveTo, ChatFrame ui){
        try {
            server = new ServerSocket(0);
            port = server.getLocalPort();
            this.saveTo = saveTo;
            this.ui = ui;
        } 
        catch (IOException ex) {
            System.out.println("Exception [Download : Download(...)]");
        }
    }

    @Override
    public void run() {
        try {
            //Once the connection is established the download will start
            socket = server.accept();
            System.out.println("Download : "+socket.getRemoteSocketAddress());
            
            In = socket.getInputStream();
            Out = new FileOutputStream(saveTo);
            
            byte[] buffer = new byte[1024];
            int count;
            //writes the content of the file completely
            while((count = In.read(buffer)) >= 0){
                Out.write(buffer, 0, count);
            }
            
            Out.flush();
            
            ui.jTextArea1.append("[Application > Me] : Download complete\n");
            
            if(Out != null){ Out.close(); }
            if(In != null){ In.close(); }
            if(socket != null){ socket.close(); }
        } 
        catch (Exception ex) {
            System.out.println("Exception [Download : run(...)]");
        }
    }
}