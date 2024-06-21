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



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
//import org.cdlib.mrt.queue.DistributedQueue;
//import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.zk.Job;
import org.cdlib.mrt.zk.JobState;

/**
 * Run fixity
 * @author dloy
 */
public abstract class ZooHandlerAbs
        implements Runnable
{

    protected static final String NAME = "ZooHandlerAbs";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DEBUG_EX = true;
    protected static final boolean STATUS = true;
    
    protected ZooManager zooManager = null;
    protected long pollTime = 30000;
    protected LoggerInf logger = null;
    protected int esuCnt = 0;
    protected Exception saveException = null;
    
 

    private static final Logger log4j = LogManager.getLogger();

    protected ZooHandlerAbs (
            ZooManager zooManager,
            long pollTime,
            LoggerInf logger)
        throws TException
    {
        this.zooManager = (ZooManager)notNull("zooManager", zooManager);
        this.logger = (LoggerInf)notNull("logger", logger);
        this.pollTime = pollTime;
        if (pollTime < 5000) pollTime = 30000;
    }    
    
    public static Object notNull(String header, Object object)
        throws TException
    {
        if (object == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + header + " null");
        }
        return object;
    }

    public void run()
    {
        try {
            process();
            
        } catch (Exception ex) {
            saveException = ex;
            return;
        }
    }

    public void process()
        throws TException
    {
        int cleCnt = 1;
        for (long ijob=0; true; ijob++) {
            if (zooManager.getZookeeperStatus() == ServiceStatus.shutdown) {
                break;
            }
            Job job = null;
            try { 
                
                try {
                    job = Job.acquireJob(zooManager.getZooKeeper(), JobState.Recording);
                    if (job == null) {
                        System.out.println(MESSAGE + "No item founc");
                        Thread.sleep(pollTime);
                        if (DEBUG) System.out.println("consume continue");
                        continue;
                    }
                    cleCnt = 0;
                    
                } catch (java.util.NoSuchElementException nsee) {
                    if (STATUS) System.out.println("ZooHandler sleep:" + pollTime);
                    Thread.sleep(pollTime);
                    if (DEBUG) System.out.println("consume continue");
                    continue;
                }
                log4j.info("Recording job found:" + job.id());
                ProcessStatus status = processJob(job);
                if (DEBUG) System.out.println("ZooHandler Status:" + status.toString());
                if ((status == ProcessStatus.unknown) || (status == ProcessStatus.shutdown)) {
                    System.out.println("Job status unknown");
                }
                esuCnt = 0;
                
            } catch (KeeperException.SessionExpiredException see) {
                see.printStackTrace(System.err);
                System.err.println("[warn] RecordConsumeData" + MESSAGE + "Session expired.  Attempting to recreate session.");
                try {
                    zooManager.setZoo();

                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.out.println("[error] Consuming queue data: Could not recreate session.");
                }
            
            } catch (KeeperException.ConnectionLossException cle) {
                cle.printStackTrace(System.err);
                System.err.println("[warn] RecordConsumeData" + MESSAGE + "Connection loss.  Attempting to reconnect.");
                try {
                    zooManager.setZoo();

                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    System.out.println("[error] Consuming queue data: Could not reconnect.");
                }
                
            } catch (Exception ex) {
                    handleException(ex, job);
            }
        }
    }
    
    public void shutdown()
        throws TException
    {
        try {
            //may be overriddent
            
        } catch (Exception ex) {
        }
    }
    
    protected void handleException(Exception passedException, Job job)
        throws TException
    {
        if (DEBUG_EX) {
            System.out.println("INFO ZooHandlerAbs-Exception:" + passedException);
            passedException.printStackTrace();
        }
        try {
            job.setStatus(zooManager.getZooKeeper(), job.status().success());
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected abstract ProcessStatus processJob(
            Job job)
        throws TException;
    
    
    
    protected String dumpJob(Job job)
    {   
        try {
            if (job == null) return "Job null";
            return ZooUtil.dumpJob("ZooHandler-Job", job);
            
        } catch (Exception ex) {
            return "Exception:" + ex;
        }
        
    }
}

