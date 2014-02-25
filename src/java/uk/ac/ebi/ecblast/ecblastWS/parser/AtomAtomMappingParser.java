/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saket
 */
public class AtomAtomMappingParser {

    public String filePath;

    public AtomAtomMappingParser(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String readFileInString() {
        String fileAsString = "";
        BufferedReader br;
        String currentLine;
        try {
            br = new BufferedReader(new FileReader(filePath));
            try {
                while ((currentLine = br.readLine()) != null) {
                    fileAsString = fileAsString + currentLine;
                }
            } catch (IOException ex) {
                return null;
            }
        } catch (FileNotFoundException ex) {
            return null;
        }

        return fileAsString;

    }
    
    public String[] getAllSections(String contents){
        String[] headerParts = contents.split("\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+\\+");
        if (headerParts.length<3){
            return null;
        }
        String[] parts = headerParts[2].split("//");
        
        return parts;
        
    }
}
