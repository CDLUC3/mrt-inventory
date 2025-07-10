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

import java.net.URL;
import java.util.Properties;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.zoo.ZooManager;

import org.cdlib.mrt.inv.action.InvManifestUrl;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.json.JSONObject;

/**
 * Inv Service Interface
 * @author  dloy
 */

public interface InvServiceInf
{
    /**
     * Add object description to inv database
     * @param node storage node
     * @param objectID storage objectID
     * @return true item added
     * @throws TException 
     */
    public boolean add(
            int node, 
            Identifier objectID)
        throws TException;
    
    /**
     * Add object description to inv database using manifest URL
     * @param manifestURL Storage Manifest URL for object
     * @return true item added
     * @throws TException 
     */
    public boolean add(
            String manifestURL)
        throws TException;
    
    
    public InvProcessState add(
            String manifestURL,
            boolean doCheckVersion)
        throws TException;
    
    /**
     * Build inv_nodes row from node can-info.txt content
     * @param nodeNum Storage node number
     * @return select state for added content
     * @throws TException 
     */
    public InvSelectState setFileNode(
            int nodeNum)
        throws TException;
    
    /**
     * Get manifest.xml URL for this object
     * @param objectID object identifier
     * @return manifest.xml URL
     */
    public InvManifestUrl getManifestUrl(
            Identifier objectID)
        throws TException;
    
    /**
     * Return the current version number
     * @param objectID
     * @return versionState with only ark and ersion number
     * @throws TException 
     */
    public VersionsState getCurrent(
            Identifier objectID)
        throws TException;
    
    /**
     * Get access version information for cloud content
     * @param objectID object identifier
     * @param version version number to be selected: null=all; 0=current
     * @return access content information
     * @throws TException 
     */
    public VersionsState getVersions(
            Identifier objectID,
            Long version)
        throws TException;
    
    /**
     * Get access version information for cloud content
     * @param objectID object identifier
     * @param version version number to be selected: null=all; 0=current
     * @param fileID data file identifier
     * @return access content information
     * @throws TException 
     */
    public VersionsState getVersions(
            Identifier objectID,
            Long version,
            String fileID)
        throws TException;
    
    /**
     * add a single primary-local map
     * @param objectID object ark
     * @param ownerID owner ark
     * @param localID local identifier
     * @return primary id or null if not found
     */
    public LocalContainerState addPrimary(
            Identifier objectID,
            Identifier ownerID,
            String localIDs)
        throws TException;
    
    /**
     * Add localid entries based on internal mysql entries
     * @param after start after this object sequence id
     * @param to stop at this object table id 
     * @return
     * @throws TException 
     */
    public LocalAfterToState addLocalFromTo(
            Long after,
            Long to)
        throws TException;
    
    /**
     * Delete primary-local records associated with primary objectID
     * @param objectID object ark for delete
     * @return container before delete
     * @throws TException 
     */
    public LocalContainerState deletePrimary(
            Identifier objectID)
        throws TException;
    
    /**
     * Get the single primary-local map
     * @param ownerID owner ark
     * @param localID local identifier
     * @return primary id or null if not found
     */
    public LocalContainerState getPrimary(
            Identifier ownerID,
            String localID)
        throws TException;
    
    /**
     * get the 1 or more primary-local maps
     * @param objectID
     * @return 
     */
    public LocalContainerState getLocal(
            Identifier objectID)
        throws TException;
    
    /**
     * Build inv_audits table from existing inv content
     * @param storageBase storage prefix used by fixity
     * @param objectID object identifier
     * @return number of audit rows added for this object
     * @throws TException process exception
     */
    public long buildAudit(
            String storageBase,
            Identifier objectID)
        throws TException;
    
    /**
     * Delete object description from inv database
     * @param objectID storage objectID
     * @return number of deleted rows
     * @throws TException 
     */
    public InvDeleteState delete(
            Identifier objectID)
        throws TException;
    
    /**
     * @return pointer to merritt logger
     */
    public LoggerInf getLogger();
    
    public InvServiceState getInvServiceState()
        throws TException;
    
    public InvServiceState shutdownZoo()
        throws TException;
    
    public InvProcessState process(
            Role copyRole,
            String manifestURL)
        throws TException;
    
    public InvServiceState startupZoo()
        throws TException;
    
    /**
     * Shutdown services
     * @return service state
     * @throws TException 
     */
    public InvServiceState shutdown()
        throws TException;
    
    /**
     * Startup services
     * @return service state
     * @throws TException 
     */
    public InvServiceState startup()
        throws TException;
    
    /**
     * Perform a select query against inv
     * @param sql select command without "select "
     * @return State containing rows for search results
     * @throws TException 
     */
    public InvSelectState select(String sql)
        throws TException;
    
    
    /**
     * Add entry to inv_tasks
     * @param taskName name of general task being tracked
     * @param taskItem item of type name
     * @param currentStatusS status 'ok','fail','partial','unknown'
     * @param note optional note about task item (e.g. exception
     * @return jsonobject of added task
     * @throws TException 
     */
    public JSONObject addTask(
            String taskName, 
            String taskItem, 
            String currentStatusS,
            String note)
        throws TException;
    
    /**
     * Delete entry to inv_tasks
     * @param taskName name of general task being tracked
     * @param taskItem item of type name
     * @return jsonobject of task entry before deletion
     * @throws TException 
     */
    public JSONObject deleteTask(
            String taskName, 
            String taskItem)
        throws TException;
    
    /**
     * Get entry to inv_tasks
     * @param taskName name of general task being tracked
     * @param taskItem item of type name
     * @return current jsonobject of task entry 
     * @throws TException 
     */
    public JSONObject getTask(
            String taskName, 
            String taskItem)
        throws TException;
    
    /**
     * 
     * @param adminID sla ark for SLA collection
     * @param name name of sla
     * @param mnemonic mnemonic of sla
     * @return
     * @throws TException 
     */
    public JSONObject addAdminSLA(
            Identifier adminID, 
            String name, 
            String mnemonic)
        throws TException;
    
    /**
     * @return Zoo Manager for this service
     */
    public ZooManager getZooManager();
}
