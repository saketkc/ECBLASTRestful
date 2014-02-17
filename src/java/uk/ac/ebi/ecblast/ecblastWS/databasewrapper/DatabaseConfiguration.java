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
        driver = "com.mysql.jdbc.Driver";
        dbName = "ecblast_restful";
        connectionString = "jdbc:mysql://localhost:3306";
// connectionString = "jdbc:mysql://mysql-reaction.ebi.ac.uk:4083";
        dbuserName = "root";
        dbpassword = "fedora13";
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