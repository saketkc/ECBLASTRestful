/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebi.ecblast.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saket
 */
public class JobsQueryWrapper extends DatabaseWrapper {

    public JobsQueryWrapper(String driver,
            String connectionString,
            String dbName,
            String userName,
            String password) throws ClassNotFoundException {
        super(driver, connectionString, dbName, userName, password);
    }

    public boolean insertJob(String uniqueID, Integer jobID) {
        boolean execute = false;
        String query = "INSERT INTO jobs(uniqueID,farmjobID,submittedAT,lastcheckedAT,status) VALUES(?,?,?,?,?)";
        Date date = new Date();

        SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String submittedAT = dateTime.format(date);
        PreparedStatement stmt = null;
        try {
            stmt = (PreparedStatement) connection.prepareStatement(query);
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;
        }
        try {
            stmt.setString(1, uniqueID);
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;
        }
        try {
            stmt.setInt(2, jobID);
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;
        }
        try {
            stmt.setString(3, submittedAT);
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stmt.setString(4, submittedAT);
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;

        }
        try {
            stmt.setString(5, "pending");
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;

        }

        try {
            execute = stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;
        }

        return execute;
    }
}
