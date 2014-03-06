/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ecblast.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import static sun.misc.RequestProcessor.postRequest;

/**
 *
 * @author saket
 */
public class EcblastTest {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here

        /* Client client = Client.create();

         WebResource webResource = client.resource("http://localhost:8080/ecblast-rest/");
         ClientResponse response = webResource.accept("application/xml")
         .get(ClientResponse.class);
         if (response.getStatus() != 200) {
         throw new RuntimeException("Failed : HTTP error code : "
         + response.getStatus());
         }

         String output = response.getEntity(String.class);

         System.out.println("Output from Server .... \n");
         System.out.println(output); */
        EcblastTest ecb = new EcblastTest();
        String file = "/home/saket/Desktop/R03200.rxn";
        //response = ecb.executeMultiPartRequest("http://localhost:8080/ecblast-rest/aam", file, file.getName(), "File Uploaded :: Tulips.jpg");
        String smi = "[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12])[CH3:6].[H:30][OH:14]>>[H:30][O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12].[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([OH:14])[CH3:6]";
        //String response = ecb.compareReactions("RXN",file,"RXN", file);
        String response = ecb.compareReactions("SMI", smi, "SMI", smi);
        System.out.println(response);

        //ecb.executeCompareReactionsRXN(file, file);
        //
     //   String x = ecb.atomAtomMappingSMI("/home/saket/Desktop/R03200.rxn", "RXN");
        //String x = ecb.atomAtomMappingSMI(smi,"SMI");
     //   System.out.println("***********" + x);

    }
    public String rxn;
    public String smi;

    public EcblastTest() {
        this.rxn = "/home/saket/Desktop/R03200.rxn";

    }

    public String atomAtomMappingRXN() throws Exception {
        EcblastTest ecb = new EcblastTest();
        File file = new File(this.rxn);
        //ecb.executeMultiPartRequest("http://localhost:8080/ecblast-rest/aam", file, file.getName(), "File Uploaded :: Tulips.jpg");
        return null;

    }

    public String atomAtomMappingSMI(String query, String type) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(MultiPartWriter.class);
        Client client = Client.create(cc);
        String urlString = "http://localhost:8080/ecblast-rest/aam";
        WebResource webResource = client.resource("http://localhost:8080/ecblast-rest/aam");
        //String smi = "[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12])[CH3:6].[H:30][OH:14]>>[H:30][O:4][C:2](=[O:3])[CH2:1][CH:11]([OH:13])[CH3:12].[O:9]=[C:8]([OH:10])[CH2:7][CH:5]([OH:14])[CH3:6]";

        FormDataMultiPart form = new FormDataMultiPart();
        switch (type) {
            case "SMI":
                form.field("q", query);
                form.field("Q", "SMI");
                break;
            case "RXN":
                /*MultivaluedMapImpl values = new MultivaluedMapImpl();
                values.add("q", new File(query));
                values.add("Q", "RXN");
                ClientResponse response = webResource.type(MediaType.).post(ClientResponse.class, values);
                return response.toString();
                
                File attachment = new File(query);
                
                FileInputStream fis = null;
                
                fis = new FileInputStream(attachment);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                try {
                for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
                }
                fis.close();
                bos.close();
                } catch (IOException ex) {
                try {
                fis.close();
                bos.close();
                } catch (IOException e) {
                return "ERROR";
                }
                return "ERROR";
                
                }
                byte[] bytes = bos.toByteArray();
                
                FormDataBodyPart bodyPart = new FormDataBodyPart("q", new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM_TYPE);
                form.bodyPart(bodyPart);
                //form.field("q", bodyPart);
                //form.field*
                form.field("Q", "RXN", MediaType.MULTIPART_FORM_DATA_TYPE);*/
                DefaultHttpClient client1;
                client1 = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost(urlString);
                MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                // FileBody queryFileBody = new FileBody(queryFile);
                multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                //multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : ""));
                //multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName()));
                File file = new File(query);
                FileBody fileBody = new FileBody(file);
                //Prepare payload
                multiPartEntity.addPart("q", fileBody);
                multiPartEntity.addPart("Q", new StringBody("RXN", "text/plain", Charset.forName("UTF-8")));
                //Set to request body
                postRequest.setEntity(multiPartEntity);
                
                //Send request
                HttpResponse response = client1.execute(postRequest);
                return response.toString();
        }
        form.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

        ClientResponse responseJson = webResource.type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);
        return responseJson.toString();
    }
    /*
     Client client = Client.create();

     WebResource webResource = client
     .resource("http://localhost:8080/ecblast-rest/aam");

     String input = "{\"Q\":\"SMI\",\"q\":\"" + smi + "\"}";

     ClientResponse response = webResource.type("application/xml")
     .post(ClientResponse.class, input);

     if (response.getStatus() != 201) {
     throw new RuntimeException("Failed : HTTP error code : "
     + response.getStatus());
     }

     System.out.println("Output from Server .... \n");
     String output = response.getEntity(String.class);
     System.out.println(output);*/

