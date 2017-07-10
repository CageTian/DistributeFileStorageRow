package edu.dlut.software.cagetian.client;
import edu.dlut.software.cagetian.FileInfo;
import edu.dlut.software.cagetian.Tool;
import edu.dlut.software.cagetian.storagenode.StorageNode;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by CageTian on 2017/7/6.
 */
public class FileClient  {
    private int port;
    private String ip;
    private String serverIP;
    private int server_port;
    private String client_name;

    public void upload(String file_path){
        try {
            File file=new File(file_path);
            Socket socket = new Socket(serverIP, server_port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInfo fileInfo=getServerMes(socket,dos,'u',file_path);

            System.out.println(fileInfo);
            //connect main node
            socket=new Socket(fileInfo.getMain_node().getNodeIP(),
                    fileInfo.getMain_node().getNodePort());
//            dos=new DataOutputStream(socket.getOutputStream());
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);

            // 文件名和长度
            oos.writeChar('u');
            oos.flush();
            oos.writeObject(fileInfo);
            oos.flush();
//            dos.writeUTF(file.getName());
//            dos.flush();
//            dos.writeLong(file.length());
//            dos.flush();
            // 开始传输文件
            System.out.println("======== 开始传输文件 ========");
            byte[] bytes = new byte[1024];
            int length;
            long progress = 0;
            while ((length = fis.read(bytes, 0, bytes.length)) != -1) {
//                bytes = Tool.quickEncrypt(bytes);
                oos.write(bytes, 0, length);
                oos.flush();
                progress += length;
                System.out.print("| " + (100 * progress / file.length()) + "% |");
            }
            System.out.println();
            System.out.println("======== 文件传输成功 ========");
            oos.close();
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private FileInfo getServerMes(Socket socket,DataOutputStream dos,char ch,String uName)throws Exception{

        ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
        dos.writeChar(ch);
        dos.flush();
        dos.writeUTF(uName);
        dos.flush();
        FileInfo fileInfo=(FileInfo)ois.readObject();
        fileInfo.setClient_name(client_name);
        dos.close();
        ois.close();
        socket.close();
        return fileInfo;

    }
    public void remove(String uuid) throws Exception {
        Socket socket=new Socket(serverIP,server_port);
        DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
        FileInfo fileInfo=getServerMes(socket,dos,'r',uuid);

        StorageNode firstNode=fileInfo.getMain_node();
        socket=new Socket(firstNode.getNodeIP(),firstNode.getNodePort());
        dos=new DataOutputStream(socket.getOutputStream());
        dos.writeChar('r');
        dos.flush();
        dos.writeUTF(fileInfo.getFile_id());
        dos.flush();
        dos.close();
        socket.close();
    }
    public void download(String uuid,String file_path) throws Exception {
        File directory=new File(file_path);
        Socket socket = new Socket(serverIP, server_port);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        FileInfo fileInfo=getServerMes(socket,dos,'d',uuid);
        socket=new Socket(fileInfo.getMain_node().getNodeIP(),
                fileInfo.getMain_node().getNodePort());
        dos=new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(uuid);
        dos.flush();
        dos.writeUTF(client_name);
        dos.flush();
        dos.close();


        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String fileName = dis.readUTF();
        Long fileLength = dis.readLong();
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                Tool.quickDecrypt(bytes);
            fos.write(bytes, 0, length);
            fos.flush();
        }
        System.out.println("======== 文件下载成功 [File Name：" + fileName + "] [Size：" + fileLength + "] ========");
        fos.close();
        dis.close();
        socket.close();
    }
    FileClient(int port){
        this.port=port;
        try {
            ip=InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("please check the internet");
        }
    }
    FileClient(File f) throws IOException {
        getProperties(f);
    }


    public static void main(String[] args) throws Exception {
        if(args.length<2)
            System.out.println("usage:\n-d file_uuid --------- download file in server" +
                    "\t\n-r file_uuid -------- delete file in server" +
                    "\t\n-u file_path -------- upload file to server");
        else {
            FileClient fileClient=new FileClient(new File("resource//client1.properties"));
            switch (args[0]){
                case "-d":
                    fileClient.download(args[1],args[2]);
                    break;
                case "-r":
                    fileClient.remove(args[1]);
                    break;
                case "-u":
                    fileClient.upload(args[1]);
                    break;
                default:
                    System.out.println("no param like "+args[0]);
                    break;
            }
        }

//        System.out.println(new FileClient(1234));
    }
    private void getProperties(File prop_file) throws IOException {
        Properties pps = new Properties();
        InputStream in = new BufferedInputStream(new FileInputStream(prop_file));
        pps.load(in);
        serverIP=pps.getProperty("FileServerIP");
        server_port=Integer.parseInt(pps.getProperty("FileServerPort"));
        client_name=pps.getProperty("ClientName");
        ip=pps.getProperty("ClientIP");
        port=Integer.parseInt(pps.getProperty("ClientPort"));
    }
}
