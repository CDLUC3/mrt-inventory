
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
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.inv.action.NodeObjectFlip;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Load manifest.
 * @author  dloy
 */

public class AddNodeObjectList
{
    private static final String NAME = "AddNodeObjectList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    protected DPRFileDB db = null;
    private File inFile = null;
    protected long newPrimaryNodeID = 0;
    protected int objcnt = 0;
    protected int mod = 1;
    
    protected int nodeObjectCnt = 0;
    protected int updateCnt = 0;
    protected int errorCnt = 0;
    protected int skipCnt = 0;
    protected int excCnt = 0;
    protected boolean doSQL = false;
    
    public AddNodeObjectList(Properties invProp, LoggerInf logger, boolean doSQL)
        throws TException
    {
        DateState dateState = new DateState();
        System.out.println("AddNodeObjectList Start:" + dateState.getIsoDate());
        String inFileS = invProp.getProperty("inFile");
        if (inFileS == null) {
            throw new TException.INVALID_OR_MISSING_PARM("'inFile' not found");
        }
        inFile = new File(inFileS);
        String newPrimaryNodeIDS = invProp.getProperty("newPrimaryNodeID");
        if (newPrimaryNodeIDS == null) {
            throw new TException.INVALID_OR_MISSING_PARM("'newPrimaryNodeID' not found");
        }
        inFile = new File(inFileS);
        newPrimaryNodeID = Long.parseLong(newPrimaryNodeIDS);
        db = new DPRFileDB(logger, invProp);
        this.logger = logger;
        this.doSQL = doSQL;
    }
    
    public AddNodeObjectList(String dirPathS)
        throws TException
    {
        try {
            if (dirPathS == null) {
                throw new TException.INVALID_OR_MISSING_PARM("'dirPathS' not found");
            }
            DateState dateState = new DateState();
            String startDate = dateState.getIsoDate();
            File dirPath = new File(dirPathS);
            if (!dirPath.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM("dirPath does not exist:" + dirPath);
            }
            File propFile = new File(dirPath, "anol-info.txt");
            if (!propFile.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM("dirPath does not exist:" + dirPath);
            }
            File log = new File(dirPath, "log");
            log.mkdir();
            FileInputStream inStream = new FileInputStream(propFile);
            Properties flipProp = new Properties();
            flipProp.load(inStream);
            
            
            System.out.println(PropertiesUtil.dumpProperties("AddNodeObjectList", flipProp));
            String newPrimaryNodeIDS = flipProp.getProperty("newPrimaryNodeID");
            if (newPrimaryNodeIDS == null) {
                throw new TException.INVALID_OR_MISSING_PARM("'newPrimaryNodeID' not found");
            }
            newPrimaryNodeID = Long.parseLong(newPrimaryNodeIDS);
            
            String modS = flipProp.getProperty("mod");
            if (modS != null) {
                mod = Integer.parseInt(modS);
            }
            newPrimaryNodeID = Long.parseLong(newPrimaryNodeIDS);
            
            String inFileS = flipProp.getProperty("inFile");
            if (inFileS == null) {
                throw new TException.INVALID_OR_MISSING_PARM("'inFile' not found");
            }
            inFile = new File(dirPath, inFileS);
            String doSQLS = flipProp.getProperty("doSQL");
                //System.out.println("***doSQLS:" + doSQLS);
            if ((doSQLS != null) && doSQLS.equals("true")) {
                this.doSQL = true;
            }
            System.out.println("doSQL:" + doSQL);
            db = new DPRFileDB(logger, flipProp);
            System.out.println("log:" + log.getCanonicalPath());
            this.logger = new TFileLogger("flip", log.getCanonicalPath(), flipProp);
            logger.logMessage("Start processing flip" + startDate, 0, true);
        
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
    
    
            
            
    public static void main(String args[])
            throws TException
    {

        TFrame tFrame = null;
        try {
            String dirPath = "/apps/replic/test/aws/migration/flip";
            
            dirPath = args[0];
            AddNodeObjectList anol = new AddNodeObjectList(dirPath);
            anol.processList();
            
            
            //service.shutdown();
            

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
        try {
            String propertyList[] = {
                "resources/AddNodeObjectListLogger.properties",
                "resources/AddNodeObjectList.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties invProp  = tFrame.getProperties();
            AddNodeObjectList anol = new AddNodeObjectList(invProp, logger, false);
            anol.processList();
            
            
            //service.shutdown();
            

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }


    protected static String get(Properties prop, String key)
        throws TException
    {
        String retVal = prop.getProperty(key);
        if (StringUtil.isEmpty(retVal)) return null;
        return retVal;
    } 
    
     private void processList() 
            throws TException
    {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(inFile);

            //Construct BufferedReader from InputStreamReader
            br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                objcnt++;
                logger.logMessage("Process:" + line, 10, true);
                processLine(line);
            }
            //logger.logMessage("***nodeObjectCnt:" + nodeObjectCnt, 2);
            //logger.logMessage("***updateCnt:" + updateCnt, 2);
 
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
            try {
                br.close();
                db.shutDown();
            } catch (Exception ex) { }
        }
    }
    
    private void processLine(String line)
        throws TException
    {
        Connection connection = null;
        try {
            if (DEBUG)System.out.println("\n\n#####Line(" + objcnt + "):" + line);
            connection = db.getConnection(false);
            Identifier objectID = new Identifier(line);
            NodeObjectFlip nof = NodeObjectFlip.getNodeObjectFlip(newPrimaryNodeID, 
                    objectID, connection, logger, doSQL);
            
            try {
                nof.process();
            
            } catch (Exception ex) {
                excCnt++;
                logger.logError("**Exception:" + line + " - " + ex, 0);
            }
            nodeObjectCnt += nof.getNodeObjectCnt();
            updateCnt += nof.getUpdateCnt();
            errorCnt += nof.getErrCnt();
            skipCnt += nof.getSkipCnt();
            
            if ((objcnt%mod) == 0)
            System.out.println("Obj="  + objcnt 
                    + " - noc=" + nodeObjectCnt 
                    + " - up=" + updateCnt
                    + " - skip=" + skipCnt
                    + " - err=" + errorCnt
                    + " - exc=" + excCnt
            );
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (Exception ex) { }
        }
        
    }
}
