package com.socket;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class Database {
    
    public String filePath;
    //constructor calls and assigns the filepath
    public Database(String filePath){
        this.filePath = filePath;
    }
    //Method checks if the user already exists or not in db
    public boolean userExists(String username){
        
        try{
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            //API to obtain DOM Document instances from an XML document
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            //Nodelist is live collection, any changes in the doc will be reflected
            //It stores all the values with the tah name user from the xml file
            NodeList nList = doc.getElementsByTagName("user");
           
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                System.out.println(nNode);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    if(getTagValue("username", eElement).equals(username)){
                        return true;
                    }
                }
            }
            return false;
        }
        catch(Exception ex){
            System.out.println("Database exception : userExists()");
            return false;
        }
    }
    //Checks the xml file return false if the user already exists
    public boolean checkLogin(String username, String password){
        
        if(!userExists(username)){ 
            return false; 
        }
        
        try{
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            //API to obtain DOM Document instances from an XML document
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            //Parse the content of the given file as an XML document and return a new DOM Document object.
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("user");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if(getTagValue("username", eElement).equals(username) && getTagValue("password", eElement).equals(password)){
                        return true;
                    }
                }
            }
            
            return false;
        }
        catch(Exception ex){
            System.out.println("Database exception : userExists()");
            return false;
        }
    }
    //this method adds the new users when ever requested 
    public void addUser(String username, String password){
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filePath);
 
            Node data = doc.getFirstChild();
            
            Element newuser = doc.createElement("user");
            Element newusername = doc.createElement("username"); 
            newusername.setTextContent(username);
            Element newpassword = doc.createElement("password"); 
            newpassword.setTextContent(password);
            
            newuser.appendChild(newusername); 
            newuser.appendChild(newpassword); 
            data.appendChild(newuser);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
           // Create a new Transformer that performs a copy of the Source to the Result.
            transformer.transform(source, result);
 
	   } 
           catch(Exception ex){
		System.out.println("Exceptionmodify xml");
	   }
	}
    
    public static String getTagValue(String sTag, Element eElement) {
	NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        
        //gives the value of the elements from xml file
        Node nValue = (Node) nlList.item(0);
        
	return nValue.getNodeValue();
  }
}
