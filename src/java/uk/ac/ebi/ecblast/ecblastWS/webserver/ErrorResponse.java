/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.webserver;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author saket
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ErrorResponse extends WebApplicationException {

    @XmlAttribute
    private String reason;
    @XmlAttribute
    private Status status;
    @XmlAttribute
    private String message;

    @XmlAttribute
    public String getReason() {
        return reason;
    }

    @XmlAttribute
    public void setReason(String reason) {
        this.reason = reason;
    }

    @XmlAttribute
    public Status getStatus() {
        return status;
    }

    @XmlAttribute
    public void setStatus(Status status) {
        this.status = status;
    }

    public ErrorResponse() {
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorResponse(Status status, String reason, String message) {
        //super(message);
        this.reason = reason;
        this.status = status;

    }

    // Getters/setters/initializer
}
