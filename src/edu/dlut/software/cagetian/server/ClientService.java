package edu.dlut.software.cagetian.server;

import edu.dlut.software.cagetian.FileInfo;
import edu.dlut.software.cagetian.storagenode.StorageNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

/**
 * Created by CageTian on 2017/7/7.
 */
public class ClientService implements Runnable {
    private Socket socket;
    private FileServer fileServer;

    public ClientService(Socket socket, FileServer fileServer) {
        this.socket = socket;
        this.fileServer = fileServer;
    }

    @Override
    public void run() {
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            char request=dis.readChar();
            FileInfo fileInfo=null;
            switch (request){
                case 'u'://upload
                    String fileName=dis.readUTF();
                    System.out.println(fileName);
                    ArrayList<StorageNode> list=fileServer.getNode_info();
                    Collections.sort(list, new Comparator<StorageNode>() {
                        @Override
                        public int compare(StorageNode o1, StorageNode o2) {
                            return (int)(o1.getRestVolume()-o2.getRestVolume());
                        }
                    });
                    //判断文件大小
                    fileInfo=new FileInfo(UUID.randomUUID().toString(),list.get(0),list.get(1));
                    //exception node not enough
                    System.out.println(fileInfo.getFile_id());
                    break;
                case 'd'://download
                case 'r'://remove
                    String uuid_str=dis.readUTF();
                    fileInfo = fileServer.getFile_info().get(UUID.fromString(uuid_str));
                    break;
                default:
                    break;

            }

            oos.writeObject(fileInfo);
            oos.flush();
            dis.close();
            oos.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
