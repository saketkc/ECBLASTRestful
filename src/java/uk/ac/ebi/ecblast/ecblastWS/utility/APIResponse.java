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
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 *
 * @author saketc
 */
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "EC_BLAST")
@JsonRootName(value = "EC_BLAST")
public class APIResponse {

    @XmlElement
    public String response;
    @XmlElement
    public String message;

    @XmlElement
    public String jobID;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

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
