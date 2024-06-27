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
package org.cdlib.mrt.inv.zoo;

import java.sql.Connection;
import java.util.Properties;
import java.util.Random;

import org.cdlib.mrt.core.ServiceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.inv.zoo.ZooManager;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.log.utility.AddStateEntryGen;
import org.cdlib.mrt.utility.TException;
import org.apache.zookeeper.ZooKeeper;
import org.cdlib.mrt.inv.action.AddZoo;
import org.cdlib.mrt.inv.action.InvActionAbs;
import org.cdlib.mrt.inv.action.SaveObject;
import org.cdlib.mrt.zk.Job;
import org.json.JSONObject;

/**
 * Run fixity
 * @author dloy
 */
public class ProcessJob
        extends InvActionAbs
        implements Runnable
{

    protected static final String NAME = "ProcessJob";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean STATUS = true;
 
    
    protected Job job = null;
    protected TException saveTex = null;
    protected ProcessStatus processStatus = null;
    protected JSONObject prop = null;
    protected String manifestURLS = null;
    protected DPRFileDB db = null;
    protected Connection connection = null;
    protected boolean shutdown = false;
    protected long ijob = 0;
    protected SaveObject saveObject = null;
    protected Random rn = new Random();
    protected ZooManager zooManager = null;
     
    public static ProcessJob getShutdownProcessItem()
        throws TException
    {
        return new ProcessJob(true);
    } 
    
    protected ProcessJob(boolean shutdown)
        throws TException
    {
        super(null, null);
        this.shutdown = shutdown;
    }
    
    public static ProcessJob getProcessJob(
            Job job,
            InventoryConfig config)
        throws TException
    {
        return new ProcessJob(job, config.getZooManager(),config.getDb());
    }
    
    public static ProcessJob getProcessJob(
            Job job,
            ZooManager zooManager,
            DPRFileDB db)
        throws TException
    {
        return new ProcessJob(job, zooManager, db);
    }
    
    protected ProcessJob(
            Job job,
            ZooManager zooManager,
            DPRFileDB db)
        throws TException
    {
        super(null, zooManager.getLogger());
        
        this.job = job;
        this.zooManager = zooManager;
        this.db = db;
        validate();
        if (zooManager.getZookeeperStatus() == ServiceStatus.shutdown) {
            this.shutdown = true;
        }
    }
    
    private void validate()
        throws TException
    {
        if (zooManager == null) throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "zooManager null");
        if (job == null) throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "job null");
        
	AddZoo.jp("ProcessJob.validate", job);
        manifestURLS = job.inventoryManifestUrl();
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
                    + " - id:" + job.id()
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
        int retry = 0;
        try {
            ZooKeeper zooKeeper = zooManager.getZooKeeper();
            if (connection == null) return;
            if (DEBUG) System.out.println(MESSAGE + "begin process");
            //saveObject.process();
            //job.setStatus(zooKeeper, job.status().stateChange(JobState.Recording));
            retry = saveObjectRetry(4);
            jobOK();
            
        } catch (TException tex) {
            jobFails();
            
        } catch (Exception tex) {
            jobFails();
            
        }finally {
            try {
                connection.close();
            } catch (Exception ex) { }
            if (STATUS) System.out.println(MESSAGE + "process "
                    + " - manifestURLS=" + manifestURLS
                    + " - processStatus=" + processStatus
                    );
            AddStateEntryGen logStateEntry = saveObject.getLogStateEntry();
            if (logStateEntry == null) {
                ThreadContext.put("manifestURL", manifestURLS);
                ThreadContext.put("processStatus", processStatus.toString());
                ThreadContext.put("attempts", "" + retry);
                LogManager.getLogger().info(MESSAGE);
                return;
            }
            logStateEntry.setStatus(processStatus.toString());
            logStateEntry.setAttempts(retry);
            Properties prop = new Properties();
            prop.setProperty("manifestURL", manifestURLS);
            logStateEntry.setProperties(prop);
            logStateEntry.addLogStateEntry("InvJSON");
        }
    }
    
    protected int saveObjectRetry(int retry)
       throws TException
    {
        for (int doit=1; true; doit++) {
            try {
                if (false && (doit < 3)) { // force test
                    throw new TException.SQL_EXCEPTION("com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction - Exception:com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException: Deadlock found when trying to get lock; try restarting transaction");
                }
                saveObject.process();
                if (doit>1) logger.logMessage("SaveObjectRetry OK - manifest:" + manifestURLS, 2);
                return doit;
            
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
                        return doit;
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
    
    public void jobFails()
       throws TException
    {
        try {
            processStatus = ProcessStatus.failed;
            job.setStatus(zooManager.getZooKeeper(), job.status().fail());
            job.unlock(zooManager.getZooKeeper());
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void jobOK()
       throws TException
    {
        try {
            processStatus = ProcessStatus.completed;
            job.setStatus(zooManager.getZooKeeper(), job.status().success());
            job.unlock(zooManager.getZooKeeper());
            
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
                if (zooManager.getZookeeperStatus() == ServiceStatus.shutdown) {
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
