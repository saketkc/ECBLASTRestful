/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.databasewrapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public int insertJob(String uniqueID, Integer jobID, String fileName, String emailID) {
        int execute = 0;
        String query;
        if (emailID == null) {
            query = "INSERT INTO jobs(uniqueID, farmjobID, submittedAT, lastcheckedAT, status, fileName, email) VALUES(?,?,?,?,?,?,?)";
        } else {
            query = "INSERT INTO jobs(uniqueID, farmjobID, submittedAT, lastcheckedAT, status, fileName, email) VALUES(?,?,?,?,?,?,?)";
        }
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
            stmt.setString(6, fileName);
        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;

        }
        
        if(emailID!=null){
            try {
                stmt.setString(7, emailID);
            } catch (SQLException ex) {
                Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                stmt.setString(7, null);
            } catch (SQLException ex) {
                Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            execute = stmt.executeUpdate();
            stmt.closeOnCompletion();

        } catch (SQLException ex) {
            Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return execute;
        }

        return execute;
    }

    public boolean updateJob(String uuid, String status) {
        String updateStatusQuery = "UPDATE jobs SET Status=? WHERE uniqueID=?";
        String updateTimeQuery = "UPDATE jobs SET lastcheckedAT=? WHERE uniqueID=?";
        PreparedStatement stmStatus;
        PreparedStatement stmTime;
        Date date = new Date();
        SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String checkedAT = dateTime.format(date);
        try {
            stmStatus = (PreparedStatement) connection.prepareStatement(updateStatusQuery);
            stmTime = (PreparedStatement) connection.prepareStatement(updateTimeQuery);
        } catch (SQLException ex) {
            // Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            stmStatus.setString(1, status);
            stmTime.setString(1, checkedAT);
        } catch (SQLException ex) {
            return false;
            //Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stmStatus.setString(2, uuid);
            stmTime.setString(2, uuid);
        } catch (SQLException ex) {
            return false;
            //Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            stmStatus.execute();
            stmTime.execute();
        } catch (SQLException ex) {
            return false;
            //Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public String getPendingJobIDs() {
        try {
            String query = "SELECT uniqueID FROM jobs WHERE status=? or status=?";
            PreparedStatement stm = (PreparedStatement) connection.prepareStatement(query);
            stm.setString(1, "pending");
            stm.setString(2, "running");
            
            ResultSet rs = stm.executeQuery();
            String returnMessage = "";

            while (rs.next()) {
                returnMessage = returnMessage + rs.getString(1) + ";";

            }
            return returnMessage;
        } catch (SQLException ex) {
            return null;
            //Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getJobStatus(String uniqueID) {
        try {
            String query = "SELECT status FROM jobs where uniqueID=?";
            PreparedStatement stm = (PreparedStatement) connection.prepareStatement(query);
            stm.setString(1, uniqueID);
            ResultSet rs = stm.executeQuery();
            String returnMessage = null;

            while (rs.next()) {
                returnMessage = rs.getString(1);
                // ...
            }
            return returnMessage;
        } catch (SQLException ex) {
            return null;
            //Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    public String getEmailFromUUID(String uniqueID){
        try {
            String query = "SELECT email FROM jobs where uniqueID=?";
            PreparedStatement stm = (PreparedStatement) connection.prepareStatement(query);
            stm.setString(1, uniqueID);
            ResultSet rs = stm.executeQuery();
            String returnMessage = null;
            System.out.println(stm.toString());
            System.out.println(query);
            System.out.println(uniqueID);
            while (rs.next()) {
                returnMessage = rs.getString(1);              
            }
            return returnMessage;
        } catch (SQLException ex) {
            return null;
            //Logger.getLogger(JobsQueryWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
