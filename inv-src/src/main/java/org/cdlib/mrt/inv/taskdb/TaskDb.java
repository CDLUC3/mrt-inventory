/*
Copyright (c) 2005-2016, Regents of the University of California
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
package org.cdlib.mrt.inv.taskdb;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.inv.utility.DPRFileDB;
//import org.cdlib.mrt.replic.basic.action.DoScan;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.utility.TFileLogger;
import org.json.JSONObject;
import org.json.JSONArray;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class TaskDb
{
    private static final String NAME = "InvProcess";
    private static final String MESSAGE = NAME + ": ";
    private static final int MAXW = 5394;
    
    public enum CurrentStatus {ok, fail, partial, unknown };
    private Connection connect = null;
    private static final Logger log4j = LogManager.getLogger();
    
    
    public static void main(String[] args) throws Exception
    {
 
        LoggerInf logger = new TFileLogger("DoScan", 9, 10);
        DPRFileDB db = null;
        Boolean addstat = null;
        try {
            InventoryConfig config = InventoryConfig.useYaml();
            config.dbStartup();
            db = config.getDb();
            Connection connection = db.getConnection(true);
            TaskDb taskDb = new TaskDb(connection);
            JSONObject badJSON = taskDb.getTaskJSON("sxxxx",  "system/mrt-membership.txt");
            System.out.println("badJSON:::" + badJSON.toString(2));
            // ('changeToken','ark:/28722/bk000108811','fail','fail note'),
            taskDb.addTestJSON("changeToken",  "ark:/28722/bk000108811", "fail", "fail note1");
            taskDb.addTestJSON("changeToken",  "ark:/28722/bk000108811", "ok", null);
            
            taskDb.addTestJSON("changeToken",  "ark:/28722/bk000108811", "ok", null);
            // DELETE FROM inv_tasks WHERE task_name='somethingElse' AND task_item='system/mrt-membership.txt';
            //taskDb.addTestJSON("somethingElse",  "system/mrt-membership.txt", "fail", "fail note2");
            //taskDb.addTestJSON("somethingElse",  "system/mrt-membership.txt", "ok", null);
            //taskDb.deleteTask("somethingElse",  "system/mrt-membership.txt");
            //taskDb.getTask("changeToken",  "ark:/28722/bk000108811");
            //taskDb.getTaskJSON("changeToken",  "ark:/28722/bk000108811");
            //JSONArray json = taskDb.addTaskJSON("changeToken",  "ark:/28722/bk0003d6b3d", "fail", "fail note1");
            //JSONArray json = taskDb.getTaskJSON("changeToken",  "ark:/28722/bk0003d6b3d");
            //JSONArray json = taskDb.addTaskJSON("changeToken",  "ark:/28722/bk0003d6b3d", "ok", null);
            //System.out.println("addTaskJSON...\n" + json.toString(2));
            
        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        } finally {
            try {
                db.shutDown();
            } catch (Exception ex) {
                System.out.println("db Exception:" + ex);
            }
        }
    }
    
    public TaskDb(
            Connection connect)
        throws TException
    {
        this.connect = connect;
        try {
            connect.isValid(5);
            connect.setAutoCommit(true);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public JSONObject addTaskJSON(
            String taskName,
            String taskItem,
            String currentStatus,
            String note)
        throws TException
    {
        addTask(taskName, taskItem, currentStatus, note);
        return getTaskJSON(taskName, taskItem);
    }
    
    public JSONObject deleteTaskJSON(
            String taskName,
            String taskItem)
        throws TException
    {
        JSONObject result = getTaskJSON(taskName, taskItem);
        deleteTask(taskName, taskItem);
        return result;
    }
    
    
    /**
     * Add or update entry in inv_tasks table
     * @param taskName general name for overall process
     * @param taskItem item to be processed
     * @param currentStatus completion status
     * @param note (optional) note for specific issue (error message)
     * @return JSON entry content for add or update entry
     * @throws TException 
     */
    public void addTestJSON(
            String taskName,
            String taskItem,
            String currentStatus,
            String note)
        throws TException
    {
        log4j.debug("***addTestJSON - \n"
                + " - taskName:" + taskName + " - taskItem:" + taskItem + "\n"
                + " - currentStatus:" + currentStatus + " - note:" + note + "\n"
        );
        addTask(taskName, taskItem, currentStatus, note);
        JSONObject json =  getTaskJSON(taskName, taskItem);
        System.out.println("addTaskJSON...\n" + json.toString(2));
    }
    
    /**
     * Add or update entry in inv_tasks table
     * @param taskName general name for overall process
     * @param taskItem item to be processed
     * @param currentStatus completion status
     * @param note (optional) note for specific issue (error message)
     * @throws TException 
     */
    public void addTask(
            String taskName,
            String taskItem,
            String currentStatus,
            String note)
        throws TException
    {
        log4j.debug("***AddTask***\n"
                + " - taskName:" + taskName + "\n"
                + " - taskItem:" + taskItem + "\n"
                + " - currentStatus:" + currentStatus + "\n"
                + " - note:" + note + "\n"
        );
        if (taskName == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processName missing");
        }
        if (taskItem == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processItem missing");
        }
        if (currentStatus == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "currentStatus missing");
        }
        try {
            CurrentStatus status = CurrentStatus.valueOf(currentStatus);
        } catch (Exception cex) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "currentStatus invalid:" + currentStatus);
        }
        /*
        INSERT INTO inv_tasks 
(task_name, task_item, retries, current_status, note)
VALUES 
('somethingElse','system/mrt-membership.txt', 0, 'fail', 'fail note2')
ON DUPLICATE KEY UPDATE current_status = 'ok', retries = retries + 1, entry_last=NOW(), note=NULL;
        */
        String sql = "INSERT INTO inv_tasks "
            + "(task_name, task_item, retries, current_status, note) "
            + "VALUES (?, ?, 0, ?, ?) "
            +  "ON DUPLICATE KEY UPDATE current_status = ?, note = ?, retries = retries + 1, updated=NOW();";
        try {
            PreparedStatement addStmt = connect.prepareStatement(sql);
            addStmt.setString (1, taskName);
            addStmt.setString (2, taskItem);
            addStmt.setString (3, currentStatus);
            addStmt.setString (4, note);
            addStmt.setString (5, currentStatus);
            addStmt.setString (6, note);
            boolean works = addStmt.execute();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    /**
     * Get content for a defined task entry
     * @param taskName general name for overall process
     * @param taskItem item to be processed
     * @return JSON entry content
     * @throws TException 
     */
    public JSONObject getTaskJSON(
            String taskName,
            String taskItem)
        throws TException
    {
        String query
            = "Select * from inv_tasks "
                + "where task_name = ? "
                + "and task_item = ?;";
        try {
            // Prepare Statement
            PreparedStatement getStmt
                = connect.prepareStatement(query);

            // Set Parameters
            getStmt.setString(1, taskName);
            getStmt.setString(2, taskItem);

            // Execute SQL query
            ResultSet resultSet  = getStmt.executeQuery();
            
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            JSONArray entries = new JSONArray();
            while (resultSet.next()) {
                JSONObject column = new JSONObject();
               
                for (int i = 1; i <= columnsNumber; i++) {
                    String columnValue = resultSet.getString(i);
                    column.put(rsmd.getColumnName(i), columnValue);;
                }
                entries.put(column);
            }
            JSONObject entry = null;
            try {
                entry = entries.getJSONObject(0);
            } catch (Exception e) {
                // no response
                return new JSONObject("{}");
            }
            return entry;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    /**
     * Delete task entry based on taskName and taskItem
     * @param taskName general name for overall process
     * @param taskItem item to be processed
     * @throws TException 
     */
    public void deleteTask(
            String taskName,
            String taskItem)
        throws TException
    {
        if (taskName == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processName missing");
        }
        if (taskItem == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processItem missing");
        }
        
        String sql = "DELETE FROM inv_tasks "
            + "WHERE task_name=? AND task_item=?;";
        try {
            PreparedStatement deleteStmt = connect.prepareStatement(sql);
            deleteStmt.setString (1, taskName);
            deleteStmt.setString (2, taskItem);
            boolean works = deleteStmt.execute();
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
}

