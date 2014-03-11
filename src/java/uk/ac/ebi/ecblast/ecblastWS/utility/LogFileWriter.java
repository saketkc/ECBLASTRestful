package uk.ac.ebi.ecblast.ecblastWS.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        /*FileWriter fileWriter = new FileWriter(file.getName(), true);
        BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
        bufferWriter.write(data);
        bufferWriter.write("\n");
        bufferWriter.close();*/
    
    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file.getName(), true)));
    out.println(data);
    out.close();

    }
}
