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


import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvGCopy
        extends ContentAbs
{
    private static final String NAME = "InvCopy";
    private static final String MESSAGE = NAME + ": ";
    /**
CREATE  TABLE IF NOT EXISTS `inv_copies` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `inv_version_id` INT UNSIGNED NOT NULL ,
  `inv_file_id` INT UNSIGNED NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `copy_region` VARCHAR(50) NOT NULL ,
  `copy_key` VARCHAR(255) NOT NULL ,
     */
    public long id = 0;
    public long objectid = 0;
    public long versionid = 0;
    public long fileid = 0;
    public DateState created = null;
    public String copyRegion = null;
    public String copyVault = null;
    public String copyKey = null;
    public String cloudKey = null;
    protected boolean newEntry = false;
    
    
    
    public InvGCopy(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvGCopy(Properties prop, LoggerInf logger)
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
            setObjectid(prop.getProperty("inv_object_id"));
            setVersionid(prop.getProperty("inv_version_id"));
            setFileid(prop.getProperty("inv_file_id"));
            setCreatedDB(prop.getProperty("created"));
            setCopyRegion(prop.getProperty("copy_region"));
            setCopyVault(prop.getProperty("copy_vault"));
            setCopyKey(prop.getProperty("copy_key"));
            setCloudKey(prop.getProperty("cloud_key"));
            
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
        if (getObjectid() != 0) prop.setProperty("inv_object_id", "" + getObjectid());
        if (getVersionid() != 0) prop.setProperty("inv_version_id", "" + getVersionid());
        if (getFileid() != 0) prop.setProperty("inv_file_id", "" + getFileid());
        if (getCreated() != null) prop.setProperty("created", getCreatedDB());
        if (getCopyRegion() != null) prop.setProperty("copy_region", getCopyRegion());
        if (getCopyVault() != null) prop.setProperty("copy_vault", getCopyVault());
        if (getCopyKey() != null) prop.setProperty("copy_key", getCopyKey());
        if (getCloudKey() != null) prop.setProperty("cloud_key", getCloudKey());
        return prop;
    }
    
    public String dump(String header)
    {
            Properties prop = retrieveProp();
            return PropertiesUtil.dumpProperties(header, prop);
    }
    
    public String getDBName()
    {
        return GCOPIES;
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
        this.versionid = setNum(versionidS);
    }

    public long getFileid() {
        return fileid;
    }

    public void setFileid(String fileidS) 
    {
        this.fileid = setNum(fileidS);
    }

    public void setFileid(long fileid) {
        this.fileid = fileid;
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


    public String getCopyRegion() {
        return copyRegion;
    }

    public void setCopyRegion(String copyRegion) {
        this.copyRegion = copyRegion;
    }

    public String getCopyKey() {
        return copyKey;
    }

    public void setCopyKey(String copyKey) {
        this.copyKey = copyKey;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    public String getCopyVault() {
        return copyVault;
    }

    public void setCopyVault(String copyVault) {
        this.copyVault = copyVault;
    }

    public String getCloudKey() {
        return cloudKey;
    }

    public void setCloudKey(String cloudKey) {
        this.cloudKey = cloudKey;
    }
    
}

