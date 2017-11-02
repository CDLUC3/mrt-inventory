/*
Copyright (c) 2005-2016, Regents of the University of California
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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.MessageDigestValue;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvLocalID
        extends ContentAbs
{
    private static final String NAME = "InvLocalID";
    private static final String MESSAGE = NAME + ": ";
    private static final int MAXW = 5394;
    

    public long id = 0;
    public Identifier objectArk = null;
    public Identifier ownerArk = null;
    public String localID = null;
    public DateState created = null;
    protected boolean newEntry = false;
    
    public InvLocalID(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvLocalID(
            String objectArkS,
            String ownerArkS,
            String localID,
            LoggerInf logger)
        throws TException
    {
        super(logger);
        setObjectArk(objectArkS);
        setOwnerArk(ownerArkS);
        setLocalID(localID);
        validateFullMap();
    }
    
    public InvLocalID(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }
    
    private void validateFullMap()
        throws TException
    {
        if (getObjectArk() == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "objectArk missing");
        }
        if (getOwnerArk() == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "ownerArk missing");
        }
        if (StringUtil.isAllBlank(getLocalID())) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "localID missing");
        }
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setObjectArk(prop.getProperty("inv_object_ark"));
            setOwnerArk(prop.getProperty("inv_owner_ark"));
            setLocalID(prop.getProperty("local_id"));
            setCreatedDB(prop.getProperty("created"));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getObjectArk() != null) prop.setProperty("inv_object_ark", getObjectArk().getValue());
        if (getOwnerArk() != null) prop.setProperty("inv_owner_ark", getOwnerArk().getValue());
        if (getLocalID() != null) prop.setProperty("local_id", getLocalID());
        setCreated();
        prop.setProperty("created", getCreatedDB());
        return prop;
    }
    
    public String getDBName()
    {
        return LOCALS;
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

    public Identifier getObjectArk() {
        return objectArk;
    }

    public void setObjectArk(Identifier objectArk) {
        this.objectArk = objectArk;
    }

    public void setObjectArk(String objectArkS)
        throws TException
    {
        this.objectArk = sToId(objectArkS);
    }

    public Identifier getOwnerArk() {
        return ownerArk;
    }

    public void setOwnerArk(Identifier ownerArk) {
        this.ownerArk = ownerArk;
    }

    public void setOwnerArk(String ownerArkS)
        throws TException
    {
        this.ownerArk = sToId(ownerArkS);
    }

    public String getLocalID() {
        return localID;
    }

    public void setLocalID(String localID) {
        this.localID = localID;
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

    public void setCreated() {
        this.created = new DateState();
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

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    protected Identifier pToId(Properties prop, String name)
        throws TException
    {
        if (prop == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "prop missing");
        }
        if (StringUtil.isAllBlank(name)) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "name missing");
        }
        String value = prop.getProperty(name);
        if (StringUtil.isAllBlank(value)) {
            return null;
        }
        Identifier retIdentifier = new Identifier(value);
        return retIdentifier;
    }

    protected Identifier sToId(String value)
        throws TException
    {
        if (StringUtil.isAllBlank(value)) {
            return null;
        }
        Identifier retIdentifier = new Identifier(value);
        return retIdentifier;
    }
}

