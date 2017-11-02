
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import org.cdlib.mrt.inv.main.*;
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
import java.util.HashMap;
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

public class TestBigManifest
{
    private static final String NAME = "AddNodeObjectList";
    private static final String MESSAGE = NAME + ": ";

    public final String SPLITDELIM = "\\s*\\|\\s*";
    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    
    private HashMap<String, Integer> map = new HashMap();
    private File inFile = null;
    protected int lineCnt = 0;
    
    
    
    public TestBigManifest(String manifestS, LoggerInf logger)
        throws TException
    {
        this.inFile = new File(manifestS);
        this.logger = logger;
    }
    
    
            
            
    public static void main(String args[])
            throws TException
    {

        TFrame tFrame = null;
        try {
            String manifestS = "/apps/replic/tomcat-28080/webapps/test/bigmanifest/mrt-manifest.txt";
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            TestBigManifest anol = new TestBigManifest(manifestS, logger);
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
                lineCnt++;
                //logger.logMessage("Process:" + line, 10, true);
                processLine(line);
                //if (lineCnt > 100) break;
            }
            System.out.println("Final:" + lineCnt);
 
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
            } catch (Exception ex) { }
        }
    }
    
    private void processLine(String line)
        throws TException
    {
        Connection connection = null;
        try {
            if (lineCnt <= 6) return;
            String [] parts = line.split(SPLITDELIM);
            if (parts.length < 7) return;
            String fileID = parts[5];
            
            //fileID = fileID.toLowerCase();
            Integer saveCnt = map.get(fileID);
            if (saveCnt != null) {
                System.out.println("Duplicate:"
                        + " - lineCnt=" + lineCnt
                        + " - fileID=" + fileID
                );
                return;
            }
            map.put(fileID, lineCnt);
            if (false) System.out.println("Add:"
                        + " - lineCnt=" + lineCnt
                        + " - fileID=" + fileID
                );
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (Exception ex) { }
        }
        
    }
}
