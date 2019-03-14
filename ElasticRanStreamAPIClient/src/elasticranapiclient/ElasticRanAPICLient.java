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
public class ElasticRanAPICLient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SOAPException, InterruptedException, IOException {
        SoapRequestStructure request = new SoapRequestStructure();
        
        Thread t = new Thread(new SoapClientInstance(args[0], args[1], request));
        t.start();
        t.join();
    }
    
}
