/*
 * 09/07/2015
 * SSHClient
  */
package communication;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client SSH to realize remote operations. 
 * @author viniciusfacco
 * 09/07/2015 - viniciusfacco
 *            - creation
 * 30/01/2017 - viniciusfacco
 *            - added ping method
 * 01/02/2017 - viniciusfacco
 *            - added createFile method
 */
public class SSHClient {
    
    private final String server;
    private final String user;
    private final String password;
    private final int port;
    
    private JSch jsch;
    private Session session;
    private UserInfo ui;
    
    public SSHClient(String srv, String usr, String pwd, int prt){
        server = srv;
        user = usr;
        password = pwd;
        port = prt;
        jsch = new JSch();  
        try {
            session = jsch.getSession(user, server, port);
        } catch (JSchException ex) {
            System.out.println("SSHClient: Error to get SSH session.");
            Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        ui = new ServerConnect(password);
        session.setUserInfo(ui);
        try {
            session.setServerAliveInterval(15 * 1000);
            session.setServerAliveCountMax(10000);
            session.setConfig("TCPKeepAlive", "yes");
        } catch (JSchException ex) {
            System.out.println("SSHClient: Error to configure TCPKeepAlive in the SSH session.");
            Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            session.connect();
        } catch (JSchException ex) {
            System.out.println("SSHClient: Error to connect SSH session.");
            Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void connect(){
        try {
            session.connect();
        } catch (JSchException ex) {
            Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void disconnect(){
        session.disconnect();
    }
    
    /**
     * Verify if a file exists in a remote directory.
     * @param remote_file_name File name
     * @param remote_dir Remote directory
     * @return
     */
    public boolean fileExists(String remote_file_name, String remote_dir){
        String retorno;
        retorno = "";
        
        if (!(session.isConnected())){
            try {
                System.out.println("fileExists: restarting connection.");
                session.connect();
            } catch (JSchException ex) {
                Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try{      
            //JSch jsch = new JSch();  
            //Session session = jsch.getSession(user, server, 22);
            //UserInfo ui = new ServerConnect(password);
            //session.setUserInfo(ui);
            //session.connect();
            String listar = "ls -l " + remote_dir;
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(listar);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    retorno = new String(tmp, 0, i);
                    //System.out.println(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();            
            return retorno.contains(remote_file_name); //se listagem conter arquivo de liberação
        }
        catch(Exception e){
            System.out.println(e);
        }
        return false;
    }
    
    /**
     * Remove a file in a remote directory.
     * @param file_name File name to be removed
     * @param remote_dir Remote directory where the file is (include "/" or "\\" in the end)
     * @return
     */
    public boolean deleteFile(String file_name, String remote_dir){
        if (!(session.isConnected())){
            try {
                System.out.println("deleteFile: restarting connection.");
                session.connect();
            } catch (JSchException ex) {
                Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try{      
            //JSch jsch = new JSch();  
            //Session session = jsch.getSession(user, server, 22);
            //UserInfo ui = new ServerConnect(password);
            //session.setUserInfo(ui);
            //session.connect();
            String remover = "rm " + remote_dir + file_name;
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(remover);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            channel.connect();
        }
        catch(Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }
    
    /**
     * Sends a file to a remote directory
     * @param filepath Full local file path including file name (source)
     * @param remote_dir Remote directory to send the file (target)
     * @return
     */
    public boolean sendFile(String filepath, String remote_dir){
        FileInputStream fis = null;
        
        if (!(session.isConnected())){
            try {
                System.out.println("sendFile: restarting connection.");
                session.connect();
            } catch (JSchException ex) {
                Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            //JSch jsch = new JSch();
            //Session session = jsch.getSession(user, server, 22);
            // username and password will be given via UserInfo interface.
            //UserInfo ui = new ServerConnect(password);
            //session.setUserInfo(ui);
            //session.connect();
            boolean ptimestamp = true;
            // exec 'scp -t destino' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remote_dir;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            try (
                OutputStream out = channel.getOutputStream()) {
                InputStream in = channel.getInputStream();
                channel.connect();
                if (checkAck(in) != 0) {
                    //gera_log(objname,"System.exit(0);");
                    channel.disconnect();
                    return false;
                }
                File _lfile = new File(filepath);
                if (ptimestamp) {
                    command = "T" + (_lfile.lastModified() / 1000) + " 0";
                    // The access time should be sent here,
                    // but it is not accessible with JavaAPI ;-<
                    command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                    out.write(command.getBytes());
                    out.flush();
                    if (checkAck(in) != 0) {
                        //gera_log(objname,"System.exit(0);");
                        channel.disconnect();
                        return false;
                    }
                }
                // send "C0644 filesize filename", where filename should not include '/'
                long filesize = _lfile.length();
                command = "C0644 " + filesize + " ";
                //realiza corte para pegar apenas o nome do arquivo
                if (filepath.lastIndexOf('\\') > 0) {
                    command += filepath.substring(filepath.lastIndexOf('\\') + 1);
                } else {
                    command += filepath;
                }
                command += "\n";
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    //gera_log(objname,"System.exit(0);");
                    channel.disconnect();
                    return false;
                }
                // send a content of arquivo
                fis = new FileInputStream(filepath);
                byte[] buf = new byte[1024];
                while (true) {
                    int len = fis.read(buf, 0, buf.length);
                    if (len <= 0) {
                        break;
                    }
                    out.write(buf, 0, len); //out.flush();
                }
                fis.close();
                fis = null;
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
                if (checkAck(in) != 0) {
                    //gera_log(objname,"System.exit(0);");
                    return false;
                }
            }
            channel.disconnect();
            //session.disconnect();
        } catch (JSchException | IOException e) {
            //gera_log(objname,e.toString());
            System.out.println(e.toString());
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ee) {
            }
        }
        return true;
    }
    
    public boolean createFile(String remote_file_name, String remote_dir, String content){
        String retorno;
        retorno = "";
        
        if (!(session.isConnected())){
            try {
                System.out.println("createFile: restarting connection.");
                session.connect();
            } catch (JSchException ex) {
                Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try{      
            //JSch jsch = new JSch();  
            //Session session = jsch.getSession(user, server, 22);
            //UserInfo ui = new ServerConnect(password);
            //session.setUserInfo(ui);
            //session.connect();
            String command = "echo \"" + content + "\" > " + remote_dir + remote_file_name;
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    retorno = new String(tmp, 0, i);
                    //System.out.println(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect(); 
            //System.out.println("createFile: " + retorno);
            return true; //se listagem conter arquivo de liberação
        }
        catch(Exception e){
            System.out.println(e);
        }
        return false;
    }
    
    //método auxiliar do método envia_arquivo(String filepath)
    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) {
            return b;
        }
        if (b == -1) {
            return b;
        }
        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                //gera_log(objname,sb.toString());
                System.out.println("Ack error: " + sb.toString());
            }
            if (b == 2) { // fatal error
                //gera_log(objname,sb.toString());
                System.out.println("Ack error: " + sb.toString());
            }
        }
        return b;
    }
    
    public boolean ping(String host){
        String retorno;
        retorno = "";
        
        if (!(session.isConnected())){
            try {
                System.out.println("ping: restarting connection.");
                session.connect();
            } catch (JSchException ex) {
                Logger.getLogger(SSHClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try{      
            //JSch jsch = new JSch();  
            //Session session = jsch.getSession(user, server, 22);
            //UserInfo ui = new ServerConnect(password);
            //session.setUserInfo(ui);
            //session.connect();
            String command = "ping -c 1 -w 1 " + host;
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    retorno = new String(tmp, 0, i);
                    //System.out.println(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();            
            //System.out.println("Retorno: " + retorno);
            if (retorno.contains("1 received")){
                return true;
            } else {
                return false;
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        return false;
    }

}
