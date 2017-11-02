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


import org.cdlib.mrt.inv.extract.StoreERC;
import org.cdlib.mrt.utility.DOMParser;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for DC content
 * @author dloy
 */
public class InvDK
    extends ContentAbs
{
    private static final String NAME = "InvDK";
    private static final String MESSAGE = NAME + ": ";
    
    public long id = 0;
    public long objectID = 0;
    public long versionID = 0;
    public long seq = 0;
    public String element = null;
    public String qualifier = null;
    public String value = null;
    public boolean newEntry = false;

    public InvDK(Properties prop, LoggerInf logger)
    {
        super(logger);
        setProp(prop);
    }

    public InvDK(LoggerInf logger)
    {
        super(logger);
    }

    public void setProp(Properties prop)
    {
        if ((prop == null) || (prop.size() == 0)) return;
        setId(prop.getProperty("id"));
        setObjectID(prop.getProperty("inv_object_id"));
        setVersionID(prop.getProperty("inv_version_id"));
        setSeq(prop.getProperty("seq_num"));
        setElement(prop.getProperty("element"));
        setQualifier(prop.getProperty("qualifier"));
        setValue(prop.getProperty("value"));
    }

    public Properties retrieveProp()
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getObjectID() != 0) prop.setProperty("inv_object_id", "" + getObjectID());
        if (getVersionID() != 0) prop.setProperty("inv_version_id", "" + getVersionID());
        if (getSeq() != 0) prop.setProperty("seq_num", "" + getSeq());
        if (getElement() != null) prop.setProperty("element", getElement());
        if (getQualifier() != null) prop.setProperty("qualifier", getQualifier());
        if (getValue() != null) prop.setProperty("value", getValue());
        return prop;
    }
    
    public String getDBName()
    {
        return DUBLINKERNELS;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(String idS) {
        id = setNum(idS);
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

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public void setSeq(String seqS) {
        this.seq = setNum(seqS);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
    
    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

}

