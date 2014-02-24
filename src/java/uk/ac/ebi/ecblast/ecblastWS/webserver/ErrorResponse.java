/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.webserver;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.map.annotate.JsonRootName;

/**
 *
 * @author saket
 */
//@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ecblastError")
@JsonRootName(value = "ecblastError")

public class ErrorResponse extends WebApplicationException {

   @XmlElement

    private Status status;
   @XmlElement


    public String message;
@XmlElement

    public Status getStatus() {
        return status;
    }
@XmlElement

    public void setStatus(Status status) {
        this.status = status;
    }

    public ErrorResponse() {
    }
@XmlElement

    public void setMessage(String message) {
        this.message = message;
    }
@XmlElement

    @Override
    public String getMessage() {
        return this.message;
    }

    public ErrorResponse(Status status, String message) {
        //super(message);
        this.message = message;

        this.status = status;

    }

    // Getters/setters/initializer
}
