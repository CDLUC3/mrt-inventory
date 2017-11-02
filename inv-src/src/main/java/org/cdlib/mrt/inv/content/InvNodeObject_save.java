/*
Copyright (c) 2005-2010, Regents of the University of California
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
**********************************************************/
package org.cdlib.mrt.inv.content;


import java.util.Properties;


import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvNodeObject_save
        extends ContentAbs
        implements StateInf
{
    private static final String NAME = "InvNodeObject";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long nodesid = 0;
    public long nodeNumber = 0;
    public long objectsid = 0;
    public Role role = null;
    public DateState created = null;
    public DateState replicated = null;
    public Long versionNumber = null;
    public boolean newEntry = true;
    public Boolean deleteStore = null;
    public Boolean deleteInv = null;
    
    public InvNodeObject_save(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvNodeObject_save(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    /**
     * From a Properties container set the local values for the nodes table
     * @param prop nodes Properties
     * @throws TException 
     */
    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setNodesid(prop.getProperty("inv_node_id"));
            setObjectsid(prop.getProperty("inv_object_id"));
            setRole(prop.getProperty("role"));
            setCreatedDB(prop.getProperty("created"));
            setReplicatedDB(prop.getProperty("replicated"));
            setVersionNumber(prop.getProperty("version_number"));
            setNodeNumber(prop.getProperty("number"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    /**
     * Retrieve a properties where key=db key and value=db usable value
     * @return db properties
     * @throws TException 
     */
    public Properties retrieveProp()
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getNodesid() != 0) prop.setProperty("inv_node_id", "" + getNodesid());
        if (getObjectsid() != 0) prop.setProperty("inv_object_id", "" + getObjectsid());
        if (getRole() != null) prop.setProperty("role", getRole().toString());
        if (getCreated() != null) prop.setProperty("created", getCreatedDB());
        prop.setProperty("version_number", "" + getVersionNumberDB());
        prop.setProperty("replicated", getReplicatedDB());
        return prop;
    }
    
    public String getDBName()
    {
        return NODES_OBJECTS;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(String idS) 
    {
        this.id = setNum(idS);
    }

    public long getNodesid() {
        return nodesid;
    }

    public void setNodesid(long nodesid) {
        this.nodesid = nodesid;
    }

    public void setNodesid(String nodesidS) 
    {
        this.nodesid = setNum(nodesidS);
    }

    public long getObjectsid() {
        return objectsid;
    }

    public void setObjectsid(long objectsid) {
        this.objectsid = objectsid;
    }

    public void setObjectsid(String objectsidS) 
    {
        this.objectsid = setNum(objectsidS);
    }

    public DateState getCreated() {
        return created;
    }

    public String getCreatedDB() {
        if (created == null) return null;
        return InvUtil.getDBDate(created);
    }

    public void setCreated(DateState created) {
        this.created = created;
    }

    public void setCreated(String createdS) {
        if (StringUtil.isAllBlank(createdS)) this.created = new DateState();
        else this.created = new DateState(createdS);
    }

    public void setCreatedDB(String createdS) {
        if (StringUtil.isAllBlank(createdS)) this.created = new DateState();
        else this.created = InvUtil.setDBDate(createdS);
    }

    public DateState getReplicated() {
        return replicated;
    }

    public String getReplicatedDB() {
        if (replicated == null) return "";
        return InvUtil.getDBDate(replicated);
    }

    public void setReplicated(DateState replicated) {
        this.replicated = replicated;
    }

    public void setReplicatedCurrent() {
        this.replicated = new DateState();
    }

    public void setReplicated(String replicatedS) {
        if (StringUtil.isAllBlank(replicatedS)) this.replicated = null;
        else this.replicated = new DateState(replicatedS);
    }

    public void setReplicatedDB(String replicatedS) {
        if (StringUtil.isAllBlank(replicatedS)) this.replicated = null;
        else this.replicated = InvUtil.setDBDate(replicatedS);
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRole(String roleS) {
        if (StringUtil.isEmpty(roleS)) {
            this.role = null;
            return;
        }
        this.role = Role.getRole(roleS);
    }

    public Long getVersionNumber() {
        return versionNumber;
    }

    public String getVersionNumberDB() {
        if (versionNumber == null) return "";
        return "" + versionNumber;
    }

    public void setVersionNumber(Long replicatedVersions) {
        this.versionNumber = replicatedVersions;
    }

    public void setVersionNumber(String versionNumberS) {
        this.versionNumber = setNumLong(versionNumberS);
    }
    
    public void setNodeNumber(String nodeNumberS) {
        this.nodeNumber = setNumLong(nodeNumberS);
    }

    public long getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(long nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public Boolean getDeleteStore() {
        return deleteStore;
    }

    public void setDeleteStore(Boolean deleteStore) {
        this.deleteStore = deleteStore;
    }

    public Boolean getDeleteInv() {
        return deleteInv;
    }

    public void setDeleteInv(Boolean deleteInv) {
        this.deleteInv = deleteInv;
    }
    
}

