/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class LogFileWriter {

    public String uuid;

    public LogFileWriter(String uuid) {
        this.uuid = uuid;
    }

    public void WriteToFile(String data) throws IOException {
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        String log_directory = prop.getProperty("tomcat_log_directory");
        File file = new File(log_directory + "/" + this.uuid+".log");
        if (!file.exists()) {
            file.createNewFile();
        }
        System.out.println(data);
        FileWriter fileWritter = new FileWriter(file.getName(), true);
        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
        bufferWritter.write(data);
        bufferWritter.write("\n");
        bufferWritter.close();
    }
}
