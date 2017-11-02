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

import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.cloud.MatchMap;
import org.cdlib.mrt.cloud.VersionMap;
import org.cdlib.mrt.core.DC;
import org.cdlib.mrt.core.DataciteConvert;
import org.cdlib.mrt.inv.content.ContentAbs;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvCollectionObject;
import org.cdlib.mrt.inv.content.InvDK;
import org.cdlib.mrt.inv.content.InvDKVersion;
import org.cdlib.mrt.inv.content.InvDua;
import org.cdlib.mrt.inv.content.InvEmbargo;
import org.cdlib.mrt.inv.content.InvFile;
import org.cdlib.mrt.inv.content.InvAudit;
import org.cdlib.mrt.inv.content.InvIngest;
import org.cdlib.mrt.inv.content.InvMeta;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvNodeObject;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.inv.extract.StoreCollections;
import org.cdlib.mrt.inv.extract.StoreDua;
import org.cdlib.mrt.inv.extract.StoreDuaTemplate;
import org.cdlib.mrt.inv.extract.StoreEmbargo;
import org.cdlib.mrt.inv.extract.StoreERC;
import org.cdlib.mrt.inv.extract.StoreIngest;
import org.cdlib.mrt.inv.extract.StoreMeta;
import org.cdlib.mrt.inv.extract.StoreMom;
import org.cdlib.mrt.inv.extract.StoreOwner;
import org.cdlib.mrt.inv.extract.StoreState;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.DBDelete;
import org.cdlib.mrt.inv.extract.StoreFile;
import org.cdlib.mrt.inv.service.InvProcessState;
import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.core.Tika;
import static org.cdlib.mrt.inv.action.InvActionAbs.getVersionMap;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;
import org.cdlib.mrt.utility.XMLUtil;

/**
 * Run fixity
 * @author dloy
 */
