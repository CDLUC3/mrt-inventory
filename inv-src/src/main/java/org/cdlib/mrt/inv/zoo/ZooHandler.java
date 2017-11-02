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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.inv.action.ProcessItem;
import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.core.ThreadHandler;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.zoo.ZooHandlerAbs;
import org.cdlib.mrt.zoo.ZooManager;

/**
 * Run fixity
 * @author dloy
 */
public class ZooHandler
        extends ZooHandlerAbs
        implements Runnable
{

    protected static final String NAME = "ZooHandler";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean STATUS = true;
    
    protected ThreadHandler threadHandler = null;
    protected DPRFileDB db = null;
    protected int threadCnt = 5;
    protected int esuCnt = 0;
    protected Exception saveException = null;
    protected long threadPollTime = 250;
    protected int restartCnt = 0;
    //protected final static int restartMax = 10000;
    protected final static int restartMax = 100;
 
    
    public static ZooHandler getZooHandler(
            ZooManager zooManager,
            DPRFileDB db,
            long zooPollTime,
            int threadCnt,
            LoggerInf logger)
        throws TException
    {
        return new ZooHandler(zooManager, db, zooPollTime, threadCnt, logger);
    }
    
    protected ZooHandler(
            ZooManager zooManager,
            DPRFileDB db,
            long zooPollTime,
            int threadCnt,
            LoggerInf logger)
        throws TException
    {
        super(zooManager, zooPollTime, logger);
        this.db = (DPRFileDB)notNull("db", db);
        if (threadCnt < 1) threadCnt = 1;
        this.threadCnt = threadCnt;
        startThreads();
        System.out.println(MESSAGE + "***threadCnt=" + threadCnt);
    }
    
    @Override
    public void shutdown()
        throws TException
    {
        try {
            if (threadCnt > 1) completeThreads();
            
        } catch (Exception ex) {
            System.out.println(MESSAGE + "WARNING shutdown:" + ex);
        }
    }
    
    
    @Override
    protected ProcessStatus processItem(
            long ijob,
            Item item)
        throws TException
    {
        
        try {
            if (DEBUG) System.out.println("processItem entered");
            if (zooManager.getZookeeperStatus() == ServiceStatus.shutdown) {
                return ProcessStatus.shutdown;
            }
            ProcessItem  processItem = null;
            try {
                processItem = ProcessItem.getProcessItem(
                    ijob,
                    zooQueue,
                    db,
                    item);
                
            } catch (Exception ex) {
                System.out.println(MESSAGE + "Exception:" + ex);
                ex.printStackTrace();
                removeItem(item);
                return ProcessStatus.format;
            }
            if (processItem.isShutdown()) return ProcessStatus.shutdown;
            ProcessStatus status = ProcessStatus.unknown;
            if (threadCnt == 1) {
                processItem.run();
                status = processItem.getProcessStatus();
            } else {
                status = runThread(processItem);
            }
            
            return status;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected ProcessStatus runThread(ProcessItem  processItem)
        throws TException
    {
        System.out.println("Start thread:" + processItem.getManifestURLS());
        restartCnt++;
        if (false && (restartCnt >= restartMax)) { // stub for now
            completeThreads();
            threadHandler = null;
            System.gc();
            startThreads();
        }
        
        if (zooManager.getZookeeperStatus() == ServiceStatus.shutdown) {
            return ProcessStatus.shutdown;
        }
        return threadHandler.runThread(processItem);
    }
    
    protected void completeThreads()
        throws TException
    {
        threadHandler.shutdown();
    }

    public void setThreadPollTime(long threadPollTime) {
        this.threadPollTime = threadPollTime;
    }
    
    private void startThreads()
        throws TException
    {
        threadHandler = ThreadHandler.getThreadHandler(threadPollTime, threadCnt, logger);
        threadHandler.setDebug(true);
        restartCnt = 0;
    }
}

