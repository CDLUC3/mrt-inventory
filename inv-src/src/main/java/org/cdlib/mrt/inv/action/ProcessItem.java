/******************************************************************************
Copyright (c) 2005-2012, Regents of the University of California
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
*******************************************************************************/
package org.cdlib.mrt.inv.action;

import java.sql.Connection;
import java.util.Properties;
import java.util.Random;

import org.cdlib.mrt.zoo.ItemInfo;
import org.cdlib.mrt.queue.DistributedQueue;
import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.core.ServiceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.zoo.ZooQueue;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.ZooCodeUtil;

/**
 * Run fixity
 * @author dloy
 */
public class ProcessItem
        extends InvActionAbs
        implements Runnable
{

    protected static final String NAME = "ProcessItem";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean STATUS = true;
 
    protected ZooQueue zooQueue = null;
    protected ZooManager zooManager = null;
    protected Item item = null;
    protected ItemInfo info = null;
    protected TException saveTex = null;
    protected ProcessStatus processStatus = null;
    protected Properties prop = null;
    protected String manifestURLS = null;
    protected DPRFileDB db = null;
    protected Connection connection = null;
    protected boolean shutdown = false;
    protected long ijob = 0;
    protected SaveObject saveObject = null;
    protected Random rn = new Random();
     
    public static ProcessItem getShutdownProcessItem()
        throws TException
    {
        return new ProcessItem(true);
    } 
    
    protected ProcessItem(boolean shutdown)
        throws TException
    {
        super(null, null);
        this.shutdown = shutdown;
    }
    
    public static ProcessItem getProcessItem(
            long ijob,
            ZooQueue zooQueue,
            DPRFileDB db,
            Item item)
        throws TException
    {
        LoggerInf logger = zooQueue.getLogger();
        return new ProcessItem(ijob, zooQueue, db, item, logger);
    }
    
    protected ProcessItem(
            long ijob,
            ZooQueue zooQueue,
            DPRFileDB db,
            Item item,
            LoggerInf logger)
        throws TException
    {
        super(null, logger);
        this.ijob = ijob;
        this.zooQueue = zooQueue;
        this.item = item;
        this.db = db;
        validate();
        if (zooQueue.getZooManager().getZookeeperStatus() == ServiceStatus.shutdown) {
            this.shutdown = true;
        }
    }
    
    private void validate()
        throws TException
    {
        if (zooQueue == null) throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "zooQueue null");
        if (item == null) throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "item null");
        info = new ItemInfo();
        info.setWithItem(zooQueue.getZooNode(), item.getId(), item);
        byte[] bytes = item.getData();
        prop = ZooCodeUtil.decodeItem(bytes);
        manifestURLS = prop.getProperty("manifestURL");
        if (manifestURLS == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "manifestURL null");
        }
        connection = getNewConnection();
        if (connection == null) return;
        if (DEBUG) System.out.println(MESSAGE + "connection returned");
        getSaveObjectRetry404(3);
        
    }

    public void run()
    {
            try {
                process();
                String threadName = Thread.currentThread().getName();
                String pmsg = "ProcessItem(" + ijob + " - " + threadName + "):"
                    + " - id:" + item.getId()
                    + " - manifestURLS:" + manifestURLS
                    + " - status:" + processStatus;
                logger.logMessage(pmsg, 1, true);
                if (DEBUG) System.out.println(pmsg);
                
            } catch (TException tex) {
                saveTex = tex;
                processStatus = ProcessStatus.exception;
            }

    }
   
    public void process()
       throws TException
    {
        try {
            if (connection == null) return;
            if (DEBUG) System.out.println(MESSAGE + "begin process");
            //saveObject.process();
            saveObjectRetry(4);
            processStatus = ProcessStatus.completed;
            setQueue(Item.COMPLETED);
            
        } catch (TException tex) {
            resetQueue();
            
        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
            if (STATUS) System.out.println(MESSAGE + "process "
                    + " - manifestURLS=" + manifestURLS
                    + " - processStatus=" + processStatus
                    );
            ThreadContext.put("manifestURL", manifestURLS);
            ThreadContext.put("processStatus", processStatus.toString());
            LogManager.getLogger().info(MESSAGE);
        }
    }
    
    protected void saveObjectRetry(int retry)
       throws TException
    {
        for (int doit=1; true; doit++) {
            try {
                if (false && (doit < 3)) { // force test
                    throw new TException.SQL_EXCEPTION("com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction - Exception:com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction");
                }
                saveObject.process();
                if (doit>1) logger.logMessage("SaveObjectRetry OK - manifest:" + manifestURLS, 2);
                return;
            
            } catch (TException tex) {
                
                if (doit >= retry) {
                    logger.logMessage(MESSAGE +"SaveObjectRetry() FAILS - manifest:" + manifestURLS + " - Exception:" + tex, 2);
                    throw tex;
                }
                
                int sleep = 10000;
                if (tex instanceof TException.REQUESTED_ITEM_NOT_FOUND) {
                    sleep = (1000*doit) + 5000;
                    
                } else if (tex instanceof TException.SQL_EXCEPTION) {
                    if (!tex.toString().contains("MySQLTransactionRollbackException")) {
                        throw tex;
                    }
                    
                    int rnval = rn.nextInt(20) + 1;
                    sleep = (rnval * 6000 * doit) + 60000;
                    
                } else {
                    logger.logMessage(MESSAGE +"SaveObjectRetry FAILS - manifest:" + manifestURLS + " - Exception:" + tex, 2);
                    throw tex;
                }
                
                
                try {
                    reInit();
                    connection = getNewConnection();
                    if (connection == null) {
                        logger.logMessage(MESSAGE +"SaveObjectRetry connection null - manifest:" + manifestURLS + " - Exception:" + tex, 2);
                        return;
                    }
                    getSaveObjectRetry404(3);
                    System.out.println("saveObjectRetry Connection reset");
                    
                } catch (Exception ex) { 
                    logger.logMessage(MESSAGE +"SaveObjectRetry reInit FAILS - manifest:" + manifestURLS + " - Exception:" + tex, 2);
                    throw new TException(ex);
                }

                System.out.println(MESSAGE + "saveObjectRetry retry(" + doit + "):"
                        + " - sleep=" + sleep
                        + " - Exception=" + tex);
                
                logger.logMessage(MESSAGE +"SaveObjectRetry[" + doit + "," + sleep + "]:" + tex, 2);
                try {
                    Thread.sleep(sleep);
                } catch (Exception ex) { }
            }
        } 
    }
    
    protected void reInit()
    {
        if (saveObject != null) {
            try {
                Connection killConnect = saveObject.getConnection();
                killConnect.close();
            } catch (Exception tmpEx) { }
            saveObject = null;
        }
        try {
            if (connection != null) connection.close();
        } catch (Exception tmpEx) { }
        connection = null;
    }
    
    public void getSaveObjectRetry404(int retry)
       throws TException
    {
        for (int doit=1; true; doit++) {
            try {
                saveObject = SaveObject.getSaveObject(manifestURLS, connection, logger);
                return;
            
            } catch (TException.REQUESTED_ITEM_NOT_FOUND tex) {
                if (doit > retry) {
                    throw tex;
                }
                int sleep = (1000*doit) + 5000;
                
                try {
                    if (connection.isClosed()) {
                        System.out.println("getSaveObjectRetry404 Connection reset");
                        connection = getNewConnection();
                    }
                } catch (Exception ex) { 
                    throw new TException(ex);
                }
               
                System.out.println(MESSAGE + "getSaveObjectRetry404 retry(" + doit + "):"
                        + " - sleep=" + sleep
                        + " - Exception=" + tex);
                try {
                    Thread.sleep(sleep);
                } catch (Exception ex) { }
            }
        } 
    }
    
    public void resetQueue()
       throws TException
    {
        try {
            setQueue(Item.FAILED);
            processStatus = ProcessStatus.failed;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
   
    public void setQueue(byte itemStatus)
       throws TException
    {
        //CONSUMED = (byte) 1;
        //DELETED  = (byte) 2;
        //FAILED   = (byte) 3;
        //COMPLETED= (byte) 4;
        try {
            DistributedQueue queue = zooQueue.getQueue();
            queue.updateStatus(item.getId(), item.getStatus(), itemStatus);
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public ProcessStatus getProcessStatus() {
        return processStatus;
    }

    public String getManifestURLS() {
        return manifestURLS;
    }

    public boolean isShutdown() {
        return shutdown;
    }
        
    protected Connection getNewConnection()
        throws TException
    {
        
        Connection connection = null;
        long sleep = 60000;
        boolean sleepConnection = false;
        for (int i=0; i<300; i++) {
            try {
                connection = db.getConnection(false);
                if (connection.isValid(3)) return connection;
                System.out.println("Connection not valid");
                try {
                    connection.close();
                } catch (Exception ex) {}    
                if (DEBUG) System.out.println("Invalid connection sleep");
                sleepConnection = true;

            } catch (Exception connEx) {
                sleepConnection = true;
            }
            if (sleepConnection) {
                if (zooQueue.getZooManager().getZookeeperStatus() == ServiceStatus.shutdown) {
                    return null;
                }   
                sleepConnection = false;
                try {   
                    if (DEBUG) System.out.println("Connection sleep:" + sleep);
                    Thread.sleep(sleep);
                } catch (Exception ex) {}
            }
            String msg = "Attempt reconnect:" + i;
            System.out.println(msg);
            logger.logError(msg, 1);
        }
        throw new TException.SQL_EXCEPTION("Database unavailable");
    }
    
}

