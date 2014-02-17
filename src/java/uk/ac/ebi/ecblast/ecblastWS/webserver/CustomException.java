/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.eac.bi.ecbla.ecblastWS.webserverst;
import javax.ws.rs.core.Response;

/**
 *
 * @author saket
 */
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
