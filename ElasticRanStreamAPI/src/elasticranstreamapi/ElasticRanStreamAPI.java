/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticranstreamapi;

/**
 *
 * @author leandro.andrioli
 */

import javax.xml.ws.Endpoint;

public class ElasticRanStreamAPI
{
   public static void main(String[] args)
   {
      //Endpoint.publish("http://localhost:9901/UC", new UCImplementation());
        Endpoint.publish(args[0], new UCImplementation());
   }
}
