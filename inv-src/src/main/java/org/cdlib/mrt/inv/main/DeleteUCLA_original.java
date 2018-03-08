
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.core.DateState
        ;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.PrimaryLocalState;
import org.cdlib.mrt.inv.service.LocalContainerState;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.cdlib.mrt.db.DBUtil;
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.inv.action.NodeObjectFlip;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvNodeObject;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Load manifest.
 * @author  dloy
 */

public class DeleteUCLA_original
{
    private static final String NAME = "DeleteUCLA";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    protected  int batchSize = 3;
    protected  int maxBatch = 2;
    protected  int nodeSeq = 0;
    protected int mod = 4;
    
    private LoggerInf logger = null;
    protected DPRFileDB db = null;
    protected int objcnt = 0;
    protected long afterObjectSeq = 0;
    
    protected int batchCnt=0;
    protected int nodeObjectCnt = 0;
    protected int updateCnt = 0;
    protected long errorCnt = 0;
    protected int skipCnt = 0;
    protected int excCnt = 0;
    protected long totalDeleteAudit  = 0;
    protected long totalDeleteNodeObject   = 0;
    
    public DeleteUCLA_original(Properties invProp, int nodeSeq, LoggerInf logger)
        throws TException
    {
        DateState dateState = new DateState();
        System.out.println(NAME + " Start:" + dateState.getIsoDate());
        db = new DPRFileDB(logger, invProp);
        this.nodeSeq = nodeSeq;
        this.logger = logger;
    }

    
    public DeleteUCLA_original(String dirPathS)
        throws TException
    {
        try {
            if (dirPathS == null) {
                throw new TException.INVALID_OR_MISSING_PARM("'dirPathS' not found");
            }
            DateState dateState = new DateState();
            String startDate = dateState.getIsoDate();
            System.out.println(NAME + " Start:" + startDate);
            File dirPath = new File(dirPathS);
            System.out.println("dirPath=" + dirPath
                    + " - canon:" + dirPath.getCanonicalPath()
            );
            if (!dirPath.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM("dirPath does not exist:" + dirPath);
            }
            File propFile = new File(dirPath, "delucla-info.txt");
            if (!propFile.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM("dirPath does not exist:" + dirPath);
            }
            File log = new File(dirPath, "log");
            log.mkdir();
            FileInputStream inStream = new FileInputStream(propFile);
            Properties deleteProp = new Properties();
            deleteProp.load(inStream);
            
            
            System.out.println(PropertiesUtil.dumpProperties("DeleteUCLA", deleteProp));
            System.out.println("log:" + log.getCanonicalPath());
            this.logger = new TFileLogger("del", log.getCanonicalPath() + "/", deleteProp);
            db = new DPRFileDB(logger, deleteProp);
            String maxBatchS = deleteProp.getProperty("maxBatch");
            if (!StringUtil.isAllBlank(maxBatchS)) {
                maxBatch = Integer.parseInt(maxBatchS);
            }
            
            String batchSizeS = deleteProp.getProperty("batchSize");
            if (!StringUtil.isAllBlank(batchSizeS)) {
                batchSize = Integer.parseInt(batchSizeS);
            }
            
            String modS = deleteProp.getProperty("mod");
            if (modS != null) {
                mod = Integer.parseInt(modS);
            }
            
            String nodeSeqS = deleteProp.getProperty("nodeSeq");
            if (nodeSeqS == null) {
                throw new TException.INVALID_OR_MISSING_PARM("'nodeSeqS' not found");
            }
            nodeSeq = Integer.parseInt(nodeSeqS);
            System.out.println("log:" + log.getCanonicalPath());
            
            Connection tempConnect = db.getConnection(true);
            InvNode invNode = InvDBUtil.getNode(8001, tempConnect, logger);
            long deleteSeq = invNode.getId();
            tempConnect.close();
            if (deleteSeq != nodeSeq) {
                throw new TException.INVALID_ARCHITECTURE(MESSAGE + "mismatch delete seq:"
                    + " - deleteSeq:" + deleteSeq
                    + " - nodeSeq:" + nodeSeq
                );
            }
            String msg = MESSAGE + "Start:"  + startDate
                    + "\n - dirPathS:" + dirPath.getAbsolutePath()
                    + "\n - nodeSeq:" + nodeSeq
                    + "\n - batchSize:" + batchSize
                    + "\n - maxBatch:" + maxBatch
                    + "\n - deleteSeq:" + deleteSeq
                    + "\n - mod:" + mod;
            this.logger.logMessage(msg, 1, true);
            System.out.println(msg);
        
        } catch (TException tex) {
            System.out.println("Exception:" + tex);
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }

    /**
     * Main method
     */       
    public static void main(String args[])
            throws TException
    {

        TFrame tFrame = null;
        try {
            //String dirPath = "/apps/replic/test/ucla/delete";
            
            String dirPath = args[0];
            DeleteUCLA_original delUCLA = new DeleteUCLA_original(dirPath);
            delUCLA.process();

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }
    
    public static void main_original(String args[])
    {

        TFrame tFrame = null;
        DPRFileDB db = null;
        try {
            String propertyList[] = {
                "resources/DeleteUCLALogger.properties",
                "resources/DeleteUCLA.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties invProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", invProp));
            
            db = new DPRFileDB(logger, invProp);
            DeleteUCLA_original du = new DeleteUCLA_original(invProp, 21, logger);
            du.process();
            
            //service.shutdown();
            

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

    protected static String get(Properties prop, String key)
        throws TException
    {
        String retVal = prop.getProperty(key);
        if (StringUtil.isEmpty(retVal)) return null;
        return retVal;
    } 
    
    public void process() 
            throws TException
    {
        try {
            Connection connection = null;
            List<Long> objectSeqList = null;

            afterObjectSeq = 0;
            for(batchCnt=1; batchCnt <= maxBatch; batchCnt++) {
                if (DEBUG) System.out.println("*************process:" + batchCnt
                        + " - nodeSeq:" + nodeSeq
                );
                try {
                    connection = db.getConnection(false);
                    objectSeqList = getObjectNodesUCLA(batchSize, afterObjectSeq,nodeSeq, connection,logger);
                    if (objectSeqList == null) break;
                    for (Long objectSeq : objectSeqList) {
                        deleteObject(nodeSeq, objectSeq, connection);
                        afterObjectSeq = objectSeq;
                    }

                    if ((batchCnt % mod) == 0) {
                        String bmsg = ">>>deleteUCLA(" + batchCnt + "):"
                                + " - nodeSeq=" + nodeSeq 
                                + " - totalDeleteAudit=" + totalDeleteAudit
                                + " - totalDeleteNodeObject=" + totalDeleteNodeObject
                                + " - objcnt=" + objcnt
                                + " - afterObjectSeq=" + afterObjectSeq
                                + " - err=" + errorCnt
                                + " - exc=" + excCnt
                                + "\n---------------\n";

                        System.out.println(bmsg);
                        logger.logMessage(bmsg, 0, true);
                    }
                //Construct BufferedReader from InputStreamReader
                } catch (TException tex) {
                    System.out.println("Exception:" + tex);
                    tex.printStackTrace();
                    throw tex;

                } catch (Exception ex) {
                    System.out.println("Exception:" + ex);
                    ex.printStackTrace();
                    throw new TException(ex);

                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (Exception ex) { }
                    }
                }
            }
            String finalMsg = "Totals"
                    + " - nodeSeq:" + nodeSeq
                    + " - afterObjectSeq=" + afterObjectSeq
                    + " - totalDeleteAudit:" + totalDeleteAudit
                    + " - totalDeleteNodeObject:" + totalDeleteNodeObject;
            this.logger.logMessage(finalMsg, 1, true);
            System.out.println(finalMsg);
            
        } catch (TException tex) {
            throw tex;
            
        } finally {
            try {
                db.shutDown();
            } catch (Exception ex) {
                System.out.println("db Exception:" + ex);
            }
        }
    }

    public static ArrayList<Long> getObjectNodesUCLA(
            int objectCnt,
            long afterObjectSeq,
            int nodeSeq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        if (DEBUG) System.out.println("getObjectNodesUCLA entered"
                    + " - objectCnt:" + objectCnt
                    + " - afterObjectSeq:" + afterObjectSeq
                    + " - nodeSeq:" + nodeSeq
        );
        String objectSql =
            "select inv_object_id from inv_nodes_inv_objects "
            + "where role='secondary' "
            + "and inv_node_id=" + nodeSeq + " "
            + "and inv_object_id > " + afterObjectSeq + " "
            + "limit " + objectCnt + ";";
        Properties[] propArray = DBUtil.cmd(connection, objectSql, logger);
        
        if ((propArray == null)) {
            System.out.println("InvDBUtil - prop null");
            System.out.println("sql:" + objectSql);
            return null;
        } else if (propArray.length == 0) {
            System.out.println("InvDBUtil - length == 0");
            System.out.println("sql:" + objectSql);
            return null;
        }
        ArrayList<Long> list = new ArrayList<>();
        for (Properties prop : propArray) {
            String objectSeqS = prop.getProperty("inv_object_id");
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***getbjectNodesUCLA***", prop));
            Long objectSeq = Long.parseLong(objectSeqS);
            list.add(objectSeq);
        }
        return list;
    }
    
    private void deleteObject(int nodeSeq, long objectSeq, Connection connection)
        throws TException
    {
        try {
            objcnt++;
            //int numDeleteAudit = selectAudit(nodeSeq, objectSeq, connection, logger);
            int numDeleteAudit = deleteAudit(nodeSeq, objectSeq, connection, logger);
            totalDeleteAudit  += numDeleteAudit;
            
            //int numDeleteNodeObject = selectNodeObject(nodeSeq, objectSeq, connection, logger);
            int numDeleteNodeObject = deleteNodeObject(nodeSeq, objectSeq, connection, logger);
            totalDeleteNodeObject += numDeleteNodeObject;
            connection.commit();
 
        } catch (Exception ex) {
            try {
                connection.rollback();
                System.out.println(MESSAGE + "rollback:"
                    + " - nodeSeq=" + nodeSeq 
                    + " - objectSeq=" + objectSeq 
                    + " - ex=" + ex 
                );
            } catch (Exception rex) {
                System.out.println("Rollback exception:" + rex);
            }
            if (ex instanceof TException) {
                throw (TException)ex;
            }
            errorCnt++;
        }
        
    }
    
    public static  int  deleteNodeObject(int nodeSeq, long objectSeq, Connection connection, LoggerInf logger)
        throws TException
    {
        try {
            String deleteSql =
                "delete inv_nodes_inv_objects  "
                    + "from inv_nodes_inv_objects "
                    + "where inv_node_id=" + nodeSeq + " "
                    + "and inv_object_id= " + objectSeq +  ";";
            
            int deleteCnt = DBUtil.update(connection, deleteSql, logger);
            if (DEBUG) System.out.println("deleteNodeObject:" + deleteCnt);
            return deleteCnt;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException.SQL_EXCEPTION(ex);
        }
    }
    
    
    public static  int  deleteAudit(int nodeSeq, long objectSeq, Connection connection, LoggerInf logger)
        throws TException
    {
        try {
            String deleteSql =
                "delete inv_audits  "
                    + "from inv_audits "
                    + "where inv_node_id=" + nodeSeq + " "
                    + "and inv_object_id=" + objectSeq +  ";";
            int deleteCnt = DBUtil.update(connection, deleteSql, logger);
            if (DEBUG) System.out.println("deleteAudit:" + deleteCnt);
            return deleteCnt;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException.SQL_EXCEPTION(ex);
        }
    }
    
    public static  int  selectNodeObject(int nodeSeq, long objectSeq, Connection connection, LoggerInf logger)
        throws TException
    {
        String objectSql =
            "select * from inv_nodes_inv_objects "
            + "where inv_node_id=" + nodeSeq + " "
            + "and inv_object_id = " + objectSeq +  ";";
        
        Properties[] propArray = DBUtil.cmd(connection, objectSql, logger);
        
        if ((propArray == null)) {
            System.out.println("InvDBUtil - prop null");
            System.out.println("sql:" + objectSql);
            return 0;
            
        } else if (propArray.length == 0) {
            System.out.println("InvDBUtil - length == 0");
            System.out.println("sql:" + objectSql);
            return 0;
            
        } else if (propArray.length != 1) {
            throw new TException.INVALID_ARCHITECTURE(MESSAGE + "deleteNodeObject - inv_nodes_inv_objects size invalid:" 
                    + propArray.length
            );
        }
        
        System.out.println(PropertiesUtil.dumpProperties("***deleteNodeObject***", propArray[0]));
        return 1;
    }
    
    
    
    public static  int  selectAudit(int nodeSeq, long objectSeq, Connection connection, LoggerInf logger)
        throws TException
    {
        String objectSql =
            "select * from inv_audits "
            + "where inv_node_id=" + nodeSeq + " "
            + "and inv_object_id= " + objectSeq +  ";";
        
        Properties[] propArray = DBUtil.cmd(connection, objectSql, logger);
        
        if ((propArray == null)) {
            System.out.println("InvDBUtil - prop null");
            System.out.println("sql:" + objectSql);
            return 0;
        } else if (propArray.length == 0) {
            System.out.println("InvDBUtil - length == 0");
            System.out.println("sql:" + objectSql);
            return 0;
        }
        System.out.println("audits"
                + " - nodeSeq=" + nodeSeq
                + " - objectSeq=" + objectSeq
                + " - cnt=" + propArray.length
        );
        for (Properties prop : propArray) {
            System.out.println(PropertiesUtil.dumpProperties("***audit dump***", prop));
            break;
        }
        return propArray.length;
    }

}
