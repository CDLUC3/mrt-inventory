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

import java.util.Properties;

import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.zoo.ZooQueue;
import org.cdlib.mrt.utility.ZooCodeUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class AddZoo
        extends InvActionAbs
        implements Runnable
{

    protected static final String NAME = "AddZoo";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean LIST = true;
    
    
    protected ZooQueue zooQueue = null;
    protected ZooManager zooManager = null;
    protected Properties zooProp = null;
    protected boolean sent = false;
    protected ProcessStatus processStatus = null;
    protected byte[] bytes = null;
    protected TException saveTex = null;
 
    
    public static AddZoo getAddZoo(
            ZooQueue zooQueue,
            Properties zooProp)
        throws TException
    {
        LoggerInf logger = zooQueue.getLogger();
        return new AddZoo(zooQueue, zooProp, logger);
    }
    
    protected AddZoo(
            ZooQueue zooQueue,
            Properties zooProp,
            LoggerInf logger)
        throws TException
    {
        super(null, logger);
        this.zooQueue = zooQueue;
        this.zooProp = zooProp;
        String zooNode = zooQueue.getZooNode();
        if (LIST) {
            System.out.println(PropertiesUtil.dumpProperties(NAME, zooProp) 
                    + " - zooNode:" + zooNode
                    );
        }
        bytes = ZooCodeUtil.encodeItem(this.zooProp);
        zooManager = zooQueue.getZooManager();
            }

    public void run()
    {
            try {
                process();

            } catch (TException tex) {
                saveTex = tex;
                processStatus = ProcessStatus.exception;
            }

    }
   
    public void process()
       throws TException
    {
        try {
            ServiceStatus runStatus = zooManager.getZookeeperStatus();
            if (DEBUG) System.out.println("process runStatus:" + runStatus);
            if (runStatus != ServiceStatus.running) {
                processStatus = ProcessStatus.shutdown;
            }

            Exception saveEx = null;
            for (int i=0; i<3; i++) {
                try {
                    if (DEBUG) System.out.println("AddZoo size=" + bytes.length
                            + " - prop string:" + new String(bytes,"utf-8")
                            );
                    boolean submitStatue = zooQueue.getQueue().submit(bytes);
                    if (DEBUG) System.out.println("submitStatue=" + submitStatue);
                    processStatus = ProcessStatus.completed;
                    return;

                } catch (Exception ex) {
                    if (DEBUG) System.out.println(MESSAGE + "Exception:" + ex);
                    zooQueue.processException(ex);
                    continue;
                }
            }
            throw new TException.EXTERNAL_SERVICE_UNAVAILABLE(saveEx);
            
        } catch (TException tex) {
            tex.printStackTrace();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public boolean isSent()
    {
        return sent;
    }
}

