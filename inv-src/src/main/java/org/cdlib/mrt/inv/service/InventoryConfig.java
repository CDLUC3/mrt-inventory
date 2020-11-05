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
import java.util.LinkedHashMap;


import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.zoo.ZooHandler;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.queue.DistributedQueue;
import org.cdlib.mrt.tools.SSMConfigResolver;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.tools.YamlParser;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerAbs;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Base properties for Inv
 * @author  dloy
 */

public class InventoryConfig
{
    private static final String NAME = "InventoryConfig";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;

    protected Properties zooProperties = null;
    protected JSONObject stateJsonObject = null;
    protected JSONObject jdb = null;
    protected DPRFileDB db = null;
    protected ZooManager zooManager = null;
    protected ZooHandler zooHandler = null;
    protected Thread zooHandlerThread = null;
    protected long zooPollTime = 120000;
    protected int zooThreadCnt = 1;
    //protected FileManager fileManager = null;
    protected LoggerInf logger = null;
    protected boolean shutdown = true;
    protected String storageBase = null;
    protected InvServiceState serviceState = null;
    private static class Test{ };
    
    public static InventoryConfig useYaml()
        throws TException
    {
        try {
            InventoryConfig inventoryConfig = new InventoryConfig();

            JSONObject jInvInfo = getYamlJson();
            System.out.println("***getYamlJson:\n" + jInvInfo.toString(3));
            JSONObject jInvLogger = jInvInfo.getJSONObject("fileLogger");
            LoggerInf logger = inventoryConfig.setLogger(jInvLogger);
            inventoryConfig.setLogger(logger);
            
            JSONObject jdb = jInvInfo.getJSONObject("db");
            inventoryConfig.setJdb(jdb);
            //inventoryConfig.setDB(logger);
            inventoryConfig.setZookeeper(jInvInfo.getJSONObject("zooserver"), jInvInfo.getJSONObject("zooclient"), logger);
            
            inventoryConfig.setStorageBase(jInvInfo.getString("storageBase"));
            
            inventoryConfig.setStateJsonObject(jInvInfo.getJSONObject("state"));
            
            return inventoryConfig;
            
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }
    
    protected static JSONObject getYamlJson()
       throws TException
    {
        try {
            String propName = "resources/inventoryConfig.yml";
            Test test=new Test();
            InputStream propStream =  test.getClass().getClassLoader().
                    getResourceAsStream(propName);
            String inventoryYaml = StringUtil.streamToString(propStream, "utf8");
            System.out.println("inventoryYaml:\n" + inventoryYaml);
            String invInfoConfig = getYamlInfo();
            System.out.println("\n\n***table:\n" + invInfoConfig);
            String rootPath = System.getenv("SSM_ROOT_PATH");
            System.out.append("\n\n***root:\n" + rootPath + "\n");
            SSMConfigResolver ssmResolver = new SSMConfigResolver();
            YamlParser yamlParser = new YamlParser(ssmResolver);
            System.out.println("\n\n***InventoryYaml:\n" + inventoryYaml);
            LinkedHashMap<String, Object> map = yamlParser.parseString(inventoryYaml);
            LinkedHashMap<String, Object> lmap = (LinkedHashMap<String, Object>)map.get(invInfoConfig);
            if (lmap == null) {
                throw new TException.INVALID_CONFIGURATION(MESSAGE + "Unable to locate configuration");
            }
            //System.out.println("lmap not null");
            yamlParser.loadConfigMap(lmap);
            yamlParser.resolveValues();
            return yamlParser.getJson();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected static String getYamlInfo()
       throws TException
    { 
        String invInfoConfig = System.getenv("which-inv-info");
        if (invInfoConfig == null) {
            invInfoConfig = System.getenv("MERRITT_INV_INFO");
        }
        if (invInfoConfig == null) {
            invInfoConfig = "inv-info";
        }
        return invInfoConfig;
    }
    
    protected void setDB(LoggerInf logger)
       throws TException
    {
        this.db = startDB(logger);
    }
    
    protected DPRFileDB startDB(LoggerInf logger)
       throws TException
    {
        try {
            /*
              "db": {
                "user": "invrw",
                "password": "ixxx",
                "url": "uc3db-inv-stg.cdlib.org",
                "name": "inv",
                "encoding": "OPTIONAL",
                "maxConnections": "OPTIONAL"
                }
            jdbc:mysql://uc3db-inv-stg.cdlib.org:3306/inv?characterEncoding=UTF-8&characterSetResults=UTF-8
            
    public DPRFileDB(LoggerInf logger,
            String dburl,
            String dbuser,
            String dbpass)
        throws TException
    {
        this.logger = logger;
        this.dburl = dburl;
        this.dbuser = dbuser;
        this.dbpass = dbpass;
        setPool();
    }
            */
            
            String  password = jdb.getString("password");
            String  user = jdb.getString("user");
            
            String server = jdb.getString("host");
            String encoding = jdb.getString("encoding");
            if (encoding.equals("OPTIONAL")) {
                encoding = "";
            } else {
                encoding = "?" + encoding;
            }
            String name = jdb.getString("name");
            String url = "jdbc:mysql://" + server + ":3306/" + name + encoding;
            DPRFileDB db = new DPRFileDB(logger, url, user, password);
            return db;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected void setZookeeper(JSONObject jzooServer, JSONObject jzooClient, LoggerInf logger)
       throws TException
    {
        try {
            zooPollTime = jzooClient.getLong("zooPollMilli");
            zooThreadCnt = jzooClient.getInt("zooThreadCount");
            
            zooProperties = new Properties();
            String  queueService = jzooServer.getString("queueService");
            zooProperties.setProperty("QueueService", queueService);
            
            String  queueName = jzooServer.getString("queueName");
            zooProperties.setProperty("QueueName", queueName);
            zooManagerStartup();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    /**
     * set local logger to node/log/...
     * @param path String path to node
     * @return Node logger
     * @throws Exception process exception
     */
    protected LoggerInf setLogger(JSONObject fileLogger)
        throws Exception
    {
        String qualifier = fileLogger.getString("qualifier");
        String path = fileLogger.getString("path");
        Properties logprop = new Properties();
        logprop.setProperty("fileLogger.message.maximumLevel", "" + fileLogger.getInt("messageMaximumLevel"));
        logprop.setProperty("fileLogger.error.maximumLevel", "" + fileLogger.getInt("messageMaximumError"));
        logprop.setProperty("fileLogger.name", fileLogger.getString("name"));
        logprop.setProperty("fileLogger.trace", "" + fileLogger.getInt("trace"));
        logprop.setProperty("fileLogger.qualifier", fileLogger.getString("qualifier"));
        if (StringUtil.isEmpty(path)) {
            throw new TException.INVALID_OR_MISSING_PARM(
                    MESSAGE + "setCANLog: path not supplied");
        }

        File canFile = new File(path);
        File log = new File(canFile, "logs");
        if (!log.exists()) log.mkdir();
        String logPath = log.getCanonicalPath() + '/';
        
        if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("LOG", logprop)
            + "\npath:" + path
            + "\nlogpath:" + logPath
        );
        LoggerInf logger = LoggerAbs.getTFileLogger(qualifier, log.getCanonicalPath() + '/', logprop);
        return logger;
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
        setDB(logger);
    }
    
    protected ZooManager getNewZooManager()
        throws TException
    {
        return ZooManager.getZooManager(logger, zooProperties);
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
    
    public InvServiceState getInvServiceState()
    {
        InvServiceState serviceState = new InvServiceState(stateJsonObject);
        serviceState.setDbStatus(getDbStatus());
        serviceState.setZookeeperStatus(getZookeeperStatus());
        serviceState.setSystemStatus(getSystemStatus());
        return serviceState;
    }
    
    public ServiceStatus getSystemStatus() {
        if (getDbStatus() == ServiceStatus.running) {
            if (getZookeeperStatus() == ServiceStatus.running) return ServiceStatus.running;
            else return ServiceStatus.partial;
        } else {
            if (getZookeeperStatus() == ServiceStatus.running) return ServiceStatus.partial;
            else return ServiceStatus.shutdown;
            
        }
    }

    public JSONObject getStateJsonObject() {
        return stateJsonObject;
    }
    

    public void setStateJsonObject(JSONObject stateJsonObject) {
        this.stateJsonObject = stateJsonObject;
    }

    public String getStorageBase() {
        return storageBase;
    }

    public void setStorageBase(String storageBase) {
        this.storageBase = storageBase;
    }
    
    public void setLogger(LoggerInf logger) {
        this.logger = logger;
    }

    public JSONObject getJdb() {
        return jdb;
    }

    public void setJdb(JSONObject jdb) {
        this.jdb = jdb;
    }
    
    public static void main(String[] argv) {
    	
    	try {
            
            LoggerInf logger = new TFileLogger("test", 50, 50);
            InventoryConfig inventoryConfig = InventoryConfig.useYaml();
            //FileManager.printNodes("MAIN NODEIO");
            ServiceStatus zooStatus = inventoryConfig.getZookeeperStatus();
            ServiceStatus dbStatus = inventoryConfig.getDbStatus();
            System.out.println("Run Status:\n"
                    + " - zooStatus:" + zooStatus + "\n"
                    + " - dbStatus:" + dbStatus + "\n"
            );
            inventoryConfig.zooHandlerStartup();
            System.out.println("Startup zooStatus:" + inventoryConfig.getZookeeperStatus());
            inventoryConfig.zooHandlerShutDown();
            System.out.println("ShutDown zooStatus:" + inventoryConfig.getZookeeperStatus());
            
            DPRFileDB db = inventoryConfig.getDb();
            db.shutDown();
            System.out.println("ShutDown dbStatus:" + inventoryConfig.getDbStatus());
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        }
    }
}
