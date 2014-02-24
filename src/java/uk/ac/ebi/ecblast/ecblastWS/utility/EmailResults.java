                /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ebi.ecblast.ecblastWS.utility;

import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class EmailResults {
    public String zipFilePath;
    public String uuid;
    public String emailID;
    public String subject;
    public String message;
    boolean attachFile;
    public String[] filepaths;

    public void setFilepaths(String[] filepaths) {
        this.filepaths = filepaths;
    }

    public String[] getFilepaths() {
        return filepaths;
    }
    public String sendMail() {
        ConfigParser parser = new ConfigParser();
        Properties prop = parser.getConfig();
        String to = this.emailID;
        String from = prop.getProperty("email_from");
        String host = prop.getProperty("email_host");
        String[] filenames = this.filepaths;
        String msgText = this.message;
        System.out.println("to" + to);
        // create some properties and get the default Session
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        Session session = Session.getInstance(props, null);

        try {

            // create a message
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(subject);

            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(msgText);
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);

            if (this.attachFile) {
                for (String filename : filenames) {
                    MimeBodyPart mbp = new MimeBodyPart();
                    FileDataSource fds = new FileDataSource(filename);
                    mbp.setDataHandler(new DataHandler(fds));
                    mbp.setFileName(fds.getName());
                    mp.addBodyPart(mbp);
                }
            }
            msg.setContent(mp);
            msg.setSentDate(new Date());
            Transport.send(msg);

            return "message_sent";

        } catch (MessagingException mex) {
            Exception ex = null;
            if ((ex = mex.getNextException()) != null) {
            }
            return "error_in_sending_message " + ex.toString();
        }

    }

    public String getZipFilePath() {
        return zipFilePath;
    }

    public void setZipFilePath(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAttachFile() {
        return attachFile;
    }

    public void setAttachFile(boolean attachFile) {
        this.attachFile = attachFile;
    }

    public EmailResults(String uuid, String emailID, String subject, String message, boolean attachFile) {
        
    
        this.uuid = uuid;
        this.emailID = emailID;
        this.subject = subject;
        this.message = message;
        this.attachFile = attachFile;
    }
}
