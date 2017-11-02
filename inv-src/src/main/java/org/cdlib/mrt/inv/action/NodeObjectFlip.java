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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.inv.content.InvFile;
import org.cdlib.mrt.inv.content.InvAudit;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvNodeObject;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Run fixity
 * @author dloy
 */
public class NodeObjectFlip
        extends InvActionAbs
{

    protected static final String NAME = "NodeObjectFlip";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;

    protected Identifier objectID = null;
    protected long objectseq = 0;
    protected long addNodeseq = 0;
    protected InvObject invObject = null;
    protected InvNodeObject newPrimary = null;
    protected List<InvNodeObject> nodeObjects = null;
    protected ArrayList<InvNodeObject> flipObjects = new ArrayList();
    protected boolean commit = false;
    protected DBAdd dbAdd = null;
    protected DateState replicatedDate = null;
    protected long versionNumber = 0;
    protected long newPrimaryNodeID = 0;
    protected int nodeObjectCnt = 0;
    protected int updateCnt = 0;
    protected boolean doFlip = false;
    protected boolean doSQL = true;
    protected int keptCnt = 0;
    protected int replaceCnt = 0;
    protected int skipCnt = 0;
    protected int errCnt = 0;
    
    
    /**
     * 
     * @param storageBase URL base used by fixity to validate content
     * @param objectID object identifier
     * @param connection inv db connector
     * @param logger general logger
     * @param doSQL do SQL flip
     * @return build state
     * @throws TException 
     */
    
    public static NodeObjectFlip getNodeObjectFlip(
            long newPrimaryNodeID,
            Identifier objectID,
            Connection connection,
            LoggerInf logger,
            boolean doSQL)
        throws TException
    {
        if (DEBUG) System.out.println("getNodeObjectFlip:"
                + " - newPrimaryNodeID:" + newPrimaryNodeID
                + " - objectID:" + objectID
        );
        return new NodeObjectFlip(newPrimaryNodeID,objectID, connection, logger, doSQL);
    }
    
    protected NodeObjectFlip(
            long newPrimaryNodeID,
            Identifier objectID,
            Connection connection,
            LoggerInf logger,
            boolean doSQL)
        throws TException
    {
        super(connection, logger);
        try {
            this.newPrimaryNodeID = newPrimaryNodeID;
            this.objectID = objectID;
            this.doSQL = doSQL;
            String msg = MESSAGE 
                        + " - objectID=" + objectID.getValue()
                    ;
            if (DEBUG) {
                System.out.println(msg);
            }
            logger.logMessage(msg, 10, true);
            dbAdd = new DBAdd(connection, logger);
        
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
            if (doFlip) connection.commit();
            commit = true;

        } catch (Exception ex) {
            
            String msg = MESSAGE + "Exception for entry id=" + objectID.getValue()
                    + " - Exception:" + ex
                    ;
            System.out.println("EXception:" + msg);
            ex.printStackTrace();
            logger.logError(msg, 2);
            logger.logError(StringUtil.stackTrace(ex),3);
            try {
                if (doFlip) connection.rollback();
            } catch (Exception cex) {
                System.out.println("WARNING: rollback Exception:" + cex);
            }
            errCnt++;
            if (ex instanceof TException) {
                throw (TException) ex;
            } else {
                throw new TException (ex);
            }

        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
            logger.logMessage("Flip:" + objectID.getValue() 
                    + " - replace:" + replaceCnt
                    + " - kept=" + keptCnt
                    + " - skip=" + skipCnt
                    + " - err:" + errCnt
                    + " - doSQL:" + doSQL
                    , 2, true);
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
            versionNumber = invObject.getVersionNumber();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("invObject", invObject.retrieveProp()));
            nodeObjects = InvDBUtil.getObjectNodes(objectseq, connection, logger);
            if (nodeObjects == null) {
                throw new TException.INVALID_ARCHITECTURE(MESSAGE + "node not found for object:" + objectID.getValue()
                        + " - objectseq=" + objectseq
                        );
            }
            doFlip = setNewPrimary();
            if (!doFlip) {
                skipCnt++;
                return;
            }
            build();
            validate();
            resetDB();
            confirm();
            
            
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
                    );
            replicatedDate = getReplicatedDate(nodeObjects);
            if (replicatedDate == null) {
                throw new TException.INVALID_DATA_FORMAT("No replicated date found for:"
                    + objectID.getValue());
            }
            for (InvNodeObject nodeObject : nodeObjects) {
                long nodeseq = nodeObject.nodesid;
                InvNodeObject flipNodeObject = flipBuild(nodeObject);
                addFlip(flipNodeObject);
            }
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void confirm()
        throws TException
    {
        int primaryCnt = 0;
        int secondaryCnt = 0;
        try {
            String arkS = invObject.getArk().getValue();
            ArrayList<InvNodeObject> confirmNodeObjects = 
                    InvDBUtil.getObjectNodes(objectseq, connection, logger);
            for (InvNodeObject nodeObject : confirmNodeObjects) {
                if (nodeObject.getRole() == Role.primary) {
                    primaryCnt++;
                    if (nodeObject.getNodesid() != newPrimaryNodeID) {
                        if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("Confirm - primary not primary nodeid:" 
                                + arkS,nodeObject.retrieveProp()));
                        errCnt++;
                    }
                } else { // secondary
                    secondaryCnt++;
                    if (nodeObject.getNodesid() == newPrimaryNodeID) {
                        if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("Confirm - secondary should be primary:" + arkS,
                                nodeObject.retrieveProp()));
                        errCnt++;
                    }
                }
                //dumpEntry("Confirm", nodeObject);
            }
            
            if (primaryCnt == 0) {
                if (DEBUG) System.out.println("Confirm - Primary not set:" + arkS);
                errCnt++;
            }
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void validate()
        throws TException
    {
        try {
            if (flipObjects.size() != nodeObjects.size() ) {
                throw new TException.INVALID_DATA_FORMAT(MESSAGE 
                        + "Not all nodeObjects copied :" 
                        + " - flipObjects.size():" + flipObjects.size()
                        + " - nodeObjects.size():" + nodeObjects.size()
                );
            }
            int primaryCnt = 0;
            int secondaryCnt = 0;
            for (InvNodeObject nodeObject : flipObjects) {
                if (nodeObject.role == Role.primary) primaryCnt++;
                else if (nodeObject.role == Role.secondary) secondaryCnt++;
            }
            if (primaryCnt != 1) {
                throw new TException.INVALID_DATA_FORMAT(MESSAGE 
                        + "Primary invalid:" 
                        + " - objectID:" + objectID.getValue()
                        + " - primaryCnt:" + primaryCnt
                );
            }
            if (secondaryCnt == 0) {
                throw new TException.INVALID_DATA_FORMAT(MESSAGE 
                        + "No secondary:" 
                        + " - objectID:" + objectID.getValue()
                        + " - secondaryCnt:" + secondaryCnt
                );
            }
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected void addFlip(InvNodeObject flipNodeObject)
        throws TException
    {
        flipObjects.add(flipNodeObject);
        dumpEntry("Add", flipNodeObject);
    }
    
    protected void resetDB()
        throws TException
    {
        try {
            if (!doFlip) return;
            for (InvNodeObject flipNodeObject : flipObjects) {
                nodeObjectCnt++;
                if (!flipNodeObject.isNewEntry()) {
                    dumpEntry("Kept", flipNodeObject);
                    keptCnt++;
                    continue;
                }
                if (doSQL) dbAdd.update(flipNodeObject);
                updateCnt++;
                replaceCnt++;
                
                dumpEntry("Replaced", flipNodeObject);
            }
                
        } catch (TException tex) {
            throw new TException(tex);
                    
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected void dumpEntry(String header, InvNodeObject invNodeObject)
    {
        if (!DEBUG) return;
        System.out.println(PropertiesUtil.dumpProperties(header + ":" + objectID.getValue(), invNodeObject.retrieveProp())
                    + " - newEntry:" + invNodeObject.isNewEntry()
                );
    }
    
    protected boolean setNewPrimary()
        throws TException
    {
        try {
            
            for (InvNodeObject nodeObject : nodeObjects) {
                
                dumpEntry("In", nodeObject);
                if ((nodeObject.getRole() == Role.primary) 
                        && (nodeObject.getNodesid() == newPrimaryNodeID)) {
                    dumpEntry("WARNING primary current ", nodeObject);
                    return false;
                }
                if ((nodeObject.getRole() == Role.secondary) 
                        && (nodeObject.getNodesid() == newPrimaryNodeID)) {
                    newPrimary = nodeObject;
                    dumpEntry("newPrimary", newPrimary);
                    if (newPrimary.getVersionNumber() != invObject.getVersionNumber()) {
                        throw new TException.INVALID_DATA_FORMAT(MESSAGE 
                            + "New Primary not same version as object:" 
                            + " - objectID:" + objectID.getValue()
                            + " - newPrimary.getVersionNumber():" + newPrimary.getVersionNumber()
                            + " - invObject.getVersionNumber():" + invObject.getVersionNumber()
                        );
                    }
                    return true;
                }
            }
            throw new TException.INVALID_DATA_FORMAT(MESSAGE 
                        + "No new primary found:" 
                        + " - objectID:" + objectID.getValue()
                );
            
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
                    
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected static DateState getReplicatedDate(List<InvNodeObject> nodeObjects)
        throws TException
    {
        try {
            long maxVersion = 0;
            if ((nodeObjects == null) || (nodeObjects.size() == 0)) {
                return null;
            }
            DateState replicated = null;
            for (InvNodeObject nodeObject : nodeObjects) {
                if (nodeObject.getRole() == Role.secondary) continue;
                replicated = nodeObject.getReplicated();
                if (replicated != null) return replicated;
            }
            return replicated;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    protected InvNodeObject flipBuild(InvNodeObject nodeObject)
        throws TException
    {
        Properties noProp = nodeObject.retrieveProp();
        InvNodeObject flipNodeObject = new InvNodeObject(noProp, logger);
        flipNodeObject.setNewEntry(false);
        if (flipNodeObject.getRole() == Role.primary) {
            return processPrimary(flipNodeObject);
            
        } else if (flipNodeObject.getRole() == Role.secondary) {
            if (flipNodeObject.getNodesid() == newPrimaryNodeID) {
                return processSecondary(flipNodeObject);
                
            // non flip secondary
            } else {
                nodeObject.setNewEntry(false);
                return nodeObject;
            }
            
        } else {
            throw new TException.INVALID_ARCHITECTURE("not primary or secondary:" 
                    + flipNodeObject.getRole());
        }
    }

    protected InvNodeObject processPrimary(InvNodeObject nodeObject)
        throws TException
    {
        nodeObject.setNewEntry(true);
        nodeObject.setRole(Role.secondary);
        nodeObject.setReplicatedCurrent();
        nodeObject.setVersionNumber(versionNumber);
        return nodeObject;
        
    }

    protected InvNodeObject processSecondary(InvNodeObject nodeObject)
        throws TException
    {
        nodeObject.setNewEntry(true);
        nodeObject.setRole(Role.primary);
        nodeObject.setReplicatedCurrent();
        nodeObject.setVersionNumber(versionNumber);
        return nodeObject;
    }

    public int getNodeObjectCnt() {
        return nodeObjectCnt;
    }

    public int getUpdateCnt() {
        return updateCnt;
    }

    public int getSkipCnt() {
        return skipCnt;
    }

    public int getErrCnt() {
        return errCnt;
    }
    
    
    
}