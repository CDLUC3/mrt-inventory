
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
import org.cdlib.mrt.inv.service.InventoryConfig;
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

public class InvMainZooHandler
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
            service = InvService.getInvService(InventoryConfig.useYaml());
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
            add(storeLoadProp, service, zooQueue);
            
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
            service.startup();
            System.out.println("Run thread sleep: 30");
            Thread.sleep(45000);
            add(storeLoadProp, service, zooQueue);
            System.out.println("Closing sleep: 3 minutes");
            Thread.sleep(180000);

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
    
    public static void add(Properties storeLoadProp, InvService service, ZooQueue zooQueue)
    {        
        try {
            System.out.println("***** ADD *****");
            for (int i=1; true; i++) {
                String manifestURL = storeLoadProp.getProperty("manifestURL." + i);
                if (manifestURL == null) break;
                Properties loadProp = new Properties();
                loadProp.setProperty("manifestURL", manifestURL);
                
                try {
                    service.addZoo(loadProp, zooQueue);
                    System.out.println("****************> ZOO ADD:" + manifestURL);
                } catch (Exception ex) {
                    System.out.println("Exception:" + ex);
                    continue;
                }
            }

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
                
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
