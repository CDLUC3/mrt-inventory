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
package org.cdlib.mrt.inv.utility;


import java.sql.Connection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;


import org.cdlib.mrt.db.DBUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class DBDelete
{
    private static final String NAME = "DBDelete";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    

    protected long objectseq = 0;
    protected LoggerInf logger = null;
    protected Connection connection = null;
    
    
    public DBDelete(long objectseq, Connection connection, LoggerInf logger)
        throws TException
    { 
        this.objectseq = objectseq;
        this.connection = connection;
        this.logger = logger;
        validate();
        if (DEBUG) System.out.println("DBDelete - objectseq=" + objectseq);
    }
    
    private void validate()
        throws TException
    {
        if (logger == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "DBDelete - logger empty");
        }
        if (connection == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "DBDelete - connection empty");
        }
        if (objectseq <= 0) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "delete - objectseq empty");
        }
        boolean isValid = true;
        try {
            isValid = connection.isValid(10);
            connection.setAutoCommit(false);
            
        } catch (Exception ex) {
            logger.logError("Connection fails:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            isValid = false;
        }
        
    }
    

    public int delete()
        throws TException
    {
        try {
            if (DEBUG) System.out.println(MESSAGE + "process entered");
            int delCnt=0;
            delCnt += deleteEmbargoes();
            delCnt += deleteAudits();
            delCnt += deleteFiles();
            delCnt += deleteDuas();
            delCnt += deleteDublinKernels();
            delCnt += deleteMetadatas();
            delCnt += deleteIngests();
            delCnt += deleteVersions();
            delCnt += deleteCollectionsObjects();
            delCnt += deleteNodesObjects();
            delCnt += deleteObjects();
            
            connection.commit();
            return delCnt;

        } catch (Exception ex) {
            String msg = MESSAGE + "Exception for objectseq=" + objectseq
                    + " - Exception:" + ex
                    ;
            System.out.println("EXception:" + msg);
            logger.logError(msg, 2);
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
    
    public int deleteAudits()
        throws TException
    {
        int delNum = delete(
                "inv_audits",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteEmbargoes()
        throws TException
    {
        int delNum = delete(
                "inv_embargoes",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteFiles()
        throws TException
    {
        int delNum = delete(
                "inv_files",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteDuas()
        throws TException
    {
        int delNum = delete(
                "inv_duas",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteMetadatas()
        throws TException
    {
        int delNum = delete(
                "inv_metadatas",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteDublinKernels()
        throws TException
    {
        int delNum = delete(
                "inv_dublinkernels",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteIngests()
        throws TException
    {
        int delNum = delete(
                "inv_ingests",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteVersions()
        throws TException
    {
        int delNum = delete(
                "inv_versions",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteCollectionsObjects()
        throws TException
    {
        int delNum = delete(
                "inv_collections_inv_objects",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteNodesObjects()
        throws TException
    {
        int delNum = delete(
                "inv_nodes_inv_objects",
                "inv_object_id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    public int deleteObjects()
        throws TException
    {
        int delNum = delete(
                "inv_objects",
                "id",
                objectseq,
                connection,
                logger
                );
        return delNum;
    }
    
    protected static int delete(
            String tableName, 
            String rowName, 
            long objectseq, 
            Connection connection, 
            LoggerInf logger)
        throws TException
    {
        try {
            if (objectseq <= 0) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "delete - objectseq not valid:" + objectseq);
            }
            String sql =
                    "DELETE FROM " + tableName + " WHERE " + rowName + "=" + objectseq;
            
            int delCnt= delete(connection, sql, logger);
            if (DEBUG) System.out.println(MESSAGE + "delete:" 
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
    
    public static int delete(
            Connection connection,
            String deleteCmd,
            LoggerInf logger)
        throws TException
    {
        if (StringUtil.isEmpty(deleteCmd)) {
            throw new TException.INVALID_OR_MISSING_PARM("deleteCmd not supplied");
        }
        if (connection == null) {
            throw new TException.INVALID_OR_MISSING_PARM("connection not supplied");
        }
        
        try {

            Statement statement = connection.createStatement();
            int delCnt = statement.executeUpdate(deleteCmd);
            
            return delCnt;

        } catch(Exception e) {
            String msg = "Exception"
                + " - sql=" + deleteCmd
                + " - exception:" + e;

            logger.logError(MESSAGE + "exec - " + msg, 0);
            System.out.println(msg);
            throw new TException.SQL_EXCEPTION(msg, e);
        }
    }

}

