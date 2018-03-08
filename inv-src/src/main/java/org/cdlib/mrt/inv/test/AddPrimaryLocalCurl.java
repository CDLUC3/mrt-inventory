
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
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
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Load manifest.
 * @author  dloy
 */

public class AddPrimaryLocalCurl
{
    private static final String NAME = "TestCurlPrimaryLocal";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    private String base = null;
    
    public AddPrimaryLocalCurl(String base, LoggerInf logger)
        throws TException
    {
        this.base = base;
        this.logger = logger;
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
                "resources/TestCurlPrimaryLocal.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties storeLoadProp  = tFrame.getProperties();
            String inFileS = storeLoadProp.getProperty("inFile");
            String outFileS = storeLoadProp.getProperty("outFile");
            String base = storeLoadProp.getProperty("base");
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            AddPrimaryLocalCurl pl = new AddPrimaryLocalCurl(base, logger);
            //File inFile = new File("/replic/test/localid-mapper/t5.txt");
            File inFile = new File(inFileS);
            File outFile = new File(outFileS);
            //File inFile = new File("/replic/test/localid-mapper/t5.txt");
            //File inFile = new File("/replic/test/localid-mapper/test1000.txt");
            pl.processFile(inFile,outFile);
            
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
    
     private void processFile(File inFile, File outFile) 
            throws TException
    {
        BufferedReader br = null;
        BufferedWriter writer = null;
        try {
            FileInputStream fis = new FileInputStream(inFile);

            //Construct BufferedReader from InputStreamReader
            br = new BufferedReader(new InputStreamReader(fis));
            writer = new BufferedWriter(new FileWriter(outFile));

            //Construct BufferedReader from InputStreamReader
            br = new BufferedReader(new InputStreamReader(fis));

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
                writer.close();
            } catch (Exception ex) { }
        }
    }
    
    private void processLine(BufferedWriter writer,String line)
        throws TException
    {
        try {
            System.out.println("\n\n#####Line:" + line);
            String [] part = line.split(",", 3);
            Identifier objectID = new Identifier(part[0]);
            Identifier ownerID = new Identifier(part[1]);
            String localIDs = part[2];
            String addCmd = add(objectID, ownerID, localIDs);
            writer.write(addCmd);
            //getPrimary(ownerID, localIDs);
            //getLocal(objectID);
            //delete(objectID);
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    private String add(Identifier objectID, Identifier ownerID, String localIDs)
        throws TException
    {
        try {
            localIDs = stripOne(localIDs);
            StringBuffer buf = new StringBuffer();
            buf.append("curl -X POST ");
            buf.append("-F \"objectid=" + objectID.getValue() + "\" ");
            buf.append("-F \"ownerid=" + ownerID.getValue() + "\" ");
            buf.append("-F \"localids=" + localIDs + "\" ");
            buf.append("-F \"response-form=xml\" ");
            buf.append(" '" + base + "/primary'");
            return buf.toString();
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    private void getPrimary(Identifier ownerID, String localIDs)
        throws TException
    {
        try {
            List<String> list = getLocalList(localIDs);
            StringBuffer buf = new StringBuffer();
            System.out.println("\nPrimary");
            buf.append("curl -X GET '");
            buf.append(base);
            buf.append("/primary");
            String encodeOwner = URLEncoder.encode(ownerID.getValue(), "utf-8");
            buf.append("/" + encodeOwner + "/");
            for (String localid : list) {
                String encodeLocalID = URLEncoder.encode(localid, "utf-8");
                String out = buf.toString() + encodeLocalID + "?t=xml'";
                System.out.println(out);
            }
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    
    private void getLocal(Identifier objectID)
        throws TException
    {
        try {
            StringBuffer buf = new StringBuffer();
            System.out.println("\nLocal");
            buf.append("curl -X GET '");
            buf.append(base);
            buf.append("/local");
            String encodeObject = URLEncoder.encode(objectID.getValue(), "utf-8");
            buf.append("/" + encodeObject + "?t=xml'");
            System.out.println(buf.toString());
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    
    private void delete(Identifier objectID)
        throws TException
    {
        try {
            StringBuffer buf = new StringBuffer();
            System.out.println("\nDelete");
            buf.append("curl -X DELETE '");
            buf.append(base);
            buf.append("/primary");
            String encodeObject = URLEncoder.encode(objectID.getValue(), "utf-8");
            buf.append("/" + encodeObject + "?t=xml'");
            System.out.println(buf.toString());
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    
    
    private String stripOne(String localIDs)
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
    
    private ArrayList<String> getLocalList(String localIDs)
        throws TException
    {
        try {
            String [] ids = localIDs.split("\\s*\\;\\s*");
            ArrayList<String> list = new ArrayList<>();
            boolean first = true;
            for (String id : ids) {
                if (first) {
                    first = false;
                    continue;
                }
                list.add(id);
            }
            return list;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
}
