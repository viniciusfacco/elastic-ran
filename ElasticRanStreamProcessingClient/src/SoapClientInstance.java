/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author leand
 */
public class SoapClientInstance implements Runnable {
    private String soapEndpointUrl;
    private final String soapAction = "";
    private final String size;
    private final SoapRequestStructure soapRequestStructure;
    private StringBuilder responseTime;
    
    public SoapClientInstance(String psize, String psoapEndpointUrl, SoapRequestStructure psoapRequestStructure, StringBuilder responseTime){
        size = psize;
        soapRequestStructure = psoapRequestStructure;
        soapEndpointUrl = psoapEndpointUrl;
        this.responseTime = responseTime;
    }
    
    public void ExecuteStream() throws SOAPException, IOException{
        soapRequestStructure.SetSize(size);

        // Create SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        
        
        long lStartTime = System.currentTimeMillis();
        SOAPMessage soapResponse = soapConnection.call(soapRequestStructure.GetRequest(), soapEndpointUrl);        
        long lEndTime = System.currentTimeMillis();
        long estimatedTime = (lEndTime - lStartTime);
        
        //soapResponse.writeTo(System.out);
        this.responseTime.delete(0, responseTime.length());
        this.responseTime.append(String.valueOf(estimatedTime));
        
        //System.out.println("Elapsed time in milliseconds for SIZE "+ size +" : " + output / 1000000);
       
        soapConnection.close();        
    } 

    @Override
    public void run() {
        try {
            ExecuteStream();
        } catch (SOAPException ex) {
            Logger.getLogger(SoapClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SoapClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

