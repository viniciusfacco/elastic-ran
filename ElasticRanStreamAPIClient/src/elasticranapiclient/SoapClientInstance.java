/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticranapiclient;

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
    
    public SoapClientInstance(String psize, String psoapEndpointUrl, SoapRequestStructure psoapRequestStructure){
        size = psize;
        soapRequestStructure = psoapRequestStructure;
        soapEndpointUrl = psoapEndpointUrl;
    }
    
    public void ExecuteStream() throws SOAPException, IOException{
        soapRequestStructure.SetSize(size);

        // Create SOAP Connection
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();
        
        
        long lStartTime = System.nanoTime();
	
        SOAPMessage soapResponse = soapConnection.call(soapRequestStructure.GetRequest(), soapEndpointUrl);        
        
        long lEndTime = System.nanoTime();
        long output = lEndTime - lStartTime;
        //soapResponse.writeTo(System.out);
        System.out.println("Elapsed time in milliseconds for SIZE "+ size +" : " + output / 1000000);
       
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
