package edu.dlut.software.cagetian.server;

import edu.dlut.software.cagetian.storagenode.StorageNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by CageTian on 2017/7/7.
 */
public class CheckNodeService implements Runnable {
    private FileServer fileServer;
    public CheckNodeService(FileServer fileServer) {
        this.fileServer=fileServer;
    }

    @Override
    public void run() {
        try {
            while (true){
                HashMap<StorageNode,Integer> map= fileServer.getNode_statue();
                ArrayList<StorageNode> node_list=fileServer.getNode_info();

                for(StorageNode k:map.keySet()){
                    if(map.get(k)==1){
                        map.put(k,0);
                        if(!node_list.contains(k)) {
                            node_list.add(k);
                        }
                    }
                    else if(map.get(k)==0){
                        fileServer.getNode_info().remove(k);
                    }
                }//statue delete items
                Thread.sleep(20000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
