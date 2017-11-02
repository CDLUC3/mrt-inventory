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
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.MessageDigest;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvFile
        extends ContentAbs
{
    private static final String NAME = "InvFile";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long objectID = 0;
    public long versionID = 0;
    public String pathName = null;
    public Source source = null;
    public Role role = null;
    public long fullSize = 0;
    public long billableSize = 0;
    public String mimeType = null;
    public DateState created = null;
    public boolean newEntry = false;
    public DigestType digestType = null;
    public String digestValue = null;
    
    public enum Source
    {
        consumer, producer, system;
    }
    
    public enum Role
    {
        data, metadata;
    }
    public enum DigestType
    {
        adler32("adler-32"),
        crc32("crc-32"),
        md2("md2"),
        md5("md5"),
        sha1("sha-1"),
        sha256("sha-256"),
        sha384("shar384"),
        sha512("sha-512")
        ;

        protected final String dispType;
        DigestType(String dispType) {
            this.dispType = dispType;
        }
        public String toString()
        {
            return dispType;
        }

        public static DigestType getDigestType(String t)
        {
            if (t == null) return null;
            for (DigestType p : DigestType.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    public InvFile(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvFile(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setFileComponent(FileComponent fileComponent, boolean billable)
        throws TException
    {
        if (fileComponent == null) return;
        try {
            String fileID = fileComponent.getIdentifier();
            
            setPathName(fileID);
            if (fileID.contains("system/")) source = Source.system;
            else if (fileID.contains("producer/")) source = Source.producer;
            if (fileID.contains("mrt-erc.txt")) role = Role.metadata;
            else if (fileID.contains("mrt-dublincore")) role = Role.metadata;
            else role = Role.data;
            long size = fileComponent.getSize();
            if (billable) {
                setBillableSize(size);
            } else {
                setBillableSize(0);
            }
            setFullSize(size);
            setMimeType(fileComponent.getMimeType());
            MessageDigest messageDigest = fileComponent.getMessageDigest();
            if (messageDigest != null) {
                String coreDigestType = messageDigest.getAlgorithm().toString();
                setDigestType(DigestType.valueOf(coreDigestType));
            }
            setDigestValue(messageDigest.getValue());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }

    public FileComponent getFileComponent()
        throws TException
    {
        FileComponent fileComponent = new FileComponent();
        try {
            fileComponent.setIdentifier(getPathName());
            fileComponent.setSize(getFullSize());
            fileComponent.setMimeType(getMimeType());
            fileComponent.setFirstMessageDigest(getDigestValue(), getDigestType().toString());
            fileComponent.setCreated(getCreated());
            return fileComponent;
            
        } catch (Exception ex) {
            ex.printStackTrace();
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
            setPathName(prop.getProperty("pathname"));
            setSource(prop.getProperty("source"));
            setRole(prop.getProperty("role"));
            setFullSize(prop.getProperty("full_size"));
            setBillableSize(prop.getProperty("billable_size"));
            setMimeType(prop.getProperty("mime_type"));
            setDigestType(prop.getProperty("digest_type"));
            setDigestValue(prop.getProperty("digest_value"));
            setCreatedDB(prop.getProperty("created"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getObjectID() != 0) prop.setProperty("inv_object_id", "" + getObjectID());
        if (getVersionID() != 0) prop.setProperty("inv_version_id", "" + getVersionID());
        // fix:       if (getPathName() != null) prop.setProperty("pathname", InvDBUtil.mySQLEsc(getPathName()));
        if (getPathName() != null) prop.setProperty("pathname", getPathName());
        if (getSource() != null) prop.setProperty("source", getSource().toString());
        if (getRole() != null) prop.setProperty("role", getRole().toString());
        if (getFullSize() != 0) prop.setProperty("full_size", "" + getFullSize());
        if (getBillableSize() != 0) prop.setProperty("billable_size", "" + getBillableSize());
        if (getMimeType() != null) prop.setProperty("mime_type", getMimeType());
        if (getDigestType() != null) prop.setProperty("digest_type", getDigestType().toString());
        if (getDigestValue() != null) prop.setProperty("digest_value", getDigestValue());
        return prop;
    }
    
    public String getDBName()
    {
        return FILES;
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
        this.role = Role.valueOf(roleS);
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public void setSource(String sourceS) {
        if (StringUtil.isEmpty(sourceS)) {
            this.source = null;
            return;
        }
        this.source = Source.valueOf(sourceS);
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public DateState getCreated() {
        return created;
    }

    public void setCreated(DateState created) {
        this.created = created;
    }

    public void setCreatedDB(String createdS) {
        this.created = InvUtil.setDBDate(createdS);
    }

    public long getBillableSize() {
        return billableSize;
    }

    public void setBillableSize(long billableSize) {
        this.billableSize = billableSize;
    }

    public void setBillableSize(String billableSizeS) {
        this.billableSize = setNum(billableSizeS);
    }

    public long getFullSize() {
        return fullSize;
    }

    public void setFullSize(long fullSize) {
        this.fullSize = fullSize;
    }

    public void setFullSize(String fullSizeS) {
        this.fullSize = setNum(fullSizeS);
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    public DigestType getDigestType() {
        return digestType;
    }

    public void setDigestType(DigestType digestType) {
        this.digestType = digestType;
    }

    public void setDigestType(String digestTypeS) {
        if (StringUtil.isEmpty(digestTypeS)) this.digestType = null;
        this.digestType = DigestType.getDigestType(digestTypeS);
    }

    public String getDigestValue() {
        return digestValue;
    }

    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }

}

