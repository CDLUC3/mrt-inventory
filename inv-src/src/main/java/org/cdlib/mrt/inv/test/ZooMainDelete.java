/*
Copyright (c) 2011, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 *
- Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
- Neither the name of the University of California nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
**********************************************************/
package org.cdlib.mrt.inv.test;


import java.util.ArrayList;
import java.util.Properties;


import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import org.cdlib.mrt.queue.DistributedQueue;
import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;


/**
 * Basic manager for Queuing Service
 * @author mreyes
 */
public class ZooMainDelete
{

    private static final String NAME = "ZooManager";
    private static final String MESSAGE = NAME + ": ";
    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;
    //private LoggerInf logger = null;
    private Properties conf = null;
    //private Properties ingestProperties = null;
    private String queueConnectionString = "vat01.cdlib.org:2181";
    private String queueNode = "/distrib.vat01.1";
    private String zooBase = null;
    private ArrayList<String> m_admin = new ArrayList<String>(20);

    private ZooKeeper zooKeeper = null;
    
    //private DistributedQueue distributedQueue = null;
    
    public ZooMainDelete(String zooBase)
        throws TException
    {
	try {
            this.conf = conf;
            this.zooBase = zooBase;
            if (zooBase == null) zooBase = "";
            setZoo();
	} catch (TException tex) {
            tex.printStackTrace();
	    throw tex;
	}
    }
    
    
    private void setZoo()
        throws TException
    {
        try {
            closeZooKeeper();
            System.out.println("**Establishing new ZooKeeper:" + queueConnectionString);
            zooKeeper = new ZooKeeper(queueConnectionString, 10000, new Ignorer());

        } catch (Exception ex) {
            String msg = MESSAGE + "setZoo Exception:" + ex;
            throw new TException.GENERAL_EXCEPTION(msg);
        }
    }
    
    public DistributedQueue getQueue()
        throws TException
    {
        try {
                System.out.println("getQueue: "
                        + " - queueNode:" + queueNode
                        + " - zooBase:" + zooBase
                        + " - comb:" + queueNode + zooBase
                        
                        );
                return new DistributedQueue(zooKeeper, queueNode + zooBase, null);

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new TException(ex);
            }
    }
    
    public void process()
        throws TException
    {
        try {
            DistributedQueue queue = getQueue();;
            for (int i=0; true; i++) {
                try {
                    Item consumedItem = queue.consume();
                    if (consumedItem == null) {
                        System.out.println("null item");
                        break;
                    }
                    if (consumedItem.getId() == null) {
                        System.out.println("item " + i + " null");
                        continue;
                    }
                    queue.delete(consumedItem.getId());
                    
                } catch (Exception cex) {
                    System.out.println("C Exception:" + cex);
                    break;
                }
            }
            
            
        } catch (Exception ex) {
            String msg = MESSAGE + " Exception:" + ex;
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(msg)
                    ;
        } finally {
            try {
                closeZooKeeper();
                System.out.println("Close zoo");
            } catch (Exception ex) {
                System.out.println("close Exception:" + ex);
            }
        }
        
    }
    
    private void closeZooKeeper()
    {
        try {
            if (zooKeeper != null) zooKeeper.close();
        } catch (Exception ze) {}
        zooKeeper = null;
    }
    
    
    public String dump(String header, byte[] bytes, Properties[] rows)
        throws TException
    {
        StringBuffer buf = new StringBuffer(2000);
        try {
            buf.append("***" + header + "***" + NL);
            if ((bytes != null) && (bytes.length > 0)) {
                buf.append("********** bytes ************" + NL);
                String out = new String(bytes, "utf-8");
                buf.append(out + NL);
            }
            if ((rows != null) && (rows.length > 0)) {
                buf.append("********** rows ************" + NL);
                for (int i=0; i< rows.length; i++) {
                    buf.append(PropertiesUtil.dumpProperties("(" + i + "):", rows[i]) + NL);
                }
            }
            buf.append(NL +  "*********************************************" + NL);
            return buf.toString();
            
            
        } catch (Exception ex) {
            String msg = MESSAGE + " Exception:" + ex;
            
            throw new TException.GENERAL_EXCEPTION(msg);
        }
    }

    public String getZooBase() {
        return zooBase;
    }
    
    
   public class Ignorer implements Watcher {
       public void process(WatchedEvent event){
           if (event.getState().equals("Disconnected"))
               System.out.println("Disconnected: " + event.toString());
       }
   }
   
    /**
     * Main method
     */
    public static void main(String args[])
    {

        try {
            System.out.println(">>>Start ZooSmpleTest1");
            ZooMainDelete itemRun = new ZooMainDelete("");
            //ZooSimpleTest1 itemRun = new ZooSimpleTest1("/tst");
            itemRun.process();
            System.out.println(">>>End ZooSmpleTest1");

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }

}
