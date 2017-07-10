package edu.dlut.software.cagetian.server;
import edu.dlut.software.cagetian.storagenode.StorageNode;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
/**
 * Created by CageTian on 2017/7/7.
 */
public class NodeService implements Runnable {
    private FileServer fileServer;

    public NodeService(FileServer fileServer) {
        this.fileServer = fileServer;
    }

    @Override
    public void run() {
        try {
            byte[] buf = new byte[1024];
            String node_mes;
            DatagramSocket ds=new DatagramSocket(3000);
            DatagramPacket dp=new DatagramPacket(buf,1024);
            while(true){
                ds.receive(dp);
                node_mes=new String(dp.getData(),0,dp.getLength());
//                System.out.print(node_mes);
                String[]s_tmp=node_mes.split("#");
                if(s_tmp.length>=4){
                    StorageNode storageNode=new StorageNode(s_tmp[0],s_tmp[1],
                            Integer.parseInt(s_tmp[2]),Long.parseLong(s_tmp[3]));
                    fileServer.getNode_statue().put(storageNode,1);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
