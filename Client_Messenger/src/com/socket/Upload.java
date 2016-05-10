package com.socket;

import com.ui.ChatFrame;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Upload implements Runnable{

    public String addr;
    public int port;
    public Socket socket;
    public FileInputStream In;
    public OutputStream Out;
    public File file;
    public ChatFrame ui;
    //File will be uploaded when the user requests file transfer
    public Upload(String addr, int port, File filepath, ChatFrame frame){
        super();
        try {
            file = filepath; 
            ui = frame;
            //new socket is created with IP and port address
            socket = new Socket(InetAddress.getByName(addr), port);
            Out = socket.getOutputStream();
            In = new FileInputStream(filepath);
        } 
        catch (Exception ex) {
            System.out.println("Exception [Upload : Upload(...)]");
        }
    }
    
    @Override
    public void run() {
        try {       
            byte[] buffer = new byte[1024];
            int count;
            
            while((count = In.read(buffer)) >= 0){
                Out.write(buffer, 0, count);
            }
            Out.flush();
            //Displays message when the file uploaded completely
            ui.jTextArea1.append("[Applcation > Me] : File upload complete\n");
            ui.jButton5.setEnabled(true); ui.jButton6.setEnabled(true);
            ui.jTextField5.setVisible(true);
            
            if(In != null){ In.close(); }
            if(Out != null){ Out.close(); }
            if(socket != null){ socket.close(); }
        }
        catch (Exception ex) {
            System.out.println("Exception [Upload : run()]");
            ex.printStackTrace();
        }
    }

}