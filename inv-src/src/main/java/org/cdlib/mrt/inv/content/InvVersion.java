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
public class InvVersion
        extends ContentAbs
{
    private static final String NAME = "InvVersion";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long objectID = 0;
    public Identifier ark = null;
    public long number = 0;
    public String note = null;
    public DateState created = null;
    public boolean newEntry = false;
    
    
    public InvVersion(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvVersion(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setObjectID(prop.getProperty("inv_object_id"));
            setArk(prop.getProperty("ark"));
            setNumber(prop.getProperty("number"));
            setNote(prop.getProperty("note"));
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
        if (getArk() != null) prop.setProperty("ark", getArk().getValue());
        if (getNumber() != 0) prop.setProperty("number", "" + getNumber());
        if (getNote() != null) prop.setProperty("note", getNote());
        return prop;
    }
    
    public String getDBName()
    {
        return VERSIONS;
    }

    public Identifier getArk() {
        return ark;
    }

    public void setArk(Identifier ark) {
        this.ark = ark;
    }

    public void setArk(String arkS)
        throws TException
    {
        this.ark = new Identifier(arkS);
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

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public void setNumber(String numberS) {
        this.number = setNum(numberS);
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }


}

