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
import java.util.Vector;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.inv.content.InvLocalID;
import org.cdlib.mrt.inv.service.LocalContainerState;
import org.cdlib.mrt.inv.service.PrimaryLocalState;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.DBDelete;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.StringUtil;

/**
 * Run fixity
 * @author dloy
 */
public class LocalMap
{

    protected static final String NAME = "LocalMap";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    
    SaveObject saveObject = null;
 
    
    public static InvLocalID getInvLocalID(
            String objectIDS,
            String ownerIDS,
            String localIDS,
            LoggerInf logger)
        throws TException
    {
        return new InvLocalID(objectIDS, ownerIDS, localIDS, logger);
    }
    
    public static LocalContainerState addLocal(
            Identifier objectID,
            Identifier ownerID,
            String localIDs,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return addLocal(
                objectID.getValue(), 
                ownerID.getValue(), 
                localIDs, connection, logger);
    }
    
    public static LocalContainerState addLocal(
            String objectIDS,
            String ownerIDS,
            String localIDs,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        try {
            Identifier ownerID = new Identifier(ownerIDS);
            LocalContainerState lcs  = getPrimaryNoClose(
                ownerID,
                localIDs,
                connection,
                logger);
            connection.setAutoCommit(false);
            Identifier primaryID = lcs.getPrimaryIdentifier();
            if (primaryID != null) {
                if (primaryID.getValue().equals(objectIDS)) {
                    lcs.setExists(true);
                    if (DEBUG) System.out.println("Exists:" + primaryID.getValue());
                    return lcs;
                } else {
                    throw new TException.REQUEST_INVALID(MESSAGE
                        + " objectID already exists and does not match add objectID"
                        + "  - existing objectID=" + primaryID.getValue()
                        + "  - new objectID=" + objectIDS
                        + "  - connection=" + connection.getAutoCommit()
                    );
                }
            }
            lcs.setExists(false);
            if (DEBUG) System.out.println("Set exists:" + lcs.isExists());
            List<String> listLocal = getLocalIDs(localIDs);
            DBAdd dbAdd = new DBAdd(connection, logger);
            ArrayList<InvLocalID> invLocalIDList = new ArrayList<>();
            for (String localID : listLocal) {
                InvLocalID invLocalID = getInvLocalID(objectIDS, ownerIDS, localID, logger);
                long localid = dbAdd.replace(invLocalID);
                invLocalID.setId(localid);
                invLocalIDList.add(invLocalID);
            }
            connection.commit();
            primaryID = new Identifier(objectIDS);
            
            
            LocalContainerState state
                        = LocalContainerState.buildLocalContainerState(
                                primaryID, 
                                ownerID,
                                localIDs,
                                true,
                                invLocalIDList);
            state.setExists(false);
            if (DEBUG) System.out.println(state.dump("Test"));
            return state;
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (Exception cex) {
                System.out.println("Connection Exception:" + cex);
            }
            if (ex instanceof TException) {
                throw (TException) ex;
            } else {
                throw new TException(ex);
            }
            
        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }
    }
    
    public static LocalContainerState deletePrimary(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        try {
            LocalContainerState local = getLocalsNoClose(objectID, connection, logger);
            if (!local.isExists()) {
                local.setDeleteCnt(0L);
                return local;
            }
            connection.setAutoCommit(false);
            long deleteCnt = deletePrimary(objectID.getValue(), connection, logger);
            connection.commit();
            local.setDeleteCnt(deleteCnt);
            if (DEBUG) System.out.println(local.dump("Test"));
            return local;
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (Exception cex) {
                System.out.println("Connection Exception:" + cex);
            }
            if (ex instanceof TException) {
                throw (TException) ex;
            } else {
                throw new TException(ex);
            }
            
        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }
    }

    public static List<String> getLocalIDs(String vals)
    {
        ArrayList<String> idList = new ArrayList<String>(10);
        if (StringUtil.isEmpty(vals)) return null;
        String [] ids = vals.split("\\s*\\;\\s*");
        for (String id : ids) {
            id = id.trim();
            idList.add(id);
        }
        return idList;
    }
    
    public static LocalContainerState getPrimaryClose(
            Identifier ownerID,
            String localIDs,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
       
        try {
            return getPrimaryNoClose(
                ownerID,
                localIDs,
                connection,
                logger);
        
        } catch (TException tex) {
            throw tex;
            
        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }
    }
    
    public static LocalContainerState getPrimaryNoClose(
            Identifier ownerID,
            String localIDs,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        LocalContainerState localContainerState = null;
        try {
            List<String> listLocal = getLocalIDs(localIDs);
            connection.setAutoCommit(true);
            ArrayList<InvLocalID> invLocalIDList = new ArrayList<>();
            Identifier currentObjectID = null;
            Identifier primaryID = null;
            boolean match = true;
            for (String localID : listLocal) {
                if (DEBUG) System.out.println("LocalList localID:" + localID);
                InvLocalID invLocalID = InvDBUtil.getPrimaryFromLocal(
                    ownerID, 
                    localID,
                    connection, 
                    logger);
                if (invLocalID == null) {
                    if (DEBUG) System.out.println("invLocalID == null");
                    match = false;
                    continue;
                }
                invLocalIDList.add(invLocalID);
                currentObjectID = invLocalID.getObjectArk();
                if (primaryID != null) {
                    if (!currentObjectID.getValue().equals(primaryID.getValue())) {
                        throw new TException.REQUEST_INVALID("Two or more local identifiers map to different primary identifiers:"
                                + " - " + currentObjectID + "=" + currentObjectID
                                + " - " + primaryID + "=" + primaryID);
                    }
                }           
                primaryID = currentObjectID;         
            }
            localContainerState 
                        = LocalContainerState.buildLocalContainerState(
                                primaryID, 
                                ownerID,
                                localIDs,
                                match,
                                invLocalIDList);
            return localContainerState;
        
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            System.out.println("LocalMap exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }
    
    public static LocalContainerState getLocalsClose(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        try {
            
            return getLocalsNoClose(objectID, connection, logger);
        
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }
    }
    
    public static LocalContainerState getLocalsNoClose(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        try {
            List<InvLocalID> invLocalIDList = InvDBUtil.getLocalFromPrimary(objectID, connection, logger);
            return LocalContainerState.buildLocalContainerState(objectID, invLocalIDList);
        
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }
    
    public static int deletePrimary(
            String objectArk, 
            Connection connection, 
            LoggerInf logger)
        throws TException
    {
        int delNum = delete(
                "inv_localids",
                "inv_object_ark",
                objectArk,
                connection,
                logger
                );
        return delNum;
    }
    
    protected static int delete(
            String tableName, 
            String rowName, 
            String rowValue, 
            Connection connection, 
            LoggerInf logger)
        throws TException
    {
        try {
            if (StringUtil.isAllBlank(rowValue)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "delete - rowValue not valid:" + rowValue);
            }
            String sql =
                    "DELETE FROM " + tableName + " WHERE " + rowName + "='" + rowValue + "'";
            
            int delCnt= DBDelete.delete(connection, sql, logger);
            System.out.println(MESSAGE + "delete:" 
                    + " - sql=" + sql
                    + " - delCnt=" + delCnt
                    );
            return delCnt;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
}

