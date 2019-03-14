/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticranapiclient;

import java.io.IOException;
import javax.xml.soap.*;

/**
 *
 * @author leand
 */
public class SoapRequestStructure {
    String soapEndpointUrl = "http://localhost:9901/UC";
    private SOAPMessage soapMessage;
    private String myNamespace = "elasticranstreamapi";
    private String myNamespaceURI = "http://elasticranstreamapi/";
    
    public SoapRequestStructure() throws SOAPException, IOException{
        BuildSoapStructure();
    }
    
    public void SetSize(String psize) throws SOAPException, IOException{
        soapMessage.getSOAPBody().getFirstChild().getFirstChild().setTextContent(psize);
        soapMessage.saveChanges();        
    }
    
    public SOAPMessage GetRequest(){
        return soapMessage;
    }    
    
        private void BuildSoapStructure() throws SOAPException, IOException
    {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();

        createSoapEnvelope(message);

        message.saveChanges();
        message.writeTo(System.out);
        soapMessage = message;
    }
    
    private void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();
        
        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);
        
        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("getStream", myNamespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("arg0");
        soapBodyElem1.addTextNode("1");
    }
}
