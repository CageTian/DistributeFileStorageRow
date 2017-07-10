package edu.dlut.software.cagetian.storagenode;
import edu.dlut.software.cagetian.FileInfo;
import java.io.*;
import java.net.Socket;

/**
 * Created by CageTian on 2017/7/8.
 */
public class StorageClientService implements Runnable {
    private StorageNode storageNode;
    private Socket socket;

    /**
     * Constructor
     * @param storageNode
     * @param socket
     */
    public StorageClientService(StorageNode storageNode, Socket socket) {
        this.storageNode = storageNode;
        this.socket = socket;
    }

    /**
     * respond client download request
     * done
     * @throws IOException
     */
    public void clientDownload(DataInputStream dis) throws Exception {
        String file_uuid=dis.readUTF();
        String client_name=dis.readUTF();
        String file_path=storageNode.getRootFolder()+
                File.separatorChar+client_name+ File.separatorChar+file_uuid;
        File file=new File(file_path);
        if(file.isFile())
            send(socket,file);
    }



    /**
     * ask other Node for backup request
     *
     * @throws Exception
     */
    private void backUpToBNode(FileInfo fileInfo) throws Exception {
        StorageNode bNode=fileInfo.getSec_node();
        Socket node_socket=new Socket(bNode.getNodeIP(),bNode.getNodePort());
        ObjectOutputStream oos=new ObjectOutputStream(node_socket.getOutputStream());
        oos.writeChar('b');
        oos.writeObject(fileInfo);

        send(socket,fileInfo.getFile());
    }
    private void receiveBackUp(DataInputStream dis) throws Exception {
        FileInfo fileInfo=receive(socket,dis);
        System.out.println("======== 节点成功接收备份文件 [File Name：" +
                fileInfo.getFile_id() + "] [Size：" + fileInfo.getFile_size() + "] ========");
        storageNode.getFile_info_map().put(fileInfo.getFile_id(),fileInfo);
    }
    /**
     * respond client upload request
     *
     * @throws IOException
     */
    public void clientUpload(DataInputStream dis)throws Exception{
        FileInfo fileInfo=receive(socket,dis);
        System.out.println("======== 节点成功接收上传文件 [File Name：" +
                fileInfo.getFile_id() + "] [Size：" + fileInfo.getFile_size() + "] ========");
        storageNode.getFile_info_map().put(fileInfo.getFile_id(),fileInfo);
        backUpToBNode(fileInfo);
    }
    /**
     * respond client remove request
     *
     * @throws Exception
     */
    public void clientRemove(DataInputStream dis)throws Exception{
        String client_name=dis.readUTF();
        String file_uuid= dis.readUTF();
        FileInfo fileInfo=storageNode.getFile_info_map().get(file_uuid);
        if(fileInfo.getFile().delete()){
            storageNode.getFile_info_map().remove(file_uuid);
        }
        if(storageNode.equals(fileInfo.getMain_node())){
            Socket bSocket=new Socket(fileInfo.getSec_node()
                    .getNodeIP(),fileInfo.getSec_node().getFileServerPort());
            DataOutputStream dos =new DataOutputStream(bSocket.getOutputStream());
            dos.writeChar('r');
            dos.flush();
            dos.writeUTF(fileInfo.getFile_id());
            dos.flush();
            dos.close();
            bSocket.close();
        }
    }

    @Override
    public void run() {
        try {
            DataInputStream dis=new DataInputStream(socket.getInputStream());
            char ch=dis.readChar();
            switch (ch){
                case 'd':
                    clientDownload(dis);
                    break;
                case 'u':
                    System.out.println("upload");
                    clientUpload(dis);
                    break;
                case 'r':
                    clientRemove(dis);
                    break;
                case 'b':
                    receiveBackUp(dis);
                default:
                    break;//do somethings
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private FileInfo receive(Socket socket,DataInputStream dis)throws Exception{
        ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
        FileInfo fileInfo=(FileInfo)ois.readObject();
        String file_uuid = fileInfo.getFile_id();
        String client_name=fileInfo.getClient_name();
        File directory=new File(storageNode.getRootFolder()
                + File.separatorChar +client_name);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory.getAbsolutePath()+File.separatorChar + file_uuid);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
            fos.write(bytes, 0, length);
            fos.flush();
        }
        fileInfo.setFile(file);
        ois.close();
        fos.close();
        return fileInfo;
    }
    private void send(Socket socket, File file) throws Exception{
        FileInputStream fis=new FileInputStream(file);
        DataOutputStream dos=new DataOutputStream(socket.getOutputStream());

        // 文件名和长度
        dos.writeUTF(file.getName());
        dos.flush();
        dos.writeLong(file.length());
        dos.flush();

        // 开始传输文件
        System.out.println("======== 开始传输文件 ========");
        byte[] bytes = new byte[1024];
        int length;
        long progress = 0;
        while((length = fis.read(bytes, 0, bytes.length)) != -1) {
            dos.write(bytes, 0, length);
            dos.flush();
            progress += length;
            System.out.print("| " + (100*progress/file.length()) + "% |");
        }
        System.out.println();
        System.out.println("======== 文件传输成功 ========");
        fis.close();
        dos.close();
    }
}
