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

import org.cdlib.mrt.inv.extract.StoreMeta;
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
public class InvMeta
        extends ContentAbs
{
    private static final String NAME = "InvMeta";
    private static final String MESSAGE = NAME + ": ";
    
    /**
     *   `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `version_id` INT UNSIGNED NOT NULL ,
  `filename` VARCHAR(255) NOT NULL ,
  `schema` ENUM('DataCite', 'DublinCore', 'CSDGM') NOT NULL ,
  `metaVersion` VARCHAR(255) NULL ,
  `serialization` ENUM('anvl', 'json', 'xml') NOT NULL ,
  `value` TEXT NOT NULL ,
     */

    public long id = 0;
    public long objectID = 0;
    public long versionID = 0;
    public String fileName = null;
    public Schema schema = null;
    public String version = null;
    public Serialization serialization = null;
    public String value = null;
    public InputStream inStream = null;
    protected boolean newEntry = false;
    
    
    
    public enum Schema {
        DataCite, DublinCore, CSDGM, EML, OAI_DublinCore, StashWrapper;
    }
    
    public enum Serialization {
        anvl, json, xml;
    }
    
    public InvMeta(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvMeta(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }
    
    public void setStoreMeta(StoreMeta.MatchMeta content, long objectseq, long versionseq)
        throws TException
    {
        if (content == null) return;
        try {
            setObjectID(objectseq);
            setVersionID(versionseq);
            setFileName(content.fileID);
            setSchema(content.schema);
            setVersion(content.metaVersion);
            setSerialization(content.serialization);
            
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
            setSchema(prop.getProperty("md_schema"));
            setVersion(prop.getProperty("version"));
            setSerialization(prop.getProperty("serialization"));
            setValue(prop.getProperty("value"));
            
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
        if (getFileName() != null) prop.setProperty("filename", "" + getFileName());
        if (getSchema() != null) prop.setProperty("md_schema", getSchema().toString());
        if (getVersion() != null) prop.setProperty("version", getVersion());
        if (getSerialization() != null) prop.setProperty("serialization", getSerialization().toString());
        if (getValue() != null) prop.setProperty("value", getValue());
        return prop;
    }
    
    public String getDBName()
    {
        return METADATAS;
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

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    public void setSerialization(String serializationS) {
        if (StringUtil.isEmpty(serializationS)) this.serialization = null;
        this.serialization = Serialization.valueOf(serializationS);
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public void setSchema(String schemaS) {
        if (StringUtil.isEmpty(schemaS)) this.schema = null;
        this.schema = Schema.valueOf(schemaS);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public InputStream getInStream() {
        return inStream;
    }

    public void setInStream(InputStream inStream) {
        this.inStream = inStream;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
    
}

