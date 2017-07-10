package edu.dlut.software.cagetian.server;

import edu.dlut.software.cagetian.FileInfo;
import edu.dlut.software.cagetian.storagenode.StorageNode;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by CageTian on 2017/7/6.
 */
public class FileServer implements Serializable{
    private ArrayList<StorageNode> node_info;
    private HashMap<String,FileInfo>file_info;
    private HashMap<StorageNode,Integer>node_statue;
    private String ip;
    private int port;
    FileServer (int port){
        this.port=port;
        node_statue=new HashMap<>();
        file_info=new HashMap<>();
        node_info=new ArrayList<>();
    }


    public void runNodeACKListenner() throws Exception {
            new Thread(new NodeService(this)).start();
            new Thread(new CheckNodeService(this)).start();
    }
    public static FileServer getServerInstance() {
        try {
            ObjectInputStream in = new ObjectInputStream
                    (new FileInputStream("D:\\HomeWork\\server_data\\file_server.obj"));
            return (FileServer)in.readObject();
        } catch (Exception e) {
            System.out.println("no object found");

        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        FileServer fs=new FileServer(6666);
        fs.runNodeACKListenner();
        ServerSocket serverSocket=new ServerSocket(fs.port);
        while(true){
            Socket socket=serverSocket.accept();
            System.out.println(socket.getInetAddress().toString()+socket.getPort()+" access in:");
            new Thread(new ClientService(socket,fs)).start();
        }

    }

    public ArrayList<StorageNode> getNode_info() {
        return node_info;
    }

    public void setNode_info(ArrayList<StorageNode> node_info) {
        this.node_info = node_info;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HashMap<String, FileInfo> getFile_info() {
        return file_info;
    }

    public void setFile_info(HashMap<String, FileInfo> file_info) {
        this.file_info = file_info;
    }

    public HashMap<StorageNode, Integer> getNode_statue() {
        return node_statue;
    }

    public void setNode_statue(HashMap<StorageNode, Integer> node_statue) {
        this.node_statue = node_statue;
    }
}
