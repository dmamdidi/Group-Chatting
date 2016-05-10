package com.socket;

import java.io.Serializable;

public class Message implements Serializable{
    /*
    If a serializable class does not explicitly declare a serialVersionUID, then the serialization 
    runtime will calculate a default serialVersionUID value for that class based on various aspects 
    of the class, as described in the Java(TM) Object Serialization Specification. However, it is strongly 
    recommended that all serializable classes explicitly declare serialVersionUID values
    */
    
   private static final long serialVersionUID = 1L;
    public String type, sender, content, recipient;
   
    public Message(String type, String sender, String content, String recipient){
        this.type = type; this.sender = sender; this.content = content; this.recipient = recipient;
    }
    
    @Override
    public String toString(){
        return "{type='"+type+"', sender='"+sender+"', content='"+content+"', recipient='"+recipient+"'}";
    }
}
