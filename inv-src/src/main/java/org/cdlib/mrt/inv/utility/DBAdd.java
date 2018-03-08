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


import org.cdlib.mrt.inv.content.*;
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
public class DBAdd
{
    private static final String NAME = "DBAdd";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DOREPLACE = true;
    private static final boolean DEBUG = false;
    

    protected ArrayList<ContentAbs> addArray = new ArrayList<>();
    protected LoggerInf logger = null;
    protected Connection connection = null;
    
    
    public DBAdd(Connection connection, LoggerInf logger)
        throws TException
    { 
        this.connection = connection;
        this.logger = logger;
        validate();
    }
    
    private void validate()
        throws TException
    {
        if (logger == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "DBadd - logger empty");
        }
        if (connection == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "DBadd - connection empty");
        }
        
        boolean isValid = true;
        try {
            isValid = connection.isValid(300);
            if (isValid) {
                connection.setAutoCommit(false);
            }
            
        } catch (Exception ex) {
            logger.logError("Connection fails:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            isValid = false;
        }
        if (!isValid) {
            throw new TException.SQL_EXCEPTION("DBADD - connection invalid");
        }
        
    }
    
    public void addContent(ContentAbs content)
        throws TException
    {
        if (content == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "add - content empty");
        }
        if (DEBUG) System.out.println(MESSAGE + "addContent:" + content.dump(content.getDBName()));
        addArray.add(content);
    }
    
    public void replace()
        throws TException
    {
        try {
            for (ContentAbs content : addArray) {
                replace(content);
            }
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            logger.logError(MESSAGE + "CException:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            throw new TException(ex);
        }
    
    }
    
    public long replace(ContentAbs content)
        throws TException
    {
        String sql = null;
        try {
            Properties prop = content.retrieveProp();
            if (prop.getProperty("id") != null) {
                return update(content);
            }
            String tableName = content.getDBName();
            String sqlReplace = "replace into " + tableName + " set ";
            String values = DBUtil.buildModifyNull(prop);
            sql = sqlReplace + values;
            if (DEBUG) System.out.println(MESSAGE + "REPLACE sql=" + sql);
            if (!DOREPLACE) {
                return 0;
            }
            long autoID = execDeadlockRetry(connection, sql, logger);
            content.setNewEntry(true);
            if (autoID == 0) {
                String msg = MESSAGE 
                        + "Fail sql=" + sql;
                throw new TException.REMOTE_IO_SERVICE_EXCEPTION(msg);
            }
            return autoID;
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            logger.logError(MESSAGE
                        + "Fail sql=" + sql + "CException:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            throw new TException.SQL_EXCEPTION(ex);
        }
    
    }
    
    public long insert(ContentAbs content)
        throws TException
    {
        String sql = null;
        try {
            Properties prop = content.retrieveProp();
            if (prop.getProperty("id") != null) {
                return update(content);
            }
            String tableName = content.getDBName();
            String sqlInsert = "insert into " + tableName + " set ";
            String values = DBUtil.buildModifyNull(prop);
            sql = sqlInsert + values;
            if (DEBUG) System.out.println(MESSAGE + "REPLACE sql=" + sql);
            if (!DOREPLACE) {
                return 0;
            }
            long autoID = exec(connection, sql, logger);
            content.setNewEntry(true);
            if (autoID == 0) {
                String msg = MESSAGE 
                        + "Fail sql=" + sql;
                throw new TException.REMOTE_IO_SERVICE_EXCEPTION(msg);
            }
            return autoID;
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            logger.logError(MESSAGE
                        + "Fail sql=" + sql + "CException:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            throw new TException.SQL_EXCEPTION(ex);
        }
    
    }
    public long update(ContentAbs content)
        throws TException
    {
        String sql = null;
        try {
            Properties prop = content.retrieveProp();
            String tableName = content.getDBName();
            String idS = prop.getProperty("id");
            long id = Long.parseLong(idS);
            prop.remove("id");
            String sqlReplace = "update " + tableName + " set ";
            String values = DBUtil.buildModifyNull(prop);
            sql = sqlReplace + values + " where id=" + idS;
            if (DEBUG) System.out.println(MESSAGE + "UPDATE sql=" + sql);
            DBUtil.exec(connection, sql, logger);
            return id;
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Exception"
                + " - sql=" + sql
                + " - exception:" + ex;

            logger.logError(MESSAGE + "exec - " + msg, 0);
            System.out.println(msg);
            throw new TException.SQL_EXCEPTION(msg, ex);
        }
    
    }
    
    public long update(String tableName, Properties prop)
        throws TException
    {
        String sql = null;
        try {
            String idS = prop.getProperty("id");
            long id = Long.parseLong(idS);
            prop.remove("id");
            String sqlReplace = "update " + tableName + " set ";
            String values = DBUtil.buildModifyNull(prop);
            sql = sqlReplace + values + " where id=" + idS;
            if (DEBUG) System.out.println(MESSAGE + "UPDATE sql=" + sql);
            DBUtil.exec(connection, sql, logger);
            return id;
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = "Exception"
                + " - sql=" + sql
                + " - exception:" + ex;

            logger.logError(MESSAGE + "exec - " + msg, 0);
            System.out.println(msg);
            throw new TException.SQL_EXCEPTION(msg, ex);
        }
    
    }
    
    public static long exec(
            Connection connection,
            String replaceCmd,
            LoggerInf logger)
        throws TException
    {
        if (StringUtil.isEmpty(replaceCmd)) {
            throw new TException.INVALID_OR_MISSING_PARM("replaceCmd not supplied");
        }
        if (connection == null) {
            throw new TException.INVALID_OR_MISSING_PARM("connection not supplied");
        }
        long autoID = 0;
        Statement statement = null;
        try {

            statement = connection.createStatement();
            boolean works = statement.execute(replaceCmd, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()){
                autoID=rs.getInt(1);
            }
            if (DEBUG) System.out.println("****autoID=" + autoID);
            return autoID;

        } catch(Exception e) {
            String msg = "Exception"
                + " - sql=" + replaceCmd
                + " - exception:" + e;

            logger.logError(MESSAGE + "exec - " + msg, 0);
            System.out.println(msg);
            throw new TException.SQL_EXCEPTION(msg, e);
            
        } finally {
            try {
		statement.close();
            } catch (Exception e) { }
	}
    }
    
    public static long execDeadlockRetry(
            Connection connection,
            String replaceCmd,
            LoggerInf logger)
        throws TException
    {
        TException.SQL_EXCEPTION saveEx = null;
        int retry=0;
        for (retry=1; retry <= 5; retry++) {
            try {
                return exec(connection, replaceCmd, logger);

            } catch(TException.SQL_EXCEPTION se) {
                saveEx = se;
                String msg = se.toString();
                msg = msg.toLowerCase();
                if (msg.contains("deadlock") || msg.contains("wait timeout exceeded")) {
                    String warnMsg = MESSAGE + "WARNING Deadlock(" + retry + "):"
                            + " - sql=" + replaceCmd
                            + " - msg=" + msg;
                    System.out.println(warnMsg);
                    logger.logError(warnMsg, 0);
                    try {
                        Thread.sleep(retry*2000);
                    } catch (Exception ex) { }
                    continue;
                }
                if (msg.contains("foreign key constraint")) {
                    String warnMsg = MESSAGE + "WARNING foreign key constraint(" + retry + "): sql=" + replaceCmd;
                    System.out.println(warnMsg);
                    logger.logError(warnMsg, 0);
                    try {
                        Thread.sleep(retry*15000);
                    } catch (Exception ex) { }
                    continue;
                }
                System.out.println(MESSAGE + "execDeadlockRetry - Non Deadlock exception:" + se);
                se.printStackTrace();
                throw se;
            }
	}
        String lastMsg = "###FINAL:" + MESSAGE + "Deadlock/foreign key exceeded: sql=" + replaceCmd 
                + " - Retry=:" + retry
                + " - Exception:" + saveEx;
        saveEx.printStackTrace();
        throw new TException.SQL_EXCEPTION(lastMsg);
    }
    
    public long getId(String table, String key, String value)
        throws TException
    {
        String sql = null;
        try {
            sql = "select id from " + table + " where " + key + "=\'"  + value + "\'";
            Properties [] props = DBUtil.cmd(connection, sql, logger);
            if ((props == null) || (props.length == 0)) return 0;
            String idS = props[0].getProperty("id");
            if (StringUtil.isAllBlank(idS)) return 0;
            long id = Long.parseLong(idS);
            return id;
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            logger.logError(MESSAGE
                        + "Fail sql=" + sql + "CException:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            throw new TException.SQL_EXCEPTION(ex);
        }
    
    }
    
    public Properties[] getProps(String table, String key, String value)
        throws TException
    {
        String sql = null;
        try {
            sql = "select * from " + table + " where " + key + "=\'"  + value + "\'";
            Properties [] props = DBUtil.cmd(connection, sql, logger);
            if ((props == null) || (props.length == 0)) return null;
            return props;
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            String msg = MESSAGE
                        + "Fail sql=" + sql + "CException:" + ex;
            logger.logError(msg, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            System.out.println(msg);
            ex.printStackTrace();
            throw new TException.SQL_EXCEPTION(ex);
        }
    
    }



}

