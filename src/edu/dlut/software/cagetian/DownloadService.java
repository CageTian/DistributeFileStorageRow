package edu.dlut.software.cagetian;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by CageTian on 2017/7/7.
 */
public class DownloadService implements Runnable{
    private Socket socket;
    private File directory;
    private Boolean isEncrypt=false;
    public DownloadService(Socket socket, String directory){
        this.socket=socket;
        this.directory= new File(directory);
    }

    public DownloadService(Socket socket, String directory, Boolean isEncrypt) {
        this.socket = socket;
        this.directory= new File(directory);
        this.isEncrypt = isEncrypt;
    }

    @Override
    public void run() {
        try {
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
                if(isEncrypt){
                    Tool.quickDecrypt(bytes);
                }
                fos.write(bytes, 0, length);
                fos.flush();
            }
            System.out.println("======== 文件下载成功 [File Name：" + fileName + "] [Size：" + fileLength + "] ========");
            fos.close();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
