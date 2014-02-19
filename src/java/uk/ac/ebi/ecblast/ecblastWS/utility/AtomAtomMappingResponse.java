/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.utility;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author saket
 */
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ecblastResponse")
public class AtomAtomMappingResponse extends APIResponse {

    @javax.xml.bind.annotation.XmlElement
    public String bondChangeFingerprint;
    @javax.xml.bind.annotation.XmlElement
    public String reactionCenterFingerprint;
    @javax.xml.bind.annotation.XmlElement
    public String ractionCenterChanges;
    @javax.xml.bind.annotation.XmlElement
    public String reactionCentreTransformationPairs;
    @javax.xml.bind.annotation.XmlElement
    public String moleculeTransformationPairs;

    public String getBondChangeFingerprint() {
        return bondChangeFingerprint;
    }

    public void setBondChangeFingerprint(String bondChangeFingerprint) {
        this.bondChangeFingerprint = bondChangeFingerprint;
    }

    public String getReactionCenterFingerprint() {
        return reactionCenterFingerprint;
    }

    public void setReactionCenterFingerprint(String reactionCenterFingerprint) {
        this.reactionCenterFingerprint = reactionCenterFingerprint;
    }

    public String getRactionCenterChanges() {
        return ractionCenterChanges;
    }

    public void setRactionCenterChanges(String ractionCenterChanges) {
        this.ractionCenterChanges = ractionCenterChanges;
    }

    public String getReactionCentreTransformationPairs() {
        return reactionCentreTransformationPairs;
    }

    public void setReactionCentreTransformationPairs(String reactionCentreTransformationPairs) {
        this.reactionCentreTransformationPairs = reactionCentreTransformationPairs;
    }

    public String getMoleculeTransformationPairs() {
        return moleculeTransformationPairs;
    }

    public void setMoleculeTransformationPairs(String moleculeTransformationPairs) {
        this.moleculeTransformationPairs = moleculeTransformationPairs;
    }

    public AtomAtomMappingResponse() {
    }

}