public class SaveObject
        extends InvActionAbs
{

    protected static final String NAME = "SaveObject";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;

    protected VersionMap versionMap = null;
    protected VersionMap copyVersionMap = null;
    protected boolean commit = false;
    protected int node = 0;
    protected int toNode = 0;
    protected Identifier objectID = null;
    protected long objectseq = 0;
    protected String storageBase = null;
    protected DBAdd dbAdd = null;
    protected Tika tika = null;
    protected HashMap<String,String> mimeList = new HashMap();
    protected InvObject invObject = null;
    protected String ingestURL = null;
    protected Role role = null;
    protected Role copyRole = null;
    protected TallyTable tally = new TallyTable();
    protected long addNodeseq = 0;
    protected String method = null;
    protected InvNode inputNode = null;
    
    public static SaveObject getSaveObject(
            String ingestURL,
            Connection connection,
            Role role,
            Role copyRole,
            LoggerInf logger)
        throws TException
    {
        return new SaveObject(ingestURL, role, copyRole, connection, logger);
    }
    
    public static SaveObject getSaveObject(
            String ingestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new SaveObject(ingestURL, Role.primary, null, connection, logger);
    }
    
    protected SaveObject(
            String ingestURL,
            Role role,
            Role copyRole,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        try {
            this.ingestURL = ingestURL;
            this.role = role;
            this.copyRole = copyRole;
            extractParts();
            String msg = "SaveObject URL:"
                        + " - storageBase=" + storageBase
                        + " - node=" + node
                        + " - objectID=" + objectID.getValue()
                        + " - role=" + role
                        + " - copyRole=" + copyRole
                    ;
            if (DEBUG) {
                System.out.println(msg);
            }
            logger.logMessage(msg, 2, true);
            //isValidNode(node, objectID, connection, logger);
            versionMap = getVersionMap(storageBase, node, objectID, logger);
            dbAdd = new DBAdd(connection, logger);
            tika = new Tika(logger);
            versionMap.setNode(this.node);
        
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
    
    public static SaveObject getSaveObject(
            String storageBase,
            int node,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new SaveObject(storageBase, node, objectID, Role.primary, connection, logger);
    }
    
    protected SaveObject(
            String storageBase,
            int node,
            Identifier objectID,
            Role role,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        try {
            //isValidNode(node, objectID, connection, logger);
            this.storageBase = storageBase;
            this.node = node;
            this.objectID = objectID;
            this.role = role;
            versionMap = getVersionMap(storageBase, node, objectID, logger);
            dbAdd = new DBAdd(connection, logger);
            tika = new Tika(logger);
            buildIngestURL();
            versionMap.setNode(this.node);
                
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
    
    public void buildIngestURL()
        throws TException
    {
        try {
            
            ingestURL = storageBase + "/" + "manifest/" + node + "/"
                + URLEncoder.encode(objectID.getValue(), "utf-8") 
                ;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void extractParts()
        throws TException
    {
        URL manifestURL = null;
        try {
            if (StringUtil.isAllBlank(ingestURL)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "setParts - ingestURL missing");
            }
            String urlS = ingestURL;
            if (StringUtil.isEmpty(urlS)) {
                logger.logError("processItem - storage_url missing", 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processItem - storage_url missing");
            }
            try {
                manifestURL = new URL(urlS);
                if (DEBUG) System.out.println("manifestURL=" + manifestURL);
            } catch (Exception ex) {
                String msg = "processItem - URL invalid:" + urlS;
                logger.logError(msg, 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            }
            String urlPath = manifestURL.getPath();
            this.storageBase = 
                    manifestURL.getProtocol() 
                    + "://" + manifestURL.getHost()
                    + ":" + manifestURL.getPort()
                    ;
            
            if (DEBUG) System.out.println("storageBase=" + storageBase);
            String parts[] = urlPath.split("\\/");
            if (DEBUG) System.out.println("parts[] length=" + parts.length);        
            if (parts.length < 4) {
                String msg = "processItem - URL format invalid:" + urlS;
                logger.logError(msg, 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            }
            for (int i=0; i < parts.length; i++) {
                if (DEBUG) System.out.println("part[" + i + "]:" + parts[i]);
                        
                if (parts[i].equals("manifest")) {
                    extractManifestParts(parts, i);
                    return;
                }
                
                if (parts[i].equals("copy")) {
                    extractCopyParts(parts, i);
                    return;
                }
                if (parts[i].length() == 0) continue;
                this.storageBase += "/" + parts[i];
            }
            String msg = "processItem - URL format invalid:" + urlS;
            logger.logError(msg, 0);
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    private void extractManifestParts(String [] parts, int manifestInx)
        throws TException
    {
        
        try {
            String nodeS = parts[manifestInx + 1];
            String objectIDSE = parts[manifestInx + 2];
            this.node = Integer.parseInt(nodeS);
            String objectIDS = URLDecoder.decode(objectIDSE, "utf-8");
            this.objectID = new Identifier(objectIDS);
            method = "add";
            
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    private void extractCopyParts(String [] parts, int manifestInx)
        throws TException
    {
        
        try {
            String fromNodeS = parts[manifestInx + 1];
            String toNodeS = parts[manifestInx + 2];
            String objectIDSE = parts[manifestInx + 3];
            this.node = Integer.parseInt(fromNodeS);
            this.toNode = Integer.parseInt(toNodeS);
            String objectIDS = URLDecoder.decode(objectIDSE, "utf-8");
            this.objectID = new Identifier(objectIDS);
            method = "copy";
            
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void process()
        throws TException
    {
        process(true);
    }
    
    public void process(boolean doCheckVersion)
        throws TException
    {
        try {
            if (method.equals("copy")) {
               copy();
               return;
            }
            log("run entered");
            connection.setAutoCommit(false);
            if (doCheckVersion && checkVersion()) {
                System.out.println("**Skip objectID:" + objectID.getValue());
                return;
            }
            objectID = versionMap.getObjectID();
            setObject();
            connection.commit();
            commit = true;
            if (DUMPTALLY) System.out.println("***Tally\n" + tally.dumpProp());

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

    public void copy()
        throws TException
    {
        try {
            log("run entered");
            connection.setAutoCommit(false);  
            
            //validate that metaVersion maps are the same for both source and target node
            copyVersionMap = getVersionMap(storageBase, toNode, objectID, logger);
            validateCopy();
            
            //add original object to make sure it is up to date
            setObject();
            long copyNodeseq = setNode(toNode, copyRole, objectseq);
            copyFixity(copyNodeseq);
            
            connection.commit();
            commit = true;
            if (DUMPTALLY) System.out.println("***Tally\n" + tally.dumpProp());

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
    
    //protectec void copyFixity(long copyNodeseq, long objectseq, node, toNode
      
    protected void copyFixity(long copyNodeseq)
        throws TException
    {
        log("copyFixity entered:" 
                + " - copyNodeseq=" + copyNodeseq
                + " - addNodeseq=" + addNodeseq
                + " - objectseq=" + objectseq
                + " - node=" + node
                + " - toNode=" + toNode
                );
        List<Properties> fixityComponents = null;
        try {
            fixityComponents = InvDBUtil.getAudits(addNodeseq, objectseq, connection, logger);
            InvDBUtil.deleteAudits(copyNodeseq, objectseq, connection, logger);
            bump("copyFixity");
            for (Properties fixityComponent : fixityComponents) {
                log(PropertiesUtil.dumpProperties("Input", fixityComponent));
                InvAudit invFixity = new InvAudit(fixityComponent, logger);
                String url = invFixity.getUrl();
                url = url.replace("/" + node + "/", "/" + toNode + "/");
                invFixity.setUrl(url);
                invFixity.setNodeid(copyNodeseq);
                invFixity.setId(0);
                invFixity.setVerified((DateState)null);
                log(invFixity.dump("copyFixity"));
                long fixityid = dbAdd.insert(invFixity);
                invFixity.setId(fixityid);
            }
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    protected void setObject()
        throws TException
    {
        try {
            invObject = InvDBUtil.getObject(objectID, connection, logger);
            bump("getObject");
            if (invObject == null) {
                log("object not found in db:" + objectID.getValue());
                invObject = getNewObject();
                log(invObject.dump("Created object"));
                objectseq = dbAdd.insert(invObject);
                bump("insertObject");
                
            } else {
                log(invObject.dump("Initial request"));
                objectseq = invObject.getId();
            }
            invObject.setId(objectseq);
            addNodeseq = setNode(node, role, objectseq);
            InvOwner invOwner = getInvOwner();
            if (invObject.getOwnerID() != invOwner.getId()) {
                    System.out.println("Owner modified:"
                            + " - invObject.getOwnerID()=" + invObject.getOwnerID()
                            + " - invOwner.getId()=" + invOwner.getId()
                    );
                invObject.setOwnerID(invOwner.getId());
                dbAdd.update(invObject);
            }
            
            if (DEBUG) System.out.println("invObject.setOwnerID=" + invObject.getOwnerID());
            setInvCollections(objectseq);
            setInvVersions(objectseq);
            setInvEmbargoes(objectseq);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    protected void validateCopy()
        throws TException
    {
        try {
            try {
                copyVersionMap = getVersionMap(storageBase, toNode, objectID, logger);
            } catch (Exception ex) {
                copyVersionMap = null;
            }
            MatchMap matchMap = new MatchMap(versionMap, copyVersionMap, logger);
            matchMap.compare();
            if (matchMap.isDifferent()) {
                throw new TException.INVALID_ARCHITECTURE(matchMap.dump("Source and target storage manifests conflict"));
            }
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }

    public boolean isCommit() {
        return commit;
    }
    
    public InvObject getNewObject()
        throws TException
    {
        try {
            log("getNewObject entered");
            invObject = new InvObject(logger);
            invObject.setArk(versionMap.getObjectID());
            invObject.setType(InvObject.Type.mrtCuratorial);
            invObject.setRole(InvObject.Role.mrtContent);
            invObject.setVersionNumber(1);
            //set4W(invObject);  dropped here because is added during metaVersion handling 
            invObject.setModified();
            InvOwner invOwner = getInvOwner();
            invObject.setOwnerID(invOwner.getId());
            
            return invObject;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void set4W(InvObject invObject)
        throws TException
    {
        try {
            log("set4W entered");
            StoreERC storeERC = StoreERC.getStoreERC(storageBase, node, objectID, logger);
            bump("StoreERC1");
            set4W(invObject, storeERC);
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public void set4W(InvObject invObject, StoreERC storeERC)
        throws TException
    {
        try {
            if (DEBUG) System.out.println("StoreERC dump:" + storeERC.dump("test"));
            invObject.setWho(storeERC.getWho());
            invObject.setWhat(storeERC.getWhat());
            invObject.setWhen(storeERC.getWhen());
            invObject.setWhere(storeERC.getWhere());

        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public long setNodeOriginal(int node, Role storageRole, long objectseq)
        throws TException
    {
        try {
            log("setNode entered");
            long nodeseq = 0;
            InvNode invNode = InvDBUtil.getNode(node, connection, logger);
            bump("getNode");
            if (invNode == null) {
                if (DEBUG) System.out.println("***invNode null");
                StoreState storeState = StoreState.getStoreState(storageBase, node, logger);
                bump("StoreState");
                log("StoreState:" + storeState.dump("test"));
                invNode = new InvNode(logger);
                invNode.setState(storeState);
                invNode.setBaseURL(storageBase);
                nodeseq = dbAdd.insert(invNode);
                bump("insertNode");
                //long id = dbAdd.getId("owners", "ark", storeOwner.getOwnerObjectID().toString());
                invNode.setId(nodeseq);
            }
            nodeseq = invNode.getId();
            if (DEBUG) System.out.println("***nodeseq=" + nodeseq + " - node=" + node);
            InvNodeObject invNodeObject = InvDBUtil.getNodeObject(nodeseq, objectseq, storageRole.toString(), connection, logger);
            bump("getNodeObject");
            if (invNodeObject == null) {
                invNodeObject = new InvNodeObject(logger);
                invNodeObject.setNodesid(nodeseq);
                invNodeObject.setObjectsid(objectseq);
                long id = dbAdd.insert(invNodeObject);
                bump("insertNodeObject");
                //long id = dbAdd.getId("owners", "ark", storeOwner.getOwnerObjectID().toString());
                invNodeObject.setId(id);
            }
            return nodeseq;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public long setNode(int node, Role storageRole, long objectseq)
        throws TException
    {
        try {
            log("setNode entered");
            InvNode invNode = getInvNode(node);
            inputNode = invNode;
            validateNode(objectID, objectseq, invNode, connection, logger);
            long targetseq = getTargetNodeSeq(invNode);
            long inputseq = invNode.getId();
            if (inputseq != targetseq) {
                if (invNode.getNodeForm() != invNode.nodeForm.virtual) {
                    throw new TException.INVALID_ARCHITECTURE("Input node not virtual:" + inputseq
                            + " and does not match target node:" + targetseq
                    );
                } 
                long sourceNodeSeq = invNode.getSourceNodeSeq();
                InvNodeObject sourceNodeObject = InvDBUtil.getNodeObject(sourceNodeSeq, objectseq, connection, logger);
                if (sourceNodeObject != null) {
                    int deleteCnt = InvDBUtil.deleteNodeObject(sourceNodeSeq, objectseq, connection, logger);
                    System.out.println("Virtual: node-objects deleted:" + deleteCnt);
                    resetVirtualAudit(invNode.getTargetNodeSeq(), objectseq);
                    // resetAuditNode(invNode.getSourceNodeSeq(), invNode.getTargetNodeSeq(), objectseq);
                } else {
                    System.out.println("Virtual: no source");
                }
            } else {
                System.out.println("Virtual: none - inputseq == targetseq:" + inputseq);
            }
           
            InvNodeObject invNodeObject = getInvNodeObject(targetseq, storageRole, objectseq);
            
            // reset replicated to force replication if needed
            if (invNodeObject.getReplicated() != null) {
                invNodeObject.setReplicated((DateState)null);
                long nodeseq = dbAdd.update(invNodeObject);
            }
            return targetseq;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public InvNode getInvNode(int node)
        throws TException
    {
        try {
            log("getInvNode entered");
            long nodeseq = 0;
            InvNode invNode = InvDBUtil.getNode(node, connection, logger);
            bump("getInvNode");
            if (invNode == null) {
                if (DEBUG) System.out.println("***invNode null");
                StoreState storeState = StoreState.getStoreState(storageBase, node, logger);
                bump("StoreState");
                log("StoreState:" + storeState.dump("test"));
                invNode = new InvNode(logger);
                invNode.setState(storeState);
                if (DEBUG) System.out.println("invNode:"
                        + " - getNodeForm=" + invNode.getNodeForm()
                        + " - getSourceNode=" + invNode.getSourceNode()
                        + " - getTargetNode=" + invNode.getTargetNode()
                        + " - getNodeForm=" + (invNode.getNodeForm() != InvNode.NodeForm.virtual) 
                        + " - getSourceNode=" + (invNode.getSourceNode() == null) 
                        + " - getTargetNode=" + (invNode.getTargetNode() == null)
                        );
               
                if (
                        invNode.getNodeForm() == InvNode.NodeForm.virtual
                        || (invNode.getSourceNode() != null) 
                        || (invNode.getTargetNode() != null)
                    ) {
                    if (
                        invNode.getNodeForm() != InvNode.NodeForm.virtual
                        || (invNode.getSourceNode() == null) 
                        || (invNode.getTargetNode() == null)
                    ) {
                         throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                                 + "not all virtual elements supplied:"  
                                + " - nodeForm:" + invNode.getNodeForm() + "*"
                                + " - sourceNodeId:" + invNode.getSourceNode() + "*"
                                + " - targetNodeId:" + invNode.getTargetNode() + "*"
                                );
                    }
                    
                    Long sourceNodeSeq = InvDBUtil.getNodeSeq(invNode.getSourceNode(), connection, logger);
                    if (sourceNodeSeq == null) {

                    }
                    invNode.setSourceNodeSeq(sourceNodeSeq);
                    
                    Long targetNodeSeq = InvDBUtil.getNodeSeq(invNode.getTargetNode(), connection, logger);
                    invNode.setTargetNodeSeq(targetNodeSeq);
                    log("source and target set:"
                            + " - SourceNodeId=" + invNode.getSourceNode()
                            + " - TargetNodeId=" + invNode.getTargetNode()
                            + " - sourceNodeSeq=" + sourceNodeSeq
                            + " - targetNodeSeq=" + targetNodeSeq
                            );
                    
                }
                invNode.setBaseURL(storageBase);
                nodeseq = dbAdd.insert(invNode);
                bump("insertNode");
                //long id = dbAdd.getId("owners", "ark", storeOwner.getOwnerObjectID().toString());
                invNode.setId(nodeseq);
            }
            return invNode;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public InvNodeObject getInvNodeObject(long nodeseq, Role storageRole, long objectseq)
        throws TException
    {
        try {
            log("getInvNodeObject entered");
            if (DEBUG) System.out.println("***nodeseq=" + nodeseq + " - node=" + node);
            InvNodeObject invNodeObject = InvDBUtil.getNodeObject(nodeseq, objectseq, connection, logger);
            bump("getInvNodeObject");
            if (invNodeObject == null) {
                invNodeObject = new InvNodeObject(logger);
                invNodeObject.setNodesid(nodeseq);
                invNodeObject.setObjectsid(objectseq);
                long id = dbAdd.insert(invNodeObject);
                bump("insertNodeObject");
                //long id = dbAdd.getId("owners", "ark", storeOwner.getOwnerObjectID().toString());
                invNodeObject.setId(id);
            }
            return invNodeObject;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public boolean resetAuditNode(long sourceNodeSeq, long targetNodeSeq, long objectSeq)
        throws TException
    {
        try {
            return InvDBUtil.resetAuditNode(sourceNodeSeq, targetNodeSeq, objectSeq, connection, logger);
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    /**
     * Reset audit values base on virtual migration
     * @param targetNodeSeq reassigned target value
     * @param objectSeq inv_object_id for inv_audits
     * @throws TException 
     */
    protected void resetVirtualAudit(long targetNodeSeq, long objectSeq)
        throws TException
    {
        try {
            InvNode targetNode = InvDBUtil.getNodeFromId(targetNodeSeq, connection, logger);
            List<Properties> props = InvDBUtil.getAudits(objectSeq, connection, logger);
            for (Properties prop : props) {
                InvAudit audit = new InvAudit(prop, logger);
                audit.setNodeid(targetNodeSeq);
                String retUrl = InvUtil.getAuditUrl(audit.getUrl(), "content", targetNode.getNumber());
                audit.setUrl(retUrl);
                dbAdd.update(audit);
                System.out.println(audit.dump("update"));
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public long getTargetNodeSeq(InvNode invNode)
        throws TException
    {
        if (invNode.getTargetNodeSeq() != null) return invNode.getTargetNodeSeq();
        return invNode.getId();
    }
    
    public InvOwner getInvOwner()
        throws TException
    {
        try {
            log("getInvOwner entered");
            InvOwner oldOwner = InvDBUtil.getOwnerFromObject(objectID, connection, logger);
            if (DEBUG) {
                if (oldOwner == null) {
                    System.out.println("oldOwner null");
                } else {
                    System.out.println("oldOwner:" + oldOwner.dump("test"));
                }
            }
            bump("getOwner");
            //if (oldOwner != null) return oldOwner;
            StoreOwner storeOwner = StoreOwner.getStoreOwner(storageBase, node, objectID, logger);
            bump("StoreOwner");
            if (DEBUG) System.out.println("storeOwner:" + storeOwner.dump("test"));
            if (oldOwner != null) {
                if (oldOwner.getArk().getValue().equals(storeOwner.getOwnerObjectID().getValue())) {
                    return oldOwner;
                }
            }
            Identifier ownerID = storeOwner.getOwnerObjectID();
            InvOwner owner = InvDBUtil.getOwner(ownerID, connection, logger);
            if (owner == null) {
                owner = new InvOwner(logger);
                owner.setStoreOwner(storeOwner);
                long id = dbAdd.insert(owner);
                owner.setId(id);
                bump("addOwner");
            }
            log("Owner id:" + owner.getId());
            return owner;
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    
    
    public void setInvCollections(long objectseq)
        throws TException
    {
        try {
            log("setInvCollections entered");
            StoreCollections storeCollections 
                    = StoreCollections.getStoreCollections(storageBase, node, objectID, logger);
            List<String> collections = storeCollections.getList();
            if (DEBUG) System.out.println("storeCollection size=:" + collections.size());
            for (String collection: collections) {
                setInvCollection(objectseq, collection);
            }
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvEmbargoes(long objectseq)
        throws TException
    {
        InvEmbargo invEmbargo = null;
        try {
            log("setInvEmbargoes entered");
            long currentVersionID = versionMap.getCurrent();
            StoreEmbargo storeEmbargo 
                    = StoreEmbargo.getStoreEmbargo(storageBase, node, objectID, currentVersionID, "producer/mrt-embargo.txt", logger);
            
            Properties prop  = storeEmbargo.getEmbargoProp();
            System.out.println(PropertiesUtil.dumpProperties("FileProp", prop));
            if (prop != null) {
                invEmbargo = InvEmbargo.getInvEmbargoFromTxt(objectseq, prop, logger);
            }
            
            InvEmbargo invEmbargoOld = InvDBUtil.getEmbargo(objectseq, connection, logger);
            if (invEmbargoOld != null) {
                if (invEmbargo == null) {
                    InvDBUtil.deleteEmbargo(objectseq, connection, logger);
                    System.out.println("delete embargo - objectseq:" + objectseq);
                    return;
                } else {
                    invEmbargo.setId(invEmbargoOld.getId());
                    System.out.println(invEmbargo.dump("update embargo"));
                    long id = dbAdd.update(invEmbargo);
                    return;
                }
            }
            if (invEmbargo == null) {
                System.out.println("no embargo - objectseq:" + objectseq);
                return;
            }
            if (invEmbargo != null) {
                long id = dbAdd.insert(invEmbargo);
                System.out.println(invEmbargo.dump("insert embargo:" + id));
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvCollection(long objectseq, String collectionS)
        throws TException
    {
        try {
            log("setInvCollection entered:"
                    + " - objectseq=" + objectseq
                    + " - collectionS=" + collectionS
                    );
            InvCollection invCollection = InvDBUtil.getCollection(collectionS, connection, logger);
            bump("getCollection");
            if (invCollection == null) {
                invCollection = new InvCollection(logger);
                invCollection.setArk(collectionS);
                long id = dbAdd.insert(invCollection);
                bump("insertCollection");
                if (DEBUG) System.out.println("REPLACE COLLECTION id=" + id);
                invCollection.setId(id);
                
            }
            long collectionseq = invCollection.getId();
            InvCollectionObject invCollectionObject 
                    = InvDBUtil.getCollectionObject(objectseq, collectionseq, connection, logger);
            bump("getCollectionObject");
            if (invCollectionObject == null) {
                invCollectionObject = new InvCollectionObject(logger);
                invCollectionObject.setObjectID(objectseq);
                invCollectionObject.setCollectionID(collectionseq);
                long id = dbAdd.insert(invCollectionObject);
                bump("insertCollectionObject");
                if (DEBUG) System.out.println("REPLACE COLLECTIONOBJECT id=" + id);
                
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvVersions(long objectseq)
        throws TException
    {
        try {
            log("setInvVersions entered");
            long versionCnt = versionMap.getVersionCount();
            long currentVersionID = versionMap.getCurrent();
            long versionseq = 0;
            for (int iv = 1; iv <= versionCnt; iv++) {
                InvVersion invVersion = InvDBUtil.getVersion(objectseq, iv, connection, logger);
                bump("getVersion");
                if (invVersion == null) {
                    invVersion = new InvVersion(logger);
                    invVersion.setArk(objectID);
                    invVersion.setNumber(iv);
                    invVersion.setObjectID(objectseq);
                    versionseq = dbAdd.insert(invVersion);
                    bump("insertVersion");
                    invVersion.setId(versionseq);
                    ComponentContent content = versionMap.getVersionContent(iv);
                    setInvContent(addNodeseq, objectseq, invVersion.getId(), iv, content);
                    setInvDK(objectseq, invVersion.getId(), iv, currentVersionID);
                    setInvMetadatas(objectseq, invVersion.getId(), iv);
                }
                if (currentVersionID == iv) {
                    setCurrent(invVersion, currentVersionID);
                    
                } else {
                    setInvIngests(invVersion, null);
                }
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setCurrent(InvVersion invVersion, long currentVersionID)
        throws TException
    {
        try {
            log("setCurrent entered");
            setInvIngests(invVersion, ingestURL);
            setObjectAggregate(currentVersionID);
            
            if (invObject.getAggregateRole() == InvObject.AggregateRole.mrtCollection) {
                long collectionseq = setCollectionObject();
                setInvDuas(invVersion, collectionseq);
            } else {
                setInvDuas(invVersion, 0);
            }
            long objectVersion = invObject.getVersionNumber();
            if (objectVersion < currentVersionID) {
                invObject.setVersionNumber(currentVersionID);
                dbAdd.update(invObject);
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvIngests(InvVersion version, String storageURL)
        throws TException
    {
        long versionseq = -1;
        try {
            log("setInvIngests entered:"
                    + " - objectseq=" + objectseq
                    + " - versionseq=" + versionseq
                    );
            if (version == null) {
                throw new TException.INVALID_OR_MISSING_PARM("setInvVersions-missing currentVersion");
            }
            objectseq = version.getObjectID();
            versionseq = version.getId();
            InvIngest invIngests = InvDBUtil.getIngest(objectseq, versionseq, connection, logger);
            bump("getIngest");
            if (invIngests == null) {
                StoreIngest storeIngest = StoreIngest.getStoreIngest(
                    storageBase, 
                    node, 
                    objectID,
                    version.getNumber(),
                    "system/mrt-ingest.txt",
                    logger);
                bump("StoreIngest");
                invIngests = new InvIngest(logger);
                invIngests.setIngestProp(storeIngest.getIngestProp(), storageURL);
                invIngests.setObjectID(objectseq);
                invIngests.setVersionID(versionseq);
                long id = dbAdd.insert(invIngests);
                bump("insertIngest");
                if (DEBUG) System.out.println("REPLACE INGESTS id=" + id);
                invIngests.setId(id);
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setObjectAggregate(long versionID)
        throws TException
    {
        try {
            if (invObject == null) return;
            if (invObject.getAggregateRole() != null) return;
            log("setObjectAggregate entered");
            StoreMom storeMom = StoreMom.getStoreMom(
                storageBase, 
                node, 
                objectID,
                versionID,
                "system/mrt-mom.txt",
                logger);
            
            bump("StoreMom");
            invObject.setAggregateRole(storeMom.getAggregateRole());
            dbAdd.update(invObject);
            bump("updateObject1");
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public void setInvDuas(InvVersion version, long collectionseq)
        throws TException
    {
        long objectseq = -1;
        long versionseq = -1;
        String DUA = "producer/mrt-dua.txt";
        InvDua invDua = null;
        try {
            if (version == null) {
                throw new TException.INVALID_OR_MISSING_PARM("setInvVersions-missing currentVersion");
            }
            FileComponent dua = versionMap.getFileComponent((int)version.getNumber(), DUA);
            if (dua == null) return;
            log("setInvDuas entered:"
                    + " - objectseq=" + objectseq
                    );
            objectseq = version.getObjectID();
            versionseq = version.getId();
            if (collectionseq > 0) {
                invDua = InvDBUtil.getDua(objectseq, collectionseq, connection, logger);
                bump("getDua1");
            } else {
                invDua = InvDBUtil.getDua(objectseq, connection, logger);
                bump("getDua2");
            }
            if (invDua == null) {
                if (DEBUG) System.out.println("DUA null"
                        + " - objectseq=" + objectseq
                        + " - collectionseq=" + collectionseq
                        );
                StoreDua storeDua = StoreDua.getStoreDua(
                    storageBase, 
                    node, 
                    objectID,
                    version.getNumber(),
                    DUA,
                    logger);
                invDua = new InvDua(logger);
                invDua.setDuaFile(storeDua.getDuaProp());
                if (collectionseq > 0) {
                    invDua.setCollectionID(collectionseq);
                }
                invDua.setObjectID(objectseq);
                long id = dbAdd.insert(invDua);
                bump("insertDua");
                if (DEBUG) System.out.println("REPLACE DUAS id=" + id);
                invDua.setId(id);
                setDuaTemplate(invDua, version);
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setDuaTemplate(InvDua invDua, InvVersion version)
        throws TException
    {
        String TEMPLATE = "producer/mrt-dua-template.xml";
        try {
            if (version == null) {
                throw new TException.INVALID_OR_MISSING_PARM("setInvVersions-missing currentVersion");
            }
            FileComponent duaTemplate = versionMap.getFileComponent((int)version.getNumber(), TEMPLATE);
            if (duaTemplate == null) return;
            log("setDuaTemplate entered:"
                    + " - objectseq=" + objectseq
                    );
            
            StoreDuaTemplate storeDuaTemplate = StoreDuaTemplate.getStoreDuaTemplate(
                storageBase, 
                node, 
                objectID,
                version.getNumber(),
                TEMPLATE,
                logger);
            bump("StoreDuaTemplate");
            String template = storeDuaTemplate.getTemplate();
            if (template != null) {
                long val = InvDBUtil.updateText(invDua, "template", template, connection, logger);
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public long setCollectionObject()
        throws TException
    {
        try {
            long collectionseq = 0;
            if (invObject.getAggregateRole() != InvObject.AggregateRole.mrtCollection) return 0;
            Identifier objectID = invObject.getArk();
            String objectIDS = objectID.getValue();
            log("setCollectionObject entered:"
                    + " - objectseq=" + objectseq
                    + " - collectionS=" + objectIDS
                    );
            long objectseq = invObject.getId();
            InvCollection invCollection = InvDBUtil.getCollection(objectIDS, connection, logger);
            bump("getCollection");
            if (invCollection == null) {
                invCollection = new InvCollection(logger);
                invCollection.setArk(objectIDS);
                collectionseq = dbAdd.insert(invCollection);
                bump("insertCollection1");
                invCollection.setId(collectionseq);
            }
            long collectionObjectID = invCollection.getObjectID();
            if (DEBUG) System.out.println("****CollectionObjectID=" + collectionObjectID);
            if (collectionObjectID <= 0) {
                invCollection.setObjectID(objectseq);
                collectionseq = dbAdd.insert(invCollection);
                bump("insertCollection2");
                if (DEBUG) System.out.println("setCollectionObject REPLACE COLLECTION id=" + collectionseq);
            }
            return collectionseq;
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvContent(long nodeseq, long objectseq, long versionseq, long versionID, ComponentContent content)
        throws TException
    {
        try {
            log("setInvContent entered:"
                    + " - objectseq=" + objectseq
                    + " - versionseq=" + versionseq
                    + " - versionID=" + versionID
                    );
            InvNode invNode = InvDBUtil.getNodeFromId(nodeseq, connection, logger);
            List<FileComponent> components = content.getFileComponents();
            for (FileComponent component : components) {
                InvFile invFile = InvDBUtil.getFile(
                        objectseq, versionseq, component.getIdentifier(), connection, logger);
                bump("getFile");
                String mimeKey = component.getLocalID();
                if (invFile != null) {
                    String mimeType = invFile.getMimeType();
                    String testMime = mimeList.get(mimeKey);
                    if (testMime != null) {
                        mimeList.put(mimeKey, mimeType);
                    }
                    
                }
                if (invFile == null) {
                    String mimeType = null;
                    invFile = new InvFile(logger);
                    boolean isBillable = setBillable(component, versionID);
                    invFile.setObjectID(objectseq);
                    invFile.setVersionID(versionseq);
                    invFile.setFileComponent(component, isBillable);
                    mimeType = invFile.getMimeType();
                    if (DEBUG) System.out.println("2**mimeType=" + mimeType);
                    if (isBillable) {
                        if (mimeType != null) {
                            mimeList.put(mimeKey, mimeType);
                            log("Existing MimeType"
                                + " - name=" + invFile.getPathName()
                                + " - mimeType=" + mimeType
                                );
                        }
                    } else {
                        if (mimeType == null) {
                            mimeType = mimeList.get(mimeKey);
                        }
                    } 
                    if (mimeType == null) {
                        StoreFile storeFile = StoreFile.getStoreFile(
                                storageBase, 
                                node, 
                                objectID, 
                                versionID, 
                                invFile.getPathName(), 
                                tika, 
                                logger);
                        bump("StoreFile");
                        mimeType = storeFile.getMimeType();
                        log("Extraced MimeType"
                                + " - name=" + invFile.getPathName()
                                + " - mimeType=" + mimeType
                                );
                        mimeList.put(mimeKey, mimeType);
                    } else {
                        log("Non-delta mime found:" + mimeType);
                    }
                    invFile.setMimeType(mimeType);
                    long id = dbAdd.insert(invFile);
                    bump("insertFile");
                    logger.logMessage("Add:" 
                            + " - component:" + invFile.getPathName()
                            ,2,true);
                    invFile.setId(id);
                    
                    if (isBillable) {
                        InvAudit invFixity = new InvAudit(logger);
                        invFixity.setNodeid(nodeseq);
                        invFixity.setObjectid(objectseq);
                        invFixity.setVersionid(versionseq);
                        invFixity.setFileid(id);
                        String url = storageBase + "/" + "content/" + invNode.getNumber() + "/"
                            + URLEncoder.encode(objectID.getValue(), "utf-8") 
                            + "/" + versionID 
                            + "/" + URLEncoder.encode(invFile.getPathName(), "utf-8") 
                            + "?fixity=no"
                            ;
                        invFixity.setUrl(url);
                        long fixityid = dbAdd.insert(invFixity);
                        invFixity.setId(fixityid);
                    }
                }
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvDK(long objectseq, long versionseq, long versionID, long currentVersionID)
        throws TException
    {
        try {
            log("setInvDC entered:"
                    + " - objectseq=" + objectseq
                    + " - versionseq=" + versionseq
                    + " - versionID=" + versionID
                    );
            InvDKVersion invDKVersion = InvDBUtil.getDKVersion(
                        objectseq, versionseq, connection, logger);
            bump("getDKVersion");
            if (invDKVersion == null) {
                invDKVersion = new InvDKVersion(objectseq, versionseq, logger);
                StoreERC storeERC = StoreERC.getStoreERC(storageBase, node, objectID,  versionID, logger);
                bump("StoreERC2");
                invDKVersion.addStoreERC(storeERC);
                
                List<InvDK> dkList = invDKVersion.getDCList();
                for (InvDK invDK : dkList) {
                    long id = dbAdd.insert(invDK);
                    bump("insertInvDK");
                    invDK.setId(id);
                }
                if (currentVersionID == versionID) {
                    set4W(invObject, storeERC);    
                    dbAdd.update(invObject);
                    bump("updateObject2");
                }
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvMetadatas(long objectseq, long versionseq, long versionID)
        throws TException
    {
        try {
            log("setInvMetadatas entered:"
                    + " - objectseq=" + objectseq
                    + " - versionseq=" + versionseq
                    + " - versionID=" + versionID
                    );
            StoreMeta meta = StoreMeta.getStoreMeta(storageBase, node, (int)versionID, versionMap, logger);
            bump("StoreMeta");
            int metaCnt = meta.getMetaCnt();
            if (metaCnt == 0) return;
            List<StoreMeta.MatchMeta> metaList = meta.getMetaComponents();
            log("setInvMetadatas:"
                    + " - cnt=" + metaList.size()
                    );
            for (StoreMeta.MatchMeta metaItem : metaList) {
                setInvMeta(meta, metaItem, objectseq, versionseq);
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public void setInvMeta(StoreMeta storeMeta, StoreMeta.MatchMeta metaItem, long objectseq, long versionseq)
        throws TException
    {
        try {
            log("setInvMeta entered:"
                    + " - objectseq=" + objectseq
                    + " - versionseq=" + versionseq
                    );
            
            InvMeta invMeta = InvDBUtil.getMeta(
                        objectseq, versionseq, metaItem.fileID, connection, logger);
            bump("getMeta");
            if (invMeta == null) {
                invMeta = new InvMeta(logger);
                invMeta.setStoreMeta(metaItem, objectseq, versionseq);
                String value = storeMeta.getValue(invMeta.fileName);
                if (metaItem.stripHeader) {
                    value = XMLUtil.removeXMLHeader(value);
                }
                long id = dbAdd.insert(invMeta);
                bump("insertInvMeta");
                invMeta.setId(id);
                long val = InvDBUtil.updateText(invMeta, "value", value, connection, logger);
            }
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected boolean setBillable(FileComponent component, long versionID)
        throws TException
    {
        try {
            log("setBillable entered");
            String key = component.getLocalID();
            if (StringUtil.isEmpty(key)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "setBillable empty key");
            }
            String [] parts = key.split("\\s*\\|\\s*");
            if (parts.length != 3) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "setBillable parts invalid=" + parts.length
                        + " - key=" + key
                        + " - objectID=" + objectID.getValue()
                        );
                
            }
            long keyVersion = Long.parseLong(parts[1]);
            if (keyVersion > versionID) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "setBillable keyVersion > versionID:" 
                        + " - keyVersion=" + keyVersion
                        + " - versionID=" + versionID
                        );
            }
            if (keyVersion == versionID) return true;
            else return false;
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }

    public Tika getTika() {
        return tika;
    }

    public VersionMap getVersionMap() {
        return versionMap;
    }
    
    protected void bump(String key)
    {
        if (!DUMPTALLY) return;
        tally.bump(key);
        if (EACHTALLY) System.out.println("Bump " + key + "=" +  tally.getValue(key));
    }
    
    public static void isValidNode(
            int testNode,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        List<Integer> nodes = InvDBUtil.getNodes(objectID, connection, logger);
        if (nodes == null) return;
        StringBuffer validNodes = new StringBuffer();
            
        for (Integer node : nodes) {
            if (validNodes.length() > 0) {
                validNodes.append(",");
            }
            validNodes.append("" + node);
            if (testNode == node) return;
        }
        
        throw new TException.REQUEST_INVALID(MESSAGE + "Add this object to inv invalid because of existing node"
                + " - testNode=" + testNode
                + " - validNodes=" + validNodes
                );
    }
    
    public static void validateNode(
            Identifier objectID,
            long objectseq, 
            InvNode invNode,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        List<Long> nodes = InvDBUtil.getNodesList(objectseq, connection, logger);
        if (nodes == null) return;
        for (Long node : nodes) {
            System.out.println("**validateNode:" + node);
        }
        if (invNode.getSourceNodeSeq() != null) { // virtual;
            boolean sourceMatch = isMatchNodeseq(invNode.getSourceNodeSeq(), nodes);
            boolean targetMatch = isMatchNodeseq(invNode.getTargetNodeSeq(), nodes);
            if (DEBUG) System.out.println("virtual match"
                    + " - sourceMatch:" + sourceMatch
                    + " - targetMatch:" + targetMatch
                    );
            if (sourceMatch || targetMatch) return;
            throw new TException.REQUEST_INVALID(MESSAGE + "Node for object not found for either target or source"
                + " - invNode.getSourceNode()=" + invNode.getSourceNodeSeq()
                + " - invNode.getTargeteNode()=" + invNode.getTargetNodeSeq()
            );
        }
        boolean idMatch = isMatchNodeseq(invNode.getId(), nodes);
        if (DEBUG) System.out.println("physical match"
                    + " - invNode.getId():" + invNode.getId()
                    + " - idMatch:" + idMatch
                );
        if (!idMatch) {
            String nodeString = InvDBUtil.getNodesString(objectID, connection, logger);
            throw new TException.REQUEST_INVALID(MESSAGE + "Node exists for object but is different from this manifest"
                + " - input node id=" + invNode.getId()
                + " - input node number=" + invNode.getNumber()
                + " - existing nodes=" + nodeString
            );
        }
        return;
    }
    
    protected boolean checkVersion()
        throws TException
    {
        int currentMap = 0;
        int currentDb = 0;
        boolean skip = false;
        try {
            currentMap = versionMap.getCurrent();
            currentDb = InvDBUtil.getVersionCnt(objectID, connection, logger);
            if (currentDb > currentMap) {
                String dbNodes = InvDBUtil.getNodesString(objectID, connection, logger);
                throw new TException.REQUEST_INVALID(MESSAGE + "-checkVersion - db version count exceeds passed map version:"
                        + " - objectID:" + objectID.getValue()
                        + " - map current:" + currentMap
                        + " - db  current:" + currentDb
                        + " - map node:" + versionMap.getNode()
                        + " - db node:" + dbNodes
                        );
            }
            if (currentDb == currentMap) {
                return true;
            }
            return false;
            
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public static boolean isMatchNodeseq(
            long nodeseq, 
            List<Long> nodes)
        throws TException
    {
        if (nodes == null) return true;
        for (long node : nodes) {
            if (nodeseq == node) return true;
        }
        return false;
    }
    
    public InvProcessState getProcessState()
    {
        InvProcessState state = new InvProcessState(ingestURL, method);
        return state;
    }
    
}