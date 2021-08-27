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
import static org.cdlib.mrt.inv.content.InvObject.DEBUG;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.MessageDigestValue;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 * CREATE TABLE `inv_storage_maints` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`inv_node_id` SMALLINT(5) UNSIGNED NOT NULL,
        `keymd5` CHAR(32) NOT NULL,
	`size` BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',
	`created` TIMESTAMP NULL,
	`logged` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`removed` TIMESTAMP NULL,
	`maint_status` ENUM('review','hold','delete','removed','objremoved','error','unknown') NOT NULL DEFAULT 'unknown',
	`maint_type` ENUM('non-ark','missing-ark','missing-file','unknown') NOT NULL DEFAULT 'unknown',
	`key` LONGTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	`note` TEXT(65535) NULL DEFAULT NULL COLLATE 'utf8_general_ci',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `inv_node_id` (`inv_node_id`) USING BTREE,
	INDEX `keymd5_idx` (`keymd5`) USING BTREE,
	INDEX `type_idx` (`maint_type`) USING BTREE,
	INDEX `status_idx` (`maint_status`) USING BTREE,
	CONSTRAINT `inv_scans_ibfk_1` FOREIGN KEY (`inv_node_id`) REFERENCES `inv`.`inv_nodes` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;
 */
public class InvStorageMaint
        extends ContentAbs
{
    private static final String NAME = "InvStorageMaint";
    private static final String MESSAGE = NAME + ": ";
    
    public enum MaintStatus {review, hold, delete, removed, objremoved, note, admin, error, unknown};
    
    public enum MaintAdmin {none, run, stop, eof};
    
    public enum MaintType
    {
        mtNonArk("non-ark"),
        mtMissArk("missing-ark"),
        mrtMissFile("missing-file"),
        mrtObject("object-delete"),
        mrtOK("OK"),
        mrtUnknown("unknown"),
        mrtAdmin("admin")
        ;

        protected final String dispMaintType;
        MaintType(String dispMaintType) {
            this.dispMaintType = dispMaintType;
        }
        public String toString()
        {
            return dispMaintType;
        }

        public static MaintType getMaintType(String t)
        {
            for (MaintType p : MaintType.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    protected long id = 0;
    protected long nodeid = 0;
    protected String key = null;
    protected String keyMd5 = null;
    protected Long size = null;
    protected DateState fileCreated = null;
    protected DateState created = null;
    protected DateState removed = null;
    protected MaintStatus maintStatus = MaintStatus.unknown;
    protected MaintType maintType = MaintType.mrtUnknown;
    protected MaintAdmin maintAdmin = MaintAdmin.none;
    protected String note = null;
    protected boolean newEntry = false;
    
    
    
    public InvStorageMaint(LoggerInf logger)
        throws TException
    {
        super(logger);
    }
    
    public InvStorageMaint(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }
    
    public InvStorageMaint(long nodeID, InvStorageMaint.MaintType type, CloudList.CloudEntry entry, LoggerInf logger)
       throws TException
    {
        super(logger);
        this.maintType = type;
        if (DEBUG) System.out.println(entry.dump("InvStorageMaint"));
        this.key = entry.getKey();
        this.size = entry.getSize();
        this.nodeid = nodeID;
        String lastModifiedS = entry.getLastModified();
        DateState lastModified = new DateState(lastModifiedS);
        this.fileCreated = lastModified;
        this.created = new DateState();
        buildKeyMd5();
    }
    
    public void setAdmin(long nodeid)
        throws TException
    {
        this.nodeid = nodeid;
        this.maintType = MaintType.mrtAdmin;
        this.maintStatus = MaintStatus.admin;
        this.maintAdmin = MaintAdmin.stop;
        this.fileCreated = new DateState();
        this.created = new DateState();
        this.key = " ";
        this.keyMd5 = "00000000000000000000000000000000";
        
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
            setKey(prop.getProperty("s3key"));
            setKeyMd5(prop.getProperty("keymd5"));
            setFileCreatedDB(prop.getProperty("file_created"));
            setCreatedDB(prop.getProperty("created"));
            setRemovedDB(prop.getProperty("file_removed"));
            setMaintStatusDB(prop.getProperty("maint_status"));
            setMaintTypeDB(prop.getProperty("maint_type"));
            setMaintAdminDB(prop.getProperty("maint_admin"));
            setNote(prop.getProperty("note"));
            setSize(prop.getProperty("size"));
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
        if (getKey() != null) prop.setProperty("s3key", getKey());
        if (getKeyMd5() != null) prop.setProperty("keymd5", getKeyMd5());
        if (getFileCreated() != null) prop.setProperty("file_created", getFileCreatedDB());
        if (getCreated() != null) prop.setProperty("created", getCreatedDB());
        if (getRemoved() != null) prop.setProperty("file_removed", getRemovedDB());
        if (getSize() != null) prop.setProperty("size", "" + getSize());
        prop.setProperty("maint_status", "" + getMaintStatus());
        prop.setProperty("maint_type", "" + getMaintType());
        prop.setProperty("maint_admin", "" + getMaintAdmin());
        if (getNote() != null) prop.setProperty("note", getNote());
        else prop.setProperty("note", "");
        return prop;
    }
    
    public String dump(String header)
    {
            Properties prop = retrieveProp();
            return PropertiesUtil.dumpProperties(header, prop);
    }
    
    public String getDBName()
    {
        return "inv_storage_maints";
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

    public DateState getFileCreated() {
        return fileCreated;
    }

    public String getFileCreatedDB() {
        if (fileCreated == null) return null;
        return InvUtil.getDBDate(fileCreated);
    }

    public void setFileCreated(DateState created) {
        this.fileCreated = created;
    }

    public void setFileCreated(String fileCreatedS) {
        if (StringUtil.isAllBlank(fileCreatedS)) this.fileCreated = new DateState();
        this.fileCreated = new DateState(fileCreatedS);
    }

    public void setFileCreatedDB(String fileCreatedS) {
        if (StringUtil.isAllBlank(fileCreatedS)) {
            this.fileCreated = new DateState();
            return;
        }
        this.fileCreated = InvUtil.setDBDate(fileCreatedS);
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


    public DateState getRemoved() {
        return removed;
    }

    public String getRemovedDB() {
        if (removed == null) return null;
        return InvUtil.getDBDate(removed);
    }

    public void setRemoved(DateState removed) {
        this.removed = removed;
    }
    
    public MaintStatus getMaintStatus() {
        return maintStatus;
    }
    
    public MaintType getMaintType() {
        return maintType;
    }

    public void setRemoved(String removedS) {
        if (StringUtil.isAllBlank(removedS)) this.removed = new DateState();
        this.removed = new DateState(removedS);
    }

    public void setRemovedDB(String removedS) {
        if (StringUtil.isAllBlank(removedS)) {
            this.removed = new DateState();
            return;
        }
        this.removed = InvUtil.setDBDate(removedS);
    }

    public void setMaintStatus(MaintStatus maintStatus) {
        this.maintStatus = maintStatus;
    }

    public void setMaintStatus(String maintStatusS) {
        if (StringUtil.isEmpty(maintStatusS)) {
            this.maintStatus = null;
            return;
        }
        this.maintStatus = MaintStatus.valueOf(maintStatusS);
    }

    public void setMaintStatusDB(String maintStatusS) {
        if (StringUtil.isEmpty(maintStatusS)) {
            this.maintStatus = null;
            return;
        }
        //this.maintStatus = MaintStatus.getFixityStatusType(statusS);
        this.maintStatus = MaintStatus.valueOf(maintStatusS);
    }

    public void setMaintType(MaintType maintType) {
        this.maintType = maintType;
    }

    public void setMaintType(String maintTypeS) {
        if (StringUtil.isEmpty(maintTypeS)) {
            this.maintType = null;
            return;
        }
        this.maintType = MaintType.valueOf(maintTypeS);
    }

    public void setMaintTypeDB(String maintTypeS) {
        if (StringUtil.isEmpty(maintTypeS)) {
            this.maintType = null;
            return;
        }
        this.maintType = MaintType.getMaintType(maintTypeS);
    }

    public MaintAdmin getMaintAdmin() {
        return maintAdmin;
    }

    public void setMaintAdmin(MaintAdmin maintAdmin) {
        this.maintAdmin = maintAdmin;
    }

    public void setMaintAdminDB(String maintAdminS) {
        if (StringUtil.isEmpty(maintAdminS)) {
            this.maintAdmin = null;
            return;
        }
        this.maintAdmin = MaintAdmin.valueOf(maintAdminS);
    }

    public String getKey() {
        return key;
    }

    public String getKeyMd5() {
        return keyMd5;
    }

    public void setKeyMd5(String keyMd5) {
        this.keyMd5 = keyMd5;
    }
    
    public void setKey(String key) {
        this.key = key;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getSize() {
        return size;
    }

    public void setBillableSize(long billableSize) {
        this.size = size;
    }

    public void setSize(String sizeS) {
        this.size = setNum(sizeS);
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
    
    public void buildKeyMd5()
         throws TException
    {
        keyMd5 = buildMd5(key, logger);
    }
    
    public static String buildMd5(String inS, LoggerInf logger)
         throws TException
    {
        try {
            if (inS == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "buildMd5 input value required");
            }
            byte[] bytes = inS.getBytes("utf-8");
            InputStream stream = new ByteArrayInputStream(bytes);
            MessageDigestValue mdv = new MessageDigestValue(stream, "md5", logger);
            return mdv.getChecksum();
            
        } catch (TException tex) {
           throw tex;
           
        } catch (Exception ex) {
           throw new TException(ex);
        }
        
    }
}

