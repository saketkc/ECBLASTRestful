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
public class EmailText {

    public static String atomAtomMappingSubject = "Your Atom Atom Mapping Results";
    public static String atomAtomMappingEmail = "Hi, Your atom atom mapping results are attached";

    public EmailText() {
    }

    public final String getAtomAtomMappingSubject() {
        return atomAtomMappingSubject;
    }

    public final String getAtomAtomMappingEmail() {
        return atomAtomMappingEmail;
    }

}
