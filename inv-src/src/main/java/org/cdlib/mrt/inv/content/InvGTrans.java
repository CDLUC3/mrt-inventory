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
public class InvGTrans
        extends ContentAbs
{
    private static final String NAME = "InvUpdate";
    private static final String MESSAGE = NAME + ": ";
    
    public enum GlacierAction {
        add, delete, retrieve, inventory
    } 
    /**
CREATE  TABLE IF NOT EXISTS `inv_glacier` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `requested` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `glacier_action` ENUM('add','delete','retrieve','inventory') NOT NULL ,
  `copy_region` VARCHAR(50) NOT NULL ,
  `jobid` VARCHAR(255) NOT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  `inv_file_id` INT UNSIGNED NULL ,
  `inv_copy_id` INT UNSIGNED NULL ,
  `completed` TIMESTAMP NULL ,
     */
    public long id = 0;
    public DateState requested = null;
    public GlacierAction glacierAction = null;
    public String copyRegion = null;
    public String copyVault = null;
    public String jobid = null;
    public String email = null;
    public long fileid = 0;
    public long copyid = 0;
    public DateState completed = null;
    protected boolean newEntry = false;
    
    
    
    public InvGTrans(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvGTrans(Properties prop, LoggerInf logger)
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
            setRequestedDB(prop.getProperty("requested"));
            setGlacierAction("glacier_action");
            setJobid(prop.getProperty("jobid"));
            setEmail(prop.getProperty("email"));
            setFileid(prop.getProperty("inv_file_id"));
            setCopyid(prop.getProperty("inv_copy_id"));
            setCompletedDB(prop.getProperty("completed"));
            setCopyRegion(prop.getProperty("copy_region"));
            setCopyVault(prop.getProperty("copy_vault"));
            
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
        if (getRequestedDB() != null) prop.setProperty("requested", getRequestedDB());
        if (getGlacierAction() != null) prop.setProperty("glacier_action", getGlacierActionDB());
        if (getJobid() != null) prop.setProperty("jobid", "" + getJobid());
        if (getEmail() != null) prop.setProperty("email", getEmail());
        if (getFileid() != 0) prop.setProperty("inv_file_id", "" + getFileid());
        if (getCopyid() != 0) prop.setProperty("inv_copy_id", "" + getCopyid());
        if (getCompletedDB() != null) prop.setProperty("completed", getCompletedDB());
        if (getCopyRegion() != null) prop.setProperty("copy_region", getCopyRegion());
        if (getCopyVault() != null) prop.setProperty("copy_vault", getCopyVault());
        return prop;
    }
    
    public String dump(String header)
    {
            Properties prop = retrieveProp();
            return PropertiesUtil.dumpProperties(header, prop);
    }
    
    public String getDBName()
    {
        return GTRANS;
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

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    public long getCopyid() {
        return copyid;
    }

    public void setCopyid(long copyid) {
        this.copyid = copyid;
    }
    
    public void setCopyid(String copyidS) 
    {
        this.copyid = setNum(copyidS);
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

    public DateState getRequested() {
        return requested;
    }

    public String getRequestedDB() {
        if (requested == null) return null;
        return InvUtil.getDBDate(requested);
    }

    public void setRequested(DateState requested) {
        this.requested = requested;
    }

    public void setRequested(String requestedS) {
        if (StringUtil.isAllBlank(requestedS)) this.requested = new DateState();
        this.requested = new DateState(requestedS);
    }

    public void setRequestedDB(String requestedS) {
        if (StringUtil.isAllBlank(requestedS)) {
            this.requested = new DateState();
            return;
        }
        this.requested = InvUtil.setDBDate(requestedS);
    }

    public String getCompletedDB() {
        if (completed == null) return null;
        return InvUtil.getDBDate(completed);
    }

    public void setCompleted() {
        this.completed = new DateState();
    }

    public void setCompleted(DateState completed) {
        this.completed = completed;
    }

    public void setCompleted(String completedS) {
        if (StringUtil.isAllBlank(completedS)) this.completed = new DateState();
        this.completed = new DateState(completedS);
    }

    public void setCompletedDB(String completedS) {
        if (StringUtil.isAllBlank(completedS)) {
            this.completed = new DateState();
            return;
        }
        this.completed = InvUtil.setDBDate(completedS);
    }


    public String getCopyRegion() {
        return copyRegion;
    }

    public void setCopyRegion(String copyRegion) {
        this.copyRegion = copyRegion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    public GlacierAction getGlacierAction() {
        return glacierAction;
    }

    public String getGlacierActionDB() {
        if (glacierAction == null) return null;
        return glacierAction.toString();
    }

    public void setGlacierAction(GlacierAction glacierAction) {
        this.glacierAction = glacierAction;
    }

    public void setGlacierAction(String glacierActionS) {
        this.glacierAction = GlacierAction.valueOf(glacierActionS);
    }

    public String getCopyVault() {
        return copyVault;
    }

    public void setCopyVault(String copyVault) {
        this.copyVault = copyVault;
    }
    
}

