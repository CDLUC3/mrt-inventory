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



import java.sql.Connection;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.cdlib.mrt.cloud.ManifestSAX;

import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.cloud.ManifestXML;
import org.cdlib.mrt.cloud.VersionMap;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.action.AddLocalAfterTo;
import org.cdlib.mrt.inv.action.AddObject;
import org.cdlib.mrt.inv.action.AddZoo;
import org.cdlib.mrt.inv.action.BuildAudits;
import org.cdlib.mrt.inv.action.DeleteObject;
import org.cdlib.mrt.inv.action.InvSelect;
import org.cdlib.mrt.inv.action.InvManifestUrl;
import org.cdlib.mrt.inv.action.LocalMap;
import org.cdlib.mrt.inv.action.ProcessItem;
import org.cdlib.mrt.inv.action.ProcessObject;
import org.cdlib.mrt.inv.action.SaveNode;
import org.cdlib.mrt.inv.action.Versions;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.zoo.ZooQueue;
import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Inv Service
 * @author  dloy
 */

public class InvService
        implements InvServiceInf
{
    private static final String NAME = "InvService";
    private static final String MESSAGE = NAME + ": ";
    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    private static final boolean THREADDEBUG = false;
    protected LoggerInf logger = null;
    protected Exception exception = null;
    protected InvServiceProperties invServiceProperties = null;

    public static InvService getInvService(InvServiceProperties invServiceProperties)
            throws TException
    {
        return new InvService(invServiceProperties);
    }

    public static InvService getInvService(Properties prop)
            throws TException
    {
        return new InvService(prop);
    }

    protected InvService(InvServiceProperties invServiceProperties)
        throws TException
    {
        this.invServiceProperties = invServiceProperties;
        this.logger = invServiceProperties.getLogger();
    }

    protected InvService(Properties prop)
        throws TException
    {
        this.invServiceProperties = new InvServiceProperties(prop);
        this.logger = invServiceProperties.getLogger();
    }
    
    @Override
    public boolean add(
            int node, 
            Identifier objectID)
        throws TException
    {
        if (DEBUG) System.out.print("add entered");
        Connection connection = invServiceProperties.getConnection(false);
        String storageBase = invServiceProperties.getStorageBase();
        AddObject addObject = AddObject.getAddObject(
                storageBase, 
                node, 
                objectID, 
                connection, 
                logger);
        addObject.process();
        return addObject.isCommit();
    }
    
    @Override
    public boolean add(
            String manifestURL)
        throws TException
    {
        Connection connection = null;
        try {
            if (DEBUG) System.out.print("add entered");
            connection = invServiceProperties.getConnection(false);
            String storageBase = invServiceProperties.getStorageBase();
            AddObject addObject = AddObject.getAddObject(
                    manifestURL,
                    connection, 
                    logger);
            addObject.process();
            return addObject.isCommit();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) { }
            }
        }
    }
    
    @Override
    public InvProcessState add(
            String manifestURL,
            boolean doCheckVersion)
        throws TException
    {
        return process(null, manifestURL, doCheckVersion);
    }
    
    @Override
    public boolean addZoo(
            Properties zooProp,
            ZooQueue queue)
        throws TException
    {
        if (DEBUG) System.out.print("add entered");
        AddZoo addZoo = AddZoo.getAddZoo(
                queue,
                zooProp);
        addZoo.process();
        return addZoo.isSent();
    }
    
    @Override
    public InvSelectState setFileNode(
            int nodeNum)
        throws TException
    {
        if (DEBUG) System.out.print("setFileNode entered:" + nodeNum);
        Connection connection = invServiceProperties.getConnection(false);
        
        
        SaveNode saveNode = SaveNode.getSaveNode(nodeNum, connection, 
                invServiceProperties.getStorageBase(),
                logger);
        saveNode.resetInvNode();
        InvSelectState state = new InvSelectState(saveNode.getCanProp());
        return state;
    }
    
    /**
     * Get manifest.xml URL for this object
     * @param objectID object identifier
     * @return manifest.xml URL
     */
    @Override
    public InvManifestUrl getManifestUrl(
            Identifier objectID)
        throws TException
    {
        if (DEBUG) System.out.print("getManifestUrl entered");
        Connection connection = invServiceProperties.getConnection(false);
        InvManifestUrl manifestUrl = InvManifestUrl.getInvManifestUrl(objectID, connection, logger);
        manifestUrl.process();
        return manifestUrl;
        
    }
    
    /**
     * Get access version information for cloud content
     * @param objectID object identifier
     * @param version version number to be selected: null=all; 0=current
     * @return access content information
     * @throws TException 
     */
    
    @Override
    public VersionsState getVersions(
            Identifier objectID,
            Long version)
        throws TException
    {
        if (DEBUG) System.out.print("getVersions entered");
        Connection connection = null;
        try {
            connection = invServiceProperties.getConnection(false);
            Versions versions = Versions.getVersions(objectID, version, connection, logger);
            VersionsState state = versions.process();
            return state;
        
        } catch (TException tex) {
            throw tex;
            
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (Exception ex) { }
            }
        }
        
    }
    
    /**
     * Get access version information for cloud content
     * @param objectID object identifier
     * @param version version number to be selected: null=all; 0=current
     * @param fileID data file identifier
     * @return access content information
     * @throws TException 
     */
    @Override
    public VersionsState getVersions(
            Identifier objectID,
            Long version,
            String fileID)
        throws TException
    {
        if (DEBUG) System.out.print("getVersions entered");
        Connection connection = null;
        try {
            connection = invServiceProperties.getConnection(false);
            Versions versions = Versions.getVersions(objectID, version, connection, logger);
            VersionsState state = versions.process(fileID);
            return state;
        
        } catch (TException tex) {
            throw tex;
            
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (Exception ex) { }
            }
        }
        
    }
    
    /**
     * Get the single primary-local map
     * @param ownerID owner ar
     * @param collectionID collection ark
     * @param localID local identifier
     * @return primary id or null if not found
     */
    @Override
    public LocalContainerState addPrimary(
            Identifier objectID,
            Identifier ownerID,
            String localIDs)
        throws TException
    {
        if (DEBUG) System.out.print("addPrimaryLocal entered");
        Connection connection = invServiceProperties.getConnection(false);
        LocalContainerState state =  LocalMap.addLocal(objectID, ownerID, localIDs, connection, logger);
        return state;
    }
    
    /**
     * Add localid entries based on internal mysql entries
     * @param after start after this object sequence id
     * @param to stop at this object table id 
     * @return
     * @throws TException 
     */
    @Override
    public LocalAfterToState addLocalFromTo(
            Long after,
            Long to)
        throws TException
    {
        if (DEBUG) System.out.print("addPrimaryLocal entered");
        DPRFileDB db = invServiceProperties.getNewDb();
        AddLocalAfterTo alat = AddLocalAfterTo.getAddLocalAfterTo(after, to, db, this, logger);
        LocalAfterToState state =  alat.process();
        return state;
    }
    
    @Override
    public LocalContainerState deletePrimary(
            Identifier objectID)
        throws TException
    {
        if (DEBUG) System.out.print("deletePrimary entered");
        Connection connection = invServiceProperties.getConnection(false);
        LocalContainerState state =  LocalMap.deletePrimary(objectID, connection, logger);
        return state;
    }
    
    /**
     * Get the single primary-local map
     * @param ownerID owner ark
     * @param localID local identifier
     * @return primary id or null if not found
     */
    @Override
    public LocalContainerState getPrimary(
            Identifier ownerID,
            String localID)
        throws TException
    {
        if (DEBUG) System.out.print("getPrimary entered");
        Connection connection = invServiceProperties.getConnection(false);
        LocalContainerState state =  LocalMap.getPrimaryClose(ownerID, localID, connection, logger);
        return state;
    }
    
    /**
     * get the 1 or more primary-local maps
     * @param objectID
     * @return 
     */
    @Override
    public LocalContainerState getLocal(
            Identifier objectID)
        throws TException
    {
        if (DEBUG) System.out.print("getPrimary entered");
        Connection connection = invServiceProperties.getConnection(false);
        LocalContainerState state =  LocalMap.getLocalsClose(objectID, connection, logger);
        return state;
    }
    
    public ProcessStatus processItem(
            Item item,
            ZooQueue queue)
        throws TException
    {
        if (DEBUG) System.out.print("processItem entered");
        if (invServiceProperties.getZookeeperStatus() == ServiceStatus.shutdown) return ProcessStatus.shutdown;
        Connection connection = invServiceProperties.getConnection(false);
        ProcessItem  processItem = ProcessItem.getProcessItem(
                1,
            queue,
            invServiceProperties.getDb(),
            item);
        processItem.process();
        return processItem.getProcessStatus();
    }
    
    @Override
    public InvProcessState process(
            Role copyRole,
            String manifestURL)
        throws TException
    {
        return process(copyRole, manifestURL, true);
    }
    
    public InvProcessState process(
            Role copyRole,
            String manifestURL,
            boolean doCheckVersion)
        throws TException
    {
        Connection connection = null;
        try {
            if (DEBUG) System.out.print("process entered");
            connection = invServiceProperties.getConnection(false);
            ProcessObject processObject = ProcessObject.getProcessObject(
                    Role.primary,
                    copyRole,
                    manifestURL,
                    connection, 
                    logger);
            processObject.setDoCheckVersion(doCheckVersion);
            processObject.process();
            return processObject.getState();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) { }
            }
        }
    }
    
    @Override
    public long buildAudit(
            String storageBase,
            Identifier objectID)
        throws TException
    {
        Connection connection = null;
        BuildAudits buildAudits = null;
        try {
            if (DEBUG) System.out.print("process entered");
            connection = invServiceProperties.getConnection(false);
            buildAudits = BuildAudits.getBuildAudits(
                storageBase,
                objectID,
                connection,
                logger);
            buildAudits.process();
            return buildAudits.getAuditCnt();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) { }
            }
            buildAudits = null;
            System.gc();
        }
    }
    
    @Override
    public InvDeleteState delete(
            Identifier objectID)
        throws TException
    {
        if (DEBUG) System.out.print("delete entered");
        Connection connection = invServiceProperties.getConnection(false);
        DeleteObject deleteObject = DeleteObject.getDeleteObject(
                objectID, 
                connection, 
                logger);
        return deleteObject.process();
    }
    
    @Override
    public InvSelectState select(String sql)
        throws TException
    {
        if (DEBUG) System.out.print("select entered");
        if (StringUtil.isEmpty(sql)) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "select sql required");
        }
        Connection connection = invServiceProperties.getConnection(false);
        InvSelect invSelectSQL = InvSelect.getInvSelect(
            sql,
            connection,
            logger);
        InvSelectState invSelectState = invSelectSQL.process();
        return invSelectState;
    }

    @Override
    public InvServiceState getInvServiceState()
        throws TException
    {
        InvServiceState invServiceState = invServiceProperties.getInvServiceState();
        return invServiceState;
    }
    
    @Override
    public InvServiceState shutdownZoo()
        throws TException
    {
        invServiceProperties.zooHandlerShutDown();
        InvServiceState invServiceState = invServiceProperties.getInvServiceState();
        return invServiceState;
    }

    @Override
    public InvServiceState startupZoo()
        throws TException
    {
        if (invServiceProperties.getDbStatus() == ServiceStatus.shutdown) {
            throw new TException.REQUEST_INVALID("Zookeeper startup requested but DB shutdown");
        }
        invServiceProperties.zooHandlerStartup();
        InvServiceState invServiceState = invServiceProperties.getInvServiceState();
        return invServiceState;
    }

    @Override
    public InvServiceState shutdown()
        throws TException
    {
        invServiceProperties.zooHandlerShutDown();
        invServiceProperties.dbShutDown();
        InvServiceState invServiceState = invServiceProperties.getInvServiceState();
        return invServiceState;
    }

    @Override
    public InvServiceState startup()
        throws TException
    {
        invServiceProperties.dbStartup();
        invServiceProperties.zooHandlerStartup();
        InvServiceState invServiceState = invServiceProperties.getInvServiceState();
        return invServiceState;
    }

    public static VersionMap getVersionMap(Identifier id, String storageBase, LoggerInf logger)
        throws TException
    {
        File tempFile = null;
        String urlS = null;
        try {
            urlS = storageBase + "/"
                + URLEncoder.encode(id.getValue(), "utf-8");
            tempFile = FileUtil.url2TempFile(logger, urlS);
            InputStream xmlStream = new FileInputStream(tempFile);
            return ManifestSAX.buildMap(xmlStream, logger);
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
            
        } finally {
            if (tempFile != null) {
                try {
                    tempFile.delete();
                } catch (Exception ex) { }
            }
        }
    }
    
    protected void throwException(Exception ex)
        throws TException
    {
        if (ex instanceof TException) {
            throw (TException) ex;
        }
        throw new TException(ex);
    }
    
    public ZooManager getZooManager()
    {
        return invServiceProperties.getZooManager();
    }

    public LoggerInf getLogger() {
        return logger;
    }

    public InvServiceProperties getInvServiceProperties() {
        return invServiceProperties;
    }
    

}
