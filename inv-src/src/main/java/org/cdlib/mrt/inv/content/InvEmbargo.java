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

import org.cdlib.mrt.inv.extract.StoreOwner;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvEmbargo
        extends ContentAbs
{
    private static final String NAME = "InvEmbargo";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    

    public long id = 0;
    public long objectsid = 0;
    public Identifier ark = null;
    public DateState embargoEndDate = null;
    public boolean newEntry = false;
    
    public static InvEmbargo getInvEmbargoFromTxt(
            long objectseq, Properties prop, LoggerInf logger)
        throws TException
    {
        InvEmbargo invEmbargo = new InvEmbargo(logger);
        
        invEmbargo.setFromEmbargoTxt(prop);
        invEmbargo.setObjectsid(objectseq);
        if (invEmbargo.getEmbargoEndDate() == null) {
            return null;
        }
        return invEmbargo;
    }
    
    public InvEmbargo(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvEmbargo(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setFromEmbargoTxt(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setEmbargoZEndDate(prop.getProperty("embargoEndDate"));
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
            setObjectsid(prop.getProperty("inv_object_id"));
            setEmbargoEndDateDB(prop.getProperty("embargo_end_date"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getObjectsid() != 0) prop.setProperty("inv_object_id", "" + getObjectsid());
        if (getEmbargoEndDate() != null) prop.setProperty("embargo_end_date", "" + getEmbargoEndDatDB());
        return prop;
    }
    
    public String dump(String header)
    {
        return PropertiesUtil.dumpProperties(header, retrieveProp());
    }
    
    public String getDBName()
    {
        return EMBARGOES;
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

    public void setEmbargoEndDate(DateState embargoEndDate) {
        this.embargoEndDate = embargoEndDate;
    }


    public DateState getEmbargoEndDate() {
        return this.embargoEndDate;
    }

    public String getEmbargoEndDatDB() {
        if (embargoEndDate == null) return null;
        return InvUtil.getDBDate(embargoEndDate);
    }

    public void setEmbargoEndDateDB(String embargoEndDateDB) {
        if (embargoEndDateDB == null) this.embargoEndDate = null;
        this.embargoEndDate = InvUtil.setDBDate(embargoEndDateDB);
    }

    public void setEmbargoZEndDate(String embargoZEndDate) {
        if (embargoZEndDate == null) {
            this.embargoEndDate = null;
            return;
        }
        if (embargoZEndDate.length() < 5) {
            String test = embargoZEndDate.toLowerCase();
            if (test.equals("none"))  {
                this.embargoEndDate = null;
                return;
            }
        }
        this.embargoEndDate = InvUtil.setDBZDate(embargoZEndDate);
    }


    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
}

