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
import java.util.Properties;
import org.cdlib.mrt.cloud.CloudList;


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.FixityStatusType;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.MessageDigestValue;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.StateInf;
/**
 * Container class for inv Object content
 * @author dloy
 * CREATE TABLE `inv_storage_scans` (
	`id` INT(10) NOT NULL AUTO_INCREMENT,
	`inv_node_id` SMALLINT(5) UNSIGNED NOT NULL,
	`created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`scan_status` ENUM('pending','started','completed','cancelled','failed','unknown') NOT NULL DEFAULT unknown COLLATE 'utf8mb4_general_ci',
	`scan_type` ENUM('list','next','delete','build','unknown') NOT NULL DEFAULT unknown COLLATE 'utf8mb4_general_ci',
	`keys_processed` BIGINT(20) NOT NULL DEFAULT '0',
	`key_list_name` VARCHAR(255) NULL COLLATE 'utf8mb4_unicode_ci',
	`last_s3_key` MEDIUMTEXT NOT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `scan_type_idx` (`scan_type`) USING BTREE,
	INDEX `scan_status_idx` (`scan_status`) USING BTREE,
	INDEX `inv_scans_node_id_ibfk_3` (`inv_node_id`) USING BTREE,
	CONSTRAINT `inv_scans_node_id_ibfk_3` FOREIGN KEY (`inv_node_id`) REFERENCES `inv`.`inv_nodes` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;
 */
public class InvStorageScan
        extends ContentAbs
        implements StateInf
{
    private static final String NAME = "InvStorageScan";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = true;
    
    public enum ScanStatus {pending, started, completed, cancelled, failed, unknown};
    
    public enum ScanType {list, next, delete, unknown};
    
    protected long id = 0;
    protected long nodeid = 0;
    protected DateState created = null;
    protected DateState updated = null;
    protected ScanStatus scanStatus = ScanStatus.unknown;
    protected ScanType scanType = ScanType.unknown;
    protected Long keysProcessed = null;
    protected String lastS3Key = null;
    protected String keyListName = null;
    protected boolean newEntry = false;
    
    
    
    public InvStorageScan(LoggerInf logger)
        throws TException
    {
        super(logger);
    }
    
    public InvStorageScan(Properties prop, LoggerInf logger)
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
        if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("setProp", prop));
        try {
            setId(prop.getProperty("id"));
            setNodeid(prop.getProperty("inv_node_id"));
            setCreatedDB(prop.getProperty("created"));
            setUpdatedDB(prop.getProperty("updated"));
            setScanStatusDB(prop.getProperty("scan_status"));
            setScanTypeDB(prop.getProperty("scan_type"));
            setKeysProcessed(prop.getProperty("keys_processed"));
            setLastS3Key(prop.getProperty("last_s3_key"));
            setKeyListName(prop.getProperty("key_list_name"));
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
        if (getNodeid() != 0) prop.setProperty("inv_node_id", "" + getNodeid());
        if (getCreated() != null) prop.setProperty("created", getCreatedDB());
        if (getUpdated() != null) prop.setProperty("updated", getUpdatedDB());
        prop.setProperty("scan_status", "" + getScanStatus());
        prop.setProperty("scan_type", "" + getScanType());
        if (getKeysProcessed() != null) prop.setProperty("keys_processed", "" + getKeysProcessed());
        if (getLastS3Key() != null) prop.setProperty("last_s3_key", getLastS3Key());
        if (getKeyListName() != null) prop.setProperty("key_list_name", getKeyListName());
        return prop;
    }
    
    public String dump(String header)
    {
            Properties prop = retrieveProp();
            return PropertiesUtil.dumpProperties(header, prop);
    }
    
    public String getDBName()
    {
        return "inv_storage_scans";
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

    public long getNodeid() {
        return nodeid;
    }

    public void setNodeid(long nodeid) {
        this.nodeid = nodeid;
    }

    public void setNodeid(String nodeidS) 
    {
        this.nodeid = setNum(nodeidS);
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

    public void setCreatedDB() {
         this.created = new DateState();
    }

    public DateState getUpdated() {
        return updated;
    }

    public String getUpdatedDB() {
        if (updated == null) return null;
        return InvUtil.getDBDate(updated);
    }

    public void setUpdated(DateState updated) {
        this.updated = updated;
    }

    public void setUpdated(String updatedS) {
        if (StringUtil.isAllBlank(updatedS)) this.updated = new DateState();
        this.updated = new DateState(updatedS);
    }

    public void setUpdatedDB(String updatedS) {
        if (StringUtil.isAllBlank(updatedS)) {
            this.updated = new DateState();
            return;
        }
        this.updated = InvUtil.setDBDate(updatedS);
    }

    public void setUpdatedDB() {
            this.updated = new DateState();
    }
    
    public ScanStatus getScanStatus() {
        return scanStatus;
    }

    public void setScanStatusDB(String scanStatusS) {
        if (StringUtil.isEmpty(scanStatusS)) {
            this.scanStatus = null;
            return;
        }
        //this.maintStatus = MaintStatus.getFixityStatusType(statusS);
        this.scanStatus = ScanStatus.valueOf(scanStatusS);
    }
    
    public ScanType getScanType() {
        return scanType;
    }

    public void setScanTypeDB(String scanTypeS) {
        if (StringUtil.isEmpty(scanTypeS)) {
            this.scanType = null;
            return;
        }
        //this.maintStatus = MaintStatus.getFixityStatusType(statusS);
        this.scanType = ScanType.valueOf(scanTypeS);
    }

    public Long getKeysProcessed() {
        return keysProcessed;
    }

    public void setKeysProcessed(Long keysProcessed) {
        this.keysProcessed = keysProcessed;
    }

    public void setKeysProcessed(String keysProcessedS) {
        this.keysProcessed = setNum(keysProcessedS);
    }

    public String getLastS3Key() {
        return lastS3Key;
    }

    public void setLastS3Key(String lastS3Key) {
        this.lastS3Key = lastS3Key;
    }

    public String getKeyListName() {
        return keyListName;
    }

    public void setKeyListName(String keyListName) {
        this.keyListName = keyListName;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
}

