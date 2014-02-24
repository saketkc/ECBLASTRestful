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
    public String reactionCenterChanges;
    @javax.xml.bind.annotation.XmlElement
    public String reactionCentreTransformationPairs;
    @javax.xml.bind.annotation.XmlElement
    public String moleculeTransformationPairs;
    @javax.xml.bind.annotation.XmlElement
    public String atomAtomMappingLink;
    @javax.xml.bind.annotation.XmlElement
    public String atomAtomMappingTextLink;
    @javax.xml.bind.annotation.XmlElement
    public String atomAtomMappingImageLink;
    @javax.xml.bind.annotation.XmlElement
    public String atomAtomMappingXMLLink;
    @javax.xml.bind.annotation.XmlElement
    public String atomatomMappingResultText;

    public String getAtomatomMappingResultText() {
        return atomatomMappingResultText;
    }

    public void setAtomatomMappingResultText(String atomatomMappingResultText) {
        this.atomatomMappingResultText = atomatomMappingResultText;
    }

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

    public String getReactionCenterChanges() {
        return reactionCenterChanges;
    }

    public void setReactionCenterChanges(String reactionCenterChanges) {
        this.reactionCenterChanges = reactionCenterChanges;
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

    public String getAtomAtomMappingLink() {
        return atomAtomMappingLink;
    }

    public void setAtomAtomMappingLink(String atomAtomMappingLink) {
        this.atomAtomMappingLink = atomAtomMappingLink;
    }

    public String getAtomAtomMappingTextLink() {
        return atomAtomMappingTextLink;
    }

    public void setAtomAtomMappingTextLink(String atomAtomMappingTextLink) {
        this.atomAtomMappingTextLink = atomAtomMappingTextLink;
    }

    public String getAtomAtomMappingImageLink() {
        return atomAtomMappingImageLink;
    }

    public void setAtomAtomMappingImageLink(String atomAtomMappingImageLink) {
        this.atomAtomMappingImageLink = atomAtomMappingImageLink;
    }

    public String getAtomAtomMappingXMLLink() {
        return atomAtomMappingXMLLink;
    }

    public void setAtomAtomMappingXMLLink(String atomAtomMappingXMLLink) {
        this.atomAtomMappingXMLLink = atomAtomMappingXMLLink;
    }

    public AtomAtomMappingResponse() {
    }

}
