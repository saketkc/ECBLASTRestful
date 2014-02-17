/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ebi.ecblast.ecblastWS.utility;

/**
 *
 * @author saket
 */
import javax.xml.bind.annotation.*;

/**
*
* @author saketc
*/
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ecblastResponse")
public class APIResponse  {
    
    @javax.xml.bind.annotation.XmlElement
    private String response;
        @javax.xml.bind.annotation.XmlElement

    private String message;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
