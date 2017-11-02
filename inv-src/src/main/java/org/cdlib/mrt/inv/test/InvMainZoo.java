
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.action.ProcessItem;
import org.cdlib.mrt.zoo.ItemInfo;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.zoo.ZooQueue;
import org.cdlib.mrt.zoo.ZooUtil;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;

/**
 * Load manifest.
 * @author  dloy
 */

public class InvMainZoo
{
    private static final String NAME = "InvMain";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        InvService service = null;

        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/InvMainZoo.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            service = InvService.getInvService(storeLoadProp);
            ZooManager zooManager = service.getZooManager();
            ZooQueue zooQueue = ZooQueue.getZooQueue(zooManager);
            for (int b=0; b<4; b++) {
                try {
                    zooQueue.getQueue().cleanup((byte)b);
                    System.out.println("cleanup:" + b);
                } catch (Exception e) { 
                    System.out.println("exception:" + b);
                }
            }
            
            for (int i=1; true; i++) {
                String manifestURL = storeLoadProp.getProperty("manifestURL." + i);
                if (manifestURL == null) break;
                Properties loadProp = new Properties();
                loadProp.setProperty("manifestURL", manifestURL);
                System.out.println("****************> PROCESSING " + manifestURL);
                
                try {
                    service.addZoo(loadProp, zooQueue);
                    System.out.println("InvMainZoo added");
                } catch (Exception ex) {
                    System.out.println("Exception:" + ex);
                    continue;
                }
            }
            
        if (DEBUG) {
            Item item = zooQueue.getQueue().peek();

            try {
                System.out.println("InvMainZoo data:" + new String(item.getData(), "utf-8"));
                System.out.println("InvMainZoo bytes:" + new String(item.getBytes(), "utf-8"));
            } catch (Exception e) { }
            Properties prop = ZooUtil.decodeItem(item.getData());
            System.out.println(PropertiesUtil.dumpProperties("InvMainZoo", prop));
        }
        
            List<ItemInfo> infos = zooQueue.browse("", (byte)-1, 20);
            System.out.println("infos size=" + infos.size());
            for (ItemInfo info : infos) {
                System.out.println(info.dump("InvMainZoo"));
            }
            System.out.println("Enter ProcItem loop");
            while (true) {
                Item procItem = null;
                try {
                    procItem = zooQueue.getQueue().consume();    
                    if (procItem == null) break;
                } catch (Exception ex) {
                    System.out.println("Loop ex:" + ex);
                    break;
                }
                System.out.println("procItem id=" + procItem.getId());
                Properties prop = ZooUtil.decodeItem(procItem.getData());
                System.out.println(PropertiesUtil.dumpProperties("procItem", prop));
                service.processItem(procItem, zooQueue);
                System.out.println("InvMainZoo: processed:" + procItem.getId());
             
            }
            service.shutdown();

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
                
        } finally {
            if (service != null) {
                try {
                    service.shutdown();
                } catch (Exception ex) { }
            }
        }
    }


    protected static String get(Properties prop, String key)
        throws TException
    {
        String retVal = prop.getProperty(key);
        if (StringUtil.isEmpty(retVal)) {
            
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property:" + key);
        }
        return retVal;
    } 
}
