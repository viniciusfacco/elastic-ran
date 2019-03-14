/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elasticranstreamapi;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface UC
{
   @WebMethod double in2cm(double in);
   @WebMethod public void upload(String fileName, byte[] imageBytes);
   @WebMethod public byte[] download(String fileName);
   @WebMethod public byte[] getStream(int size);
   @WebMethod public void sendStream(byte[] imageBytes);
}