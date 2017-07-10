package edu.dlut.software.cagetian;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by CageTian on 2017/7/7.
 */
public class UploadService implements Runnable {
    private String peer_ip;
    private int port;
    private FileInfo fileInfo;
    private Boolean isEncrypt=false;
    @Override
    public void run() {
        try {
            Socket socket=new Socket(peer_ip,port);
            FileInputStream fis=new FileInputStream(new File(fileInfo.getFile_name()));
            DataOutputStream dos=new DataOutputStream(socket.getOutputStream());

            // 文件名和长度
            dos.writeUTF(fileInfo.getFile_id());
            dos.flush();
            dos.writeLong(fileInfo.getFile_size());
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
                System.out.print("| " + (100*progress/fileInfo.getFile_size()) + "% |");
            }
            System.out.println();
            System.out.println("======== 文件传输成功 ========");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
