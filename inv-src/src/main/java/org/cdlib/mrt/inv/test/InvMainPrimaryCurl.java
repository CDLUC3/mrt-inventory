
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Load manifest.
 * @author  dloy
 */

public class InvMainPrimaryCurl
{
    private static final String NAME = "InvMainAddList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    private InvService service = null;
    private String urlS = null;
    
    public InvMainPrimaryCurl(InvService service, String urlS, LoggerInf logger)
        throws TException
    {
        this.service = service;
        this.logger = logger;
        this.urlS = urlS;
    }

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
//        ArrayList<String> list = new ArrayList();
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/PrimeLocalTest.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            InvService service = InvService.getInvService(InventoryConfig.useYaml());
            String urlS = "http://uc3-mrt-inv-dev.cdlib.org:26121/mrtinv/primary";
            InvMainPrimaryCurl pl = new InvMainPrimaryCurl(service, urlS, logger);
            File inFile = new File("/replic/test/localid-mapper/t5.txt");
            File outFile = new File("/replic/test/localid-mapper/get5.sh");
            //File inFile = new File("/replic/test/localid-mapper/t5.txt");
            //File inFile = new File("/replic/test/localid-mapper/test1000.txt");
            pl.processFile(inFile, outFile);
            
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
    
    private void processFile(File fin, File out) 
            throws TException
    {
        BufferedReader br = null;
        BufferedWriter writer = null;
        try {
            FileInputStream fis = new FileInputStream(fin);

            //Construct BufferedReader from InputStreamReader
            br = new BufferedReader(new InputStreamReader(fis));
            writer = new BufferedWriter(new FileWriter(out));
            String line = null;
            while ((line = br.readLine()) != null) {
                processLine(writer, line);
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
            try {
                writer.close();
            } catch (Exception ex) { }
        }
    }
    
    private void processLine(BufferedWriter writer, String line)
        throws TException
    {
        try {
            System.out.println("Line:" + line);
            String [] part = line.split(",", 3);
            Identifier objectID = new Identifier(part[0]);
            Identifier ownerID = new Identifier(part[1]);
            String localIDs = part[2];
            addRow(writer, objectID, ownerID, localIDs);
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    private void addRow(BufferedWriter writer, Identifier objectID, Identifier ownerID, String localIDs)
        throws TException
    {
        try {
            localIDs = stripFirst(localIDs);
            StringBuffer buf = new StringBuffer();
            buf.append("curl -X GET '" + urlS 
                    + "/" + URLEncoder.encode(ownerID.getValue(), "utf-8")
                    + "/" + URLEncoder.encode(localIDs, "utf-8") + "?t=xml'"
                    + "\n");
            buf.append("printf \"\\n\"\n");
            writer.write(buf.toString());
       
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    private String stripFirst(String localIDs)
        throws TException
    {
        try {
            String [] ids = localIDs.split("\\s*\\;\\s*");
            StringBuffer buf = new StringBuffer();
            boolean first = true;
            for (String id : ids) {
                if (first) {
                    first = false;
                    continue;
                }
                if (buf.length() > 0) {
                    buf.append(" ; ");
                }
                buf.append(id);
            }
            return buf.toString();
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
}
