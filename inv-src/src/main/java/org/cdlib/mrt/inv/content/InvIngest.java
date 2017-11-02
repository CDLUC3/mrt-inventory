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


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.DateUtil;
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
public class InvIngest
        extends ContentAbs
{
    private static final String NAME = "InvIngest";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long objectID = 0;
    public long versionID = 0;
    public String fileName = null;
    public ManifestType manifestType = null;
    public String profile = null;
    public String batchID = null;
    public String jobID = null;
    public String userAgent = null;
    public DateState submitted = null;
    public String storageURL = null;
    public boolean newEntry = false;
    
    
    //ENUM('file', 'container', 'object-manifest', 'single-file-batch-manifest', 'container-batch-manifest', 'batch-manifest')
    public enum ManifestType
    {
        file("file"),
        container("container"),
        objectManifest("object-manifest"),
        singleFileBatchManifest("single-file-batch-manifest"),
        containerBatchManifest("container-batch-manifest"),
        batchManifest("batch-manifest");

        protected final String dispType;
        ManifestType(String dispType) {
            this.dispType = dispType;
        }
        public String toString()
        {
            return dispType;
        }

        public static ManifestType getManifestType(String t)
        {
            for (ManifestType p : ManifestType.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    public InvIngest(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvIngest(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setIngestProp(Properties prop, String storageURL)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setObjectID(prop.getProperty("object_id"));
            setVersionID(prop.getProperty("version_id"));
            setFileName(prop.getProperty("file"));
            setManifestType(prop.getProperty("type"));
            setProfile(prop.getProperty("profile"));
            setBatchID(prop.getProperty("batch"));
            setJobID(prop.getProperty("job"));
            setUserAgent(prop.getProperty("userAgent"));
            setSubmitted(prop.getProperty("submissionDate"));
            if (StringUtil.isNotEmpty(storageURL))
                setStorageURL(storageURL);
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setObjectID(prop.getProperty("inv_object_id"));
            setVersionID(prop.getProperty("inv_version_id"));
            setFileName(prop.getProperty("filename"));
            setManifestType(prop.getProperty("ingest_type"));
            setProfile(prop.getProperty("profile"));
            setBatchID(prop.getProperty("batch_id"));
            setJobID(prop.getProperty("job_id"));
            setUserAgent(prop.getProperty("user_agent"));
            setSubmittedDB(prop.getProperty("submitted"));
            setStorageURL(prop.getProperty("storage_url"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getObjectID() > 0) prop.setProperty("inv_object_id", "" + getObjectID());
        if (getVersionID() > 0) prop.setProperty("inv_version_id", "" + getVersionID());
        if (getFileName() != null) prop.setProperty("filename", getFileName());
        if (getManifestType() != null) prop.setProperty("ingest_type", getManifestType().toString());
        if (getProfile() != null) prop.setProperty("profile", getProfile());
        if (getBatchID() != null) prop.setProperty("batch_id", getBatchID());
        if (getJobID() != null) prop.setProperty("job_id", getJobID());
        if (getUserAgent() != null) prop.setProperty("user_agent", getUserAgent());
        if (getSubmitted() != null) prop.setProperty("submitted", getSubmittedDB());
        if (getStorageURL() != null) prop.setProperty("storage_url", getStorageURL());
        return prop;
    }
    
    public String getDBName()
    {
        return INGESTS;
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

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long objectID) {
        this.objectID = objectID;
    }

    public void setObjectID(String objectIDS) {
        this.objectID = setNum(objectIDS);
    }

    public long getVersionID() {
        return versionID;
    }

    public void setVersionID(long versionID) {
        this.versionID = versionID;
    }

    public void setVersionID(String versionIDS) {
        this.versionID = setNum(versionIDS);
    }

    public void setManifestType(ManifestType manifestType) {
        this.manifestType = manifestType;
    }

    public void setManifestType(String manifestTypeS) {
        if (StringUtil.isEmpty(manifestTypeS)) this.manifestType = null;
        this.manifestType = ManifestType.getManifestType(manifestTypeS);
    }

    public ManifestType getManifestType() {
        return manifestType;
    }

    public String getBatchID() {
        return batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getStorageURL() {
        return storageURL;
    }

    public void setStorageURL(String storageURL) {
        this.storageURL = storageURL;
    }

    public DateState getSubmitted() {
        return submitted;
    }

    public String getSubmittedDB() {
        if (submitted == null) return null;
        return InvUtil.getDBDate(submitted);
    }

    public void setSubmitted(DateState submitted) {
        this.submitted = submitted;
    }

    public void setSubmitted(String submittedS) {
        this.submitted = new DateState(submittedS);
    }

    public void setSubmittedDB(String submittedS) {
        this.submitted = InvUtil.setDBDate(submittedS);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    
}

