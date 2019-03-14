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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.jws.WebService;

@WebService
public class UCImplementation implements UC
{
  
   @Override
   public double in2cm(double in)
   {
      return in * 2.54;
   }
   
   @Override
    public void upload(String fileName, byte[] imageBytes) {
         
        String filePath = "e:/Test/Server/Upload/" + fileName;
         
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            BufferedOutputStream outputStream = new BufferedOutputStream(fos);
            outputStream.write(imageBytes);
            outputStream.close();
             
            System.out.println("Received file: " + filePath);
             
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    @Override
    public void sendStream(byte[] imageBytes) {
            
    }
    
    @Override
    public byte[] download(String fileName) {
        fileName="teste.jpg";
        String filePath = "C:\\Pessoal\\Workspace\\elastic-ran\\ElasticRanStreamAPI\\dist\\" + fileName;
        System.out.println("Sending file: " + filePath);
         
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream inputStream = new BufferedInputStream(fis);
            byte[] fileBytes = new byte[(int) file.length()];
            //inputStream.read(fileBytes);
            inputStream.close();
             
            return fileBytes;
        } catch (IOException ex) {
            System.err.println(ex);
        }      
       return null;
    }
    
    @Override
    public byte[] getStream(int size){
        byte[] fileBytes = new byte[size];
        return fileBytes;    
    }
}