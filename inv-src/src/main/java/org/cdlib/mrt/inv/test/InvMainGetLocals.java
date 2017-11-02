
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.PrimaryLocalState;
import org.cdlib.mrt.inv.service.LocalContainerState;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;

/**
 * Load manifest.
 * @author  dloy
 */

public class InvMainGetLocals
{
    private static final String NAME = "InvMainAddList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    private InvService service = null;
    private InvFormatter formatter = null;
    
    public InvMainGetLocals(InvService service, LoggerInf logger)
        throws TException
    {
        this.service = service;
        this.logger = logger;
        formatter = new InvFormatter(logger);
        
    }

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        ArrayList<String> list = new ArrayList();
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/PrimeLocalTest.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            InvService service = InvService.getInvService(storeLoadProp);
            InvMainGetLocals pl = new InvMainGetLocals(service, logger);
            File inFile = new File("/replic/test/localid-mapper/t1.txt");
           // File inFile = new File("/replic/test/localid-mapper/e1.txt");
            //File inFile = new File("/replic/test/localid-mapper/test1000.txt");
            pl.processFile(inFile);
            
            service.shutdown();
            

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
    
    private void processFile(File fin) 
            throws TException
    {
        BufferedReader br = null;
        try {
            FileInputStream fis = new FileInputStream(fin);

            //Construct BufferedReader from InputStreamReader
            br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                processLine(line);
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
            try {
                br.close();
            } catch (Exception ex) { }
        }
    }
    
    private void processLine(String line)
        throws TException
    {
        try {
            System.out.println("Line:" + line);
            String [] part = line.split(",", 3);
            Identifier objectID = new Identifier(part[0]);
            Identifier ownerID = new Identifier(part[1]);
            String localIDs = part[2];
            getRow(objectID);
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    private void getRow(Identifier objectID)
        throws TException
    {
        try {
            LocalContainerState lcs = service.getLocal(objectID);
            if (lcs == null) System.out.println("LCS=null");
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println(formatter.formatIt(lcs));
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }
}
