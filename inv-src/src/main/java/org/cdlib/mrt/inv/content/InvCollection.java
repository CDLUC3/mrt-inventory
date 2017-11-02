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

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvCollection
        extends ContentAbs
{
    private static final String NAME = "InvCollection";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long objectID = 0;
    public Identifier ark = null;
    public String name = null;
    public String mnemonic = null;
    public Priviledge read = null;
    public Priviledge write = null;
    public Priviledge download = null;
    public StorageTier storageTier = null;
    public HarvestPrivilege harvestPrivilege = null;
    public boolean newEntry = false;
    
    
    public enum Priviledge
    {
        prvPublic("public"),
        prvRestricted("restricted");

        protected final String dispPriviledge;
        Priviledge(String dispPriviledge) {
            this.dispPriviledge = dispPriviledge;
        }
        public String toString()
        {
            return dispPriviledge;
        }

        public static Priviledge getType(String t)
        {
            for (Priviledge p : Priviledge.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    public enum StorageTier
    {
        standard("standard"),
        premium("premium");

        protected final String storageTier;
        StorageTier(String storageTier) {
            this.storageTier = storageTier;
        }
        public String toString()
        {
            return storageTier;
        }

        public static StorageTier getType(String t)
        {
            for (StorageTier p : StorageTier.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    public enum HarvestPrivilege
    {
        public_privilege("public"),
        no_privilege("none");

        protected final String harvestPrivilege;
        HarvestPrivilege(String harvestPrivilege) {
            this.harvestPrivilege = harvestPrivilege;
        }
        public String toString()
        {
            return harvestPrivilege;
        }

        public static HarvestPrivilege getType(String t)
        {
            for (HarvestPrivilege p : HarvestPrivilege.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    public InvCollection(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvCollection(Properties prop, LoggerInf logger)
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
            setName(prop.getProperty("name"));
            setMnemonic(prop.getProperty("mnemonic"));
            setRead(prop.getProperty("read_privilege"));
            setWrite(prop.getProperty("write_privilege"));
            setDownload(prop.getProperty("download_privilege"));
            setHarvestPrivilege(prop.getProperty("harvest_privilege"));
            setStorageTier(prop.getProperty("storage_tier"));
        } catch (Exception ex) {
            logger.logError(StringUtil.stackTrace(ex), 2);
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
        if (getName() != null) prop.setProperty("name", getName());
        if (getMnemonic() != null) prop.setProperty("mnemonic", getMnemonic());
        if (getRead() != null) prop.setProperty("read_privilege", getRead().toString());
        if (getWrite() != null) prop.setProperty("write_privilege", getWrite().toString());
        if (getDownload() != null) prop.setProperty("download_privilege", getDownload().toString());
        if (getHarvestPrivilege() != null) prop.setProperty("harvest_privilege", getHarvestPrivilege().toString());
        if (getStorageTier() != null) prop.setProperty("storage_tier", getStorageTier().toString());
        return prop;
    }
    
    public String getDBName()
    {
        return COLLECTIONS;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Priviledge getDownload() {
        return download;
    }

    public void setDownload(Priviledge download) {
        this.download = download;
    }

    public void setDownload(String downloadS)
        throws Exception
    {
        try {
          if (StringUtil.isEmpty(downloadS)) {
            this.download = null;
            return;
            }
            this.download = Priviledge.getType(downloadS);
            
        } catch (Exception ex) {
            logger.logError("setDownload error:" + downloadS, 2);
            throw ex;
        }
    }

    public StorageTier getStorageTier() {
        return storageTier;
    }

    public void setStorageTier(StorageTier storageTier) {
        this.storageTier = storageTier;
    }

    public void setStorageTier(String storageTierS) {
        if (StringUtil.isEmpty(storageTierS)) {
            this.storageTier = null;
            return;
        }
        this.storageTier = StorageTier.valueOf(storageTierS);
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public Priviledge getRead() {
        return read;
    }

    public void setRead(Priviledge read) {
        this.read = read;
    }

    public void setRead(String readS) 
        throws Exception
    {
        try {
            if (StringUtil.isEmpty(readS)) {
                this.read = null;
                return;
            }
            this.read = Priviledge.getType(readS);
        } catch (Exception ex) {
            logger.logError("set Read error:" + readS, 2);
            throw ex;
        }
    }
    

    public Priviledge getWrite() {
        return write;
    }

    public void setWrite(Priviledge write) {
        this.write = write;
    }

    public void setWrite(String writeS)
        throws Exception
    {
        try {
            if (StringUtil.isEmpty(writeS)) {
                this.write = null;
                return;
            }
            this.write = Priviledge.getType(writeS);
            
        } catch (Exception ex) {
            logger.logError("setWrite error:" + writeS, 2);
            throw ex;
        }
    }

    public HarvestPrivilege getHarvestPrivilege() {
        return harvestPrivilege;
    }

    public void setHarvestPrivilege(HarvestPrivilege harvestPrivilege) {
        this.harvestPrivilege = harvestPrivilege;
    }

    public void setHarvestPrivilege(String harvestPrivilegeS)
        throws Exception
    {
        try {
            if (StringUtil.isEmpty(harvestPrivilegeS)) {
                this.harvestPrivilege = null;
                return;
            }
            this.harvestPrivilege = HarvestPrivilege.getType(harvestPrivilegeS);
            
        } catch (Exception ex) {
            logger.logError("setWrite error:" + harvestPrivilegeS, 2);
            throw ex;
        }
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
}

