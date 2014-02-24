/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.databasewrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class DatabaseConfiguration {

   
    
    private final String driver;
    private final String dbName;
    private final String connectionString;
    private final String dbuserName;
    private final String dbpassword;

    public DatabaseConfiguration() {
        ConfigParser parser = new ConfigParser();
        Properties prop = parser.getConfig();
        
        driver = prop.getProperty("driver") ;
        dbName = prop.getProperty("db_name");
        connectionString = prop.getProperty("connection_string");
// connectionString = "jdbc:mysql://mysql-reaction.ebi.ac.uk:4083";
        dbuserName = prop.getProperty("db_username");
        dbpassword = prop.getProperty("db_password");
    }

    /**
* @return the driver
*/
    public String getDriver() {
        return driver;
    }

    /**
* @return the dbName
*/
    public String getDBName() {
        return dbName;
    }

    /**
* @return the connectionString
*/
    public String getConnectionString() {
        return connectionString;
    }

    /**
* @return the dbuserName
*/
    public String getDBUserName() {
        return dbuserName;
    }

    /**
* @return the dbpassword
*/
    public String getDBPassword() {
        return dbpassword;
    }
}