/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ebi.ecblast.ecblastWS.webserver;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 *
 * @author saket
 */
@XmlRootElement(name = "ecblastError")
@JsonRootName(value = "ecblastError")

public class CustomException extends Exception {
    
    private int errorID;
    
    public CustomException(){
        super( "Internal Server Error");
    }
    
    public CustomException(String message){
        super(message);
    }
    
    public CustomException(String message, Throwable cause){
        super(message, cause);
    }

    private CustomException(Response.Status status, String internal_Server_Error) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
