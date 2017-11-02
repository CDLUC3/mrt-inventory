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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;


import org.cdlib.mrt.inv.extract.StoreState;
import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.FixityStatusType;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.MessageDigest;
import static org.cdlib.mrt.inv.content.ContentAbs.setNum;
import static org.cdlib.mrt.inv.content.InvNode.MediaType.unknown;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvAudit
        extends ContentAbs
{
    private static final String NAME = "InvAudit";
    private static final String MESSAGE = NAME + ": ";
    
    public long id = 0;
    public long nodeid = 0;
    public long objectid = 0;
    public long versionid = 0;
    public long fileid = 0;
    public String url = null;
    public FixityStatusType status = FixityStatusType.unknown;
    public DateState created = null;
    public DateState verified = null;
    public DateState modified = null;
    public long failedSize = 0;
    public String failedDigestValue = null;
    public String note = null;
    protected boolean newEntry = false;
    
    
    
    public InvAudit(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvAudit(Properties prop, LoggerInf logger)
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
            setNodeid(prop.getProperty("inv_node_id"));
            setObjectid(prop.getProperty("inv_object_id"));
            setVersionid(prop.getProperty("inv_version_id"));
            setFileid(prop.getProperty("inv_file_id"));
            setUrl(prop.getProperty("url"));
            setStatusDB(prop.getProperty("status"));
            setCreatedDB(prop.getProperty("created"));
            setVerifiedDB(prop.getProperty("verified"));
            setModifiedDB(prop.getProperty("modified"));
            setFailedSize(prop.getProperty("failed_size"));
            setFailedDigestValue(prop.getProperty("failed_digest_value"));
            setNote(prop.getProperty("note"));
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
        if (getNodeid() != 0) prop.setProperty("inv_node_id", "" + getNodeid());
        if (getObjectid() != 0) prop.setProperty("inv_object_id", "" + getObjectid());
        if (getVersionid() != 0) prop.setProperty("inv_version_id", "" + getVersionid());
        if (getFileid() != 0) prop.setProperty("inv_file_id", "" + getFileid());
        if (getUrl() != null) prop.setProperty("url", getUrl());
        if (getCreated() != null) prop.setProperty("created", getCreatedDB());
        if (getVerified() != null) prop.setProperty("verified", getVerifiedDB());
        if (getModified() != null) prop.setProperty("modified", getModifiedDB());
        prop.setProperty("failed_size", "" + getFailedSize());
        if (getFailedDigestValue() != null) prop.setProperty("failed_digest_value", getFailedDigestValue());
        else prop.setProperty("failed_digest_value", "");
        if (getNote() != null) prop.setProperty("note", getNote());
        else prop.setProperty("note", "");
        prop.setProperty("status", "" + getFixityStatusType());
        return prop;
    }
    
    public String dump(String header)
    {
            Properties prop = retrieveProp();
            return PropertiesUtil.dumpProperties(header, prop);
    }
    
    public String getDBName()
    {
        return AUDITS;
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

    public long getNodeid() {
        return nodeid;
    }

    public void setNodeid(long nodeid) {
        this.nodeid = nodeid;
    }

    public void setNodeid(String nodeidS) 
    {
        this.nodeid = setNum(nodeidS);
    }

    public long getObjectid() {
        return objectid;
    }

    public void setObjectid(long objectid) {
        this.objectid = objectid;
    }

    public void setObjectid(String objectidS) 
    {
        this.objectid = setNum(objectidS);
    }

    public long getVersionid() {
        return versionid;
    }

    public void setVersionid(long versionid) {
        this.versionid = versionid;
    }

    public void setVersionid(String versionidS) 
    {
        this.versionid= setNum(versionidS);
    }

    public long getFileid() {
        return fileid;
    }

    public void setFileid(long fileid) {
        this.fileid = fileid;
    }

    public void setFileid(String fileidS) 
    {
        this.fileid = setNum(fileidS);
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
        this.created = new DateState(createdS);
    }

    public void setCreatedDB(String createdS) {
        if (StringUtil.isAllBlank(createdS)) {
            this.created = new DateState();
            return;
        }
        this.created = InvUtil.setDBDate(createdS);
    }

    public DateState getVerified() {
        return verified;
    }

    public String getVerifiedDB() {
        if (verified == null) return null;
        return InvUtil.getDBDate(verified);
    }

    public void setVerified(DateState verified) {
        this.verified = verified;
    }

    public void setVerified(String verifiedS) {
        if (StringUtil.isAllBlank(verifiedS)) {
            this.verified = new DateState();
            return;
        }
        this.verified = new DateState(verifiedS);
    }


    public void setVerifiedDB(String verifiedS) {
        if (StringUtil.isAllBlank(verifiedS)) this.verified = new DateState();
        this.verified = InvUtil.setDBDate(verifiedS);
    }

    public DateState getModified() {
        return modified;
    }

    public String getModifiedDB() {
        if (modified == null) return null;
        return InvUtil.getDBDate(modified);
    }

    public void setModified(DateState modified) {
        this.modified = modified;
    }

    public void setModified(String modifiedS) {
        if (StringUtil.isAllBlank(modifiedS)) this.modified = new DateState();
        this.modified = new DateState(modifiedS);
    }

    public void setModifiedDB(String modifiedS) {
        if (StringUtil.isAllBlank(modifiedS)) {
            this.modified = new DateState();
            return;
        }
        this.modified = InvUtil.setDBDate(modifiedS);
    }

    public FixityStatusType getFixityStatusType() {
        return status;
    }

    public void setStatus(FixityStatusType status) {
        this.status = status;
    }

    public void setStatus(String statusS) {
        if (StringUtil.isEmpty(statusS)) {
            this.status = null;
            return;
        }
        this.status = FixityStatusType.valueOf(statusS);
    }

    public void setStatusDB(String statusS) {
        if (StringUtil.isEmpty(statusS)) {
            this.status = null;
            return;
        }
        this.status = FixityStatusType.getFixityStatusType(statusS);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getFailedSize() {
        return failedSize;
    }

    public void setFailedSize(long failedSize) {
        this.failedSize = failedSize;
    }

    public void setFailedSize(String failedSizeS) 
    {
        this.failedSize = setNum(failedSizeS);
    }

    public String getFailedDigestValue() {
        return failedDigestValue;
    }

    public void setFailedDigestValue(String failedDigestValue) {
        this.failedDigestValue = failedDigestValue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
    
}

