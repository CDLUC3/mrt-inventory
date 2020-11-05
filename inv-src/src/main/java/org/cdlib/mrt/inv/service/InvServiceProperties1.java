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

package org.cdlib.mrt.inv.service;
import java.util.Properties;

import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;


import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.zoo.ZooHandler;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.queue.DistributedQueue;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TFileLogger;

/**
 * Base properties for Inv
 * @author  dloy
 */

public class InvServiceProperties1
{
    private static final String NAME = "InvServiceProperties";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;

    protected Properties serviceProperties = null;
    protected Properties setupProperties = null;
    protected Properties mergeProperties = null;
    protected File invService = null;
    protected File invInfo = null;
    protected DPRFileDB db = null;
    protected ZooManager zooManager = null;
    protected ZooHandler zooHandler = null;
    protected Thread zooHandlerThread = null;
    protected long zooPollTime = 120000;
    protected int zooThreadCnt = 1;
    protected LoggerInf logger = null;
    protected boolean shutdown = true;
    protected String storageBase = null;
    protected InvServiceState serviceState = null;

    public static InvServiceProperties1 getInvServiceProperties(Properties prop)
        throws TException
    {
        return new InvServiceProperties1(prop);
    }

    /**
     * Using the setupProp build a merge properties using the service properties
     * Setup the db and zookeeper functions from the merge properties
     * @param setupProp properties included in the configuration files
     * @throws TException 
     */
    protected InvServiceProperties1(Properties setupProp)
        throws TException
    {
        try {
            this.setupProperties = setupProp;
            
            String invServiceS = setupProp.getProperty("InvService");
            if (StringUtil.isEmpty(invServiceS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property: InvService");
            }
            invService = new File(invServiceS);
            if (!invService.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "inv service directory does not exist:"
                        + invService.getCanonicalPath());
            }
            File logDir = new File(invService, "log");
            if (!logDir.exists()) {
                logDir.mkdir();
            }
            
            invInfo = new File(invService, "inv-info.txt");
            if (!invInfo.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "inv-info.txt does not exist:"
                        + invService.getCanonicalPath());
            }
            InputStream fis = new FileInputStream(invInfo);
            serviceProperties = new Properties();
            serviceProperties.load(fis);
            
            System.out.println(PropertiesUtil.dumpProperties(NAME, serviceProperties));
            mergeProperties = new Properties();
            mergeProperties.putAll(setupProperties);
            mergeProperties.putAll(serviceProperties);
            System.out.println(PropertiesUtil.dumpProperties(NAME + "-merge", mergeProperties));
            
            logger = new TFileLogger("inv", logDir.getCanonicalPath() + '/', mergeProperties);
            
            db = new DPRFileDB(logger, mergeProperties);
            zooPollTime = 120000;
            String zooPollTimeS = this.mergeProperties.getProperty("zooPollMilli");
            if (StringUtil.isNotEmpty(zooPollTimeS)) {
                zooPollTime = Long.parseLong(zooPollTimeS);
            }
            
            zooManagerStartup();
            
            storageBase = mergeProperties.getProperty("storageURI");
            if (StringUtil.isEmpty(storageBase)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property: storageBase");
            }
            String zooThreadCntS = mergeProperties.getProperty("ZooThreadCount");
            if (StringUtil.isAllBlank(zooThreadCntS)) {
                zooThreadCnt = 2;
            } else {
                zooThreadCnt = Integer.parseInt(zooThreadCntS);
            }
            /*
            InvServiceState state = new InvServiceState(serviceProperties);
            invState = new InvState(invInfo);
            serviceStateManager
                    = InvServiceStateManager.getInvServiceStateManager(logger, invInfo);

            File adminDir = new File(invService, "admin");
            if (!adminDir.exists()) {
                adminDir.mkdir();
            }
            File rewriteEntryFile = new File(invService,"rewrite.txt");
            rewriteEntry = new RewriteEntry(rewriteEntryFile, logger);

            InvScheme scheme = state.retrieveServiceScheme();
            if (scheme != null) {
                scheme.buildNamasteFile(invService);
            }

            setPeriodicReport();
             */

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public DPRFileDB getNewDb()
        throws TException
    {
        return new DPRFileDB(logger, setupProperties);
    }

    public DPRFileDB getDb() {
        return db;
    }

    public void dbShutDown()
        throws TException
    {
        if (db == null) return;
        db.shutDown();
        db = null;
    }

    public void dbStartup()
        throws TException
    {
        if (db != null) return;
        db = getNewDb();
    }
    
    protected ZooManager getNewZooManager()
        throws TException
    {
        return ZooManager.getZooManager(logger, mergeProperties);
    }

    public ZooManager getZooManager() {
        return zooManager;
    }

    protected void zooManagerStartup()
        throws TException
    {
        if (zooManager != null) {
            zooManager.startup();
        } else {
            zooManager = getNewZooManager();
        }
    }

    public void zooHandlerStartup()
        throws TException
    {
        try {
            if ((zooHandlerThread != null) && zooHandlerThread.isAlive()) return;
            zooManagerStartup();
            zooHandler = ZooHandler.getZooHandler(zooManager, db, zooPollTime, zooThreadCnt, logger);
            zooHandlerThread = new Thread(zooHandler);
            zooHandlerThread.start();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }


    public void zooHandlerShutDown()
        throws TException
    {
        zooManager.setStatusShutdown();
        if (zooHandler != null) zooHandler.shutdown();
        while (true) {
            if (zooHandlerThread == null) break;
            if (zooHandlerThread.isAlive()) {
                try {
                    if (DEBUG) System.out.println("Retry zooHandlerThread shutdown");
                    Thread.sleep(500);
                } catch (Exception ex) { }
            } else {
                zooHandlerThread = null;
            }
        }
        zooManager.shutdown();
    }
    
    public Connection getConnection(boolean autoCommit)
        throws TException
    {
        if (db == null) return null;
        return db.getSingleConnection(autoCommit);
    }

    public DistributedQueue getDistributedQueue()
        throws TException
    {
        return zooManager.getQueue();
    }

    public String getStorageBase() {
        return storageBase;
    }
    

    public LoggerInf getLogger() {
        return logger;
    }

    public ServiceStatus getZookeeperStatus() {
        if (zooHandlerThread == null) return ServiceStatus.unknown;
        else if (zooManager.getZookeeperStatus() == ServiceStatus.shutdown) {
            if (zooHandlerThread.isAlive()) return ServiceStatus.shuttingdown;
            else return ServiceStatus.shutdown;
        } else if (zooManager.getZookeeperStatus() == ServiceStatus.running) {
            if (zooHandlerThread.isAlive()) return ServiceStatus.running;
            else return ServiceStatus.unknown;
        } else return ServiceStatus.unknown;
    }

    public ServiceStatus getDbStatus() {
        if (db == null) return ServiceStatus.shutdown;
        else return ServiceStatus.running;
    }
    
    /*
    public InvServiceState getInvServiceState()
    {
        InvServiceState serviceState = new InvServiceState(serviceProperties);
        serviceState.setDbStatus(getDbStatus());
        serviceState.setZookeeperStatus(getZookeeperStatus());
        serviceState.setSystemStatus(getSystemStatus());
        return serviceState;
    }
    */
    public ServiceStatus getSystemStatus() {
        if (getDbStatus() == ServiceStatus.running) {
            if (getZookeeperStatus() == ServiceStatus.running) return ServiceStatus.running;
            else return ServiceStatus.partial;
        } else {
            if (getZookeeperStatus() == ServiceStatus.running) return ServiceStatus.partial;
            else return ServiceStatus.shutdown;
            
        }
    }

}