//return null;
    //}


    public String executeCompareReactions(File queryFile, File targetFile) {
        String urlString = "http://localhost:8080/ecblast-rest/compare";
        DefaultHttpClient client;
        client = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(urlString);

        try {
            //Set various attributes
            MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileBody queryFileBody = new FileBody(queryFile);
            //Prepare payload
            multiPartEntity.addPart("q", queryFileBody);
            multiPartEntity.addPart("Q", new StringBody("RXN", "text/plain", Charset.forName("UTF-8")));

            FileBody targetFileBody = new FileBody(targetFile);
            multiPartEntity.addPart("t", targetFileBody);
            multiPartEntity.addPart("T", new StringBody("RXN", "text/plain", Charset.forName("UTF-8")));
            //Set to request body
            postRequest.setEntity(multiPartEntity);

            //Send request
            HttpResponse response = client.execute(postRequest);
            
            //Verify response if any
            if (response != null) {
                System.out.println(response.getStatusLine().getStatusCode());
                
                return response.toString();
            }
        } catch (IOException ex) {
            return null;
        }
        return null;

    }

    public String compareReactions(String queryFormat, String query, String targetFormat, String target ) throws Exception {
        DefaultHttpClient client;
        client = new DefaultHttpClient();
        String urlString = "http://localhost:8080/ecblast-rest/compare";
        HttpPost postRequest = new HttpPost(urlString);
        try {
            //Set various attributes
            MultipartEntity multiPartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            //multiPartEntity.addPart("fileDescription", new StringBody(fileDescription != null ? fileDescription : ""));
            //multiPartEntity.addPart("fileName", new StringBody(fileName != null ? fileName : file.getName()));
            switch (queryFormat) {
                case "RXN":
                    FileBody fileBody = new FileBody(new File(query));
                    //Prepare payload
                    multiPartEntity.addPart("q", fileBody);
                    multiPartEntity.addPart("Q", new StringBody("RXN", "text/plain", Charset.forName("UTF-8")));
                    break;
                case "SMI":
                    multiPartEntity.addPart("q",  new StringBody(query, "text/plain", Charset.forName("UTF-8")));
                    multiPartEntity.addPart("Q", new StringBody("SMI", "text/plain", Charset.forName("UTF-8")));
                    break;
            }
            switch (targetFormat) {
                case "RXN":
                    FileBody fileBody = new FileBody(new File(target));
                    //Prepare payload
                    multiPartEntity.addPart("t", fileBody);
                    multiPartEntity.addPart("T", new StringBody("RXN", "text/plain", Charset.forName("UTF-8")));
                    break;
                case "SMI":
                    multiPartEntity.addPart("t",  new StringBody(target, "text/plain", Charset.forName("UTF-8")));
                    multiPartEntity.addPart("T", new StringBody("SMI", "text/plain", Charset.forName("UTF-8")));
                    break;
            }
            
            
            
            //Set to request body
            postRequest.setEntity(multiPartEntity);

            //Send request
            HttpResponse response = client.execute(postRequest);

            //Verify response if any
            if (response != null) {
                System.out.println(response.getStatusLine().getStatusCode());
                 return response.toString();
            }
        } catch (IOException ex) {
        }
        return null;
    }

    public String getStatus(String jobID) {
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/ecblast-rest/status/" + jobID);
        ClientResponse response = webResource.accept("application/xml")
                .get(ClientResponse.class);
        return response.toString();
    }

    public String getResultText(String jobID) {
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/ecblast-rest/status/" + jobID + "/text");
        ClientResponse response = webResource.accept("application/xml")
                .get(ClientResponse.class);
        return response.toString();
    }

    public String getResultXml(String jobID) {
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/ecblast-rest/status/" + jobID + "/xml");
        ClientResponse response = webResource.accept("application/xml")
                .get(ClientResponse.class);
        return response.toString();
    }

    public String getResultMapped(String jobID) {
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/ecblast-rest/result/" + jobID + "/mapped");
        ClientResponse response = webResource.accept("application/xml")
                .get(ClientResponse.class);
        return response.toString();
    }

}
