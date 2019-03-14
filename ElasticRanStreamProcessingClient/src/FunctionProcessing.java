/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.util.Random;
import javax.xml.soap.SOAPException;

/**
 *
 * @author vinicius.rodrigues
 */
public class FunctionProcessing {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, SOAPException {
        
        int port = 40000; //porta de comunicação que será utilizada pelos sockets
        int sockets = 4; //quantidade inicial de sockets (essa é a quantidade inicial de slaves)
        int elastic_grain = 1;
        int max_sockets = 30; //quantidade máxima de sockets que poderão ser abertos
        String type = ""; //tipo de aplicação que se está executando (master ou slave)
        String ip_master = "";
        String logname = null; //nome dos logs
        String soapEndpoint = null; //Endereço com porta que está rodando a API do ElasticRanStream
        //String arquivo = "C:\\one\\app\\jobs\\func.txt"; //Windows aquivo que será processado
        //String compath = "C:\\one\\app\\msg\\"; //Windows diretório de comunicação onde a aplicação verificará a utilização de recursos
        String arquivo = "/one/app/jobs/func.txt"; //Linux aquivo que será processado
        String compath = "/one/app/msg/"; //Linux diretório de comunicação onde a aplicação verificará a utilização de recursos
        String logpath = "/one/app/logs/"; //diretório que serão salvos os logs
        System.out.println("Quantidade de parâmetros: " + args.length);
        
        switch (args.length){
           case 7:
                type = "master";
                compath = args[0];//Linux
                logpath = args[1];
                arquivo = args[2];//Linux
                ip_master = args[3];//Linux
                sockets = Integer.parseInt(args[4]);//Linux
                elastic_grain = Integer.parseInt(args[5]);//Linux
                logname = args[6];
                System.out.println("Iniciando processamento tipo MASTER");
                System.out.println("Diretório compartilhado: " + compath);
                System.out.println("Arquivo que será processado: " + arquivo);
                System.out.println("Quantidade inicial de SLAVES: " + sockets);
                break;
            case 3:
                type = "slave";
                compath = args[0];//Linux
                logpath = args[1];                
                soapEndpoint = args[2];
                System.out.println("Inicando processamento tipo SLAVE");
                System.out.println("Diretório compartilhado: " + compath);
                break;
            default: 
                System.out.println("Parâmetros inválidos.");
                //type = "slave";
                System.exit(0);
        }                
        //Master srv = new Master(arquivo); //processa arquivo sozinho
        if (type.equalsIgnoreCase("master")){
            Master server = new Master(port, max_sockets, sockets, elastic_grain, compath, logpath, arquivo, ip_master, logname);
            server.computa();
            //server.resultados();
        } else if (type.equalsIgnoreCase("slave")){
            Slave client = new Slave(port, compath, logpath, soapEndpoint);
            client.computa();
        } else {System.out.println("tipo não reconhecido");}
    }
}
