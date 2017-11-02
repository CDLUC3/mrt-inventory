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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.cdlib.mrt.inv.action.ProcessItem;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class ItemHandler
        implements Runnable
{

    protected static final String NAME = "ItemHandler";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    
    protected LoggerInf logger = null;
    protected BlockingQueue<ProcessItem> itemQueue = null;
    protected int threadCnt = 1;
    protected ExecutorService exService = null;
    
    public static ItemHandler getItemHandler(
            int threadCnt,
            BlockingQueue<ProcessItem> itemQueue,
            LoggerInf logger)
        throws TException
    {
        return new ItemHandler(threadCnt, itemQueue, logger);
    }
    
    protected ItemHandler(
            int threadCnt,
            BlockingQueue<ProcessItem> itemQueue,
            LoggerInf logger)
        throws TException
    {
        this.threadCnt = threadCnt;
        this.itemQueue = itemQueue;
        this.logger = (LoggerInf)notNull("logger", logger);
        exService =
            Executors.newFixedThreadPool(threadCnt);
        if (DEBUG) System.out.println("initialize ItemHandler - threadCnt=" + threadCnt);
    }
    
    private Object notNull(String header, Object object)
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
        }
    }

    public void process()
        throws TException
    {
    
        try {
            
            for (int i = 0; true; i++) {
                ProcessItem processItem = itemQueue.take();
                if (DEBUG) System.out.println(MESSAGE + "processItem=" 
                        + " - itemQueue.size=" + itemQueue.size()
                        + " - isShutdown=" + processItem.isShutdown()
                        + " - URL=" + processItem.getManifestURLS()
                        );
                if (processItem.isShutdown()) break;
                exService.execute(processItem);
            }
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            try {
                exService.shutdown();
            } catch (Exception ex) { }
        }
    }
}

