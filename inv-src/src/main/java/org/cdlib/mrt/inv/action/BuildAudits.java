/******************************************************************************
Copyright (c) 2005-2012, Regents of the University of California
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
*******************************************************************************/
package org.cdlib.mrt.inv.action;

import java.sql.Connection;
import java.util.List;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvFile;
import org.cdlib.mrt.inv.content.InvAudit;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvNodeObject;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Run fixity
 * @author dloy
 */
public class BuildAudits
        extends InvActionAbs
{

    protected static final String NAME = "BuildAudits";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;

    protected Identifier objectID = null;
    protected long objectseq = 0;
    protected long addNodeseq = 0;
    protected InvObject invObject = null;
    protected List<InvNodeObject> nodeObjects = null;
    protected List<InvFile> files = null;
    protected boolean commit = false;
    protected String storageBase = null;
    protected DBAdd dbAdd = null;
    protected long auditCnt = 0;
    
    /**
     * 
     * @param storageBase URL base used by fixity to validate content
     * @param objectID object identifier
     * @param connection inv db connector
     * @param logger general logger
     * @return build state
     * @throws TException 
     */
    public static BuildAudits getBuildAudits(
            String storageBase,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new BuildAudits(storageBase, objectID, connection, logger);
    }
    
    protected BuildAudits(
            String storageBase,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        try {
            this.objectID = objectID;
            this.storageBase = getStorageBase(storageBase, objectID, connection, logger);
            if (StringUtil.isAllBlank(this.storageBase)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "Missing required value: storageBase");
            }
            String msg = "BuildAudits URL:"
                        + " - storageBase=" + storageBase 
                        + " - objectID=" + objectID.getValue()
                    ;
            if (DEBUG) {
                System.out.println(msg);
            }
            logger.logMessage(msg, 2, true);
            dbAdd = new DBAdd(connection, logger);
        
        } catch (Exception ex) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ex2) { }
            
            if (ex instanceof TException) {
                throw (TException) ex;
            }
            else throw new TException(ex);
        }
    }
    
    private String getStorageBase(
            String storageBase,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        if (DEBUG) System.out.println("+++getStorageBase entered");
        try {
            if (StringUtil.isNotEmpty(storageBase)) {
                return storageBase;
            }
            return InvDBUtil.getStorageBase( objectID, connection, logger);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void process()
        throws TException
    {
        try {
            connection.setAutoCommit(false);
            setObject();
            connection.commit();
            commit = true;

        } catch (Exception ex) {
            String msg = MESSAGE + "Exception for entry id=" + objectID.getValue()
                    + " - Exception:" + ex
                    ;
            System.out.println("EXception:" + msg);
            logger.logError(msg, 2);
            logger.logError(StringUtil.stackTrace(ex),3);
            try {
                connection.rollback();
            } catch (Exception cex) {
                System.out.println("WARNING: rollback Exception:" + cex);
            }
            if (ex instanceof TException) {
                throw (TException) ex;
            } else {
                throw new TException (ex);
            }

        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }

    }
    
    protected void setObject()
        throws TException
    {
        if (DEBUG) System.out.println("+++setObject entered");
        try {
            invObject = InvDBUtil.getObject(objectID, connection, logger);
            if (invObject == null) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND(
                        MESSAGE + "objectID not found:" + objectID.getValue());
                
            }
            objectseq = invObject.getId();
            nodeObjects = InvDBUtil.getObjectNodes(objectseq, connection, logger);
            if (nodeObjects == null) {
                throw new TException.INVALID_ARCHITECTURE(MESSAGE + "node not found for object:" + objectID.getValue()
                        + " - objectseq=" + objectseq
                        );
            }
            files = InvDBUtil.getObjectFiles(objectseq, connection, logger);
            if (files == null) {
                throw new TException.INVALID_ARCHITECTURE(MESSAGE + "no files found for object:" + objectID.getValue()
                        + " - objectseq=" + objectseq
                        );
            }
            build();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }

    public boolean isCommit() {
        return commit;
    }
    
    public void build()
        throws TException
    {
        try {
            if (DEBUG) System.out.println("+++build entered" 
                    + " - nodeObjects.size=" + nodeObjects.size()
                    + " - files.size=" + files.size()
                    );
            for (InvNodeObject nodeObject : nodeObjects) {
                long nodeseq = nodeObject.nodesid;
                for (InvFile file : files) {
                    long billableSize = file.getBillableSize();
                    if (billableSize > 0) addAudit(nodeObject, file);
                }
            }
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    
    public void addAudit(InvNodeObject invNodeObject, InvFile file)
        throws TException
    {
        if (DEBUG) System.out.println("+++addAudit entered:"
                + " - nodeid=" + invNodeObject.getId()
                + " - fileid=" + file.getId()
                );
        try {
            log("addAudit entered");
            long nodeseq = invNodeObject.getNodesid();
            InvAudit audit = InvDBUtil.getAudit(nodeseq, objectseq, file.getId(), connection, logger);
            if (audit != null) return;
            InvNode invNode = InvDBUtil.getNodeFromId(nodeseq, connection, logger);
            if (invNode == null) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND("Node not found for seq:" + invNodeObject.getId());
            }
            audit = new InvAudit(logger);
            audit.setNodeid(nodeseq);
            audit.setObjectid(objectseq);
            audit.setVersionid(file.getVersionID());
            audit.setFileid(file.getId());
            String url = buildMerrittURL(invNode, file);
            audit.setUrl(url);
            log(invObject.dump("Created object"));
            long auditseq = dbAdd.replace(audit);
            logger.logMessage("Create inv_audit url=" + url, 1);
            if (DEBUG) System.out.println(audit.dump("+++created object+++"));
            
            audit.setId(auditseq);
            auditCnt++;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public String buildMerrittURL(InvNode invNode, InvFile file)
        throws TException
    {
        try {
            long versionseq = file.getVersionID();
            InvVersion invVersion = InvDBUtil.getVersionFromId(versionseq, connection, logger);
            if (invVersion == null) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND("Version not found for seq:" + versionseq);
            }
            String url = storageBase + "/" + "content/" + invNode.getNumber() + "/"
                            + URLEncoder.encode(objectID.getValue(), "utf-8") 
                            + "/" + invVersion.getNumber()
                            + "/" + URLEncoder.encode(file.getPathName(), "utf-8") 
                            + "?fixity=no"
                            ;
            return url;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }

    public long getAuditCnt() {
        return auditCnt;
    }
    
}