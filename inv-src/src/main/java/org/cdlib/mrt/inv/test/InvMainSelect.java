
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;




import org.cdlib.mrt.zoo.ZooQueue;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.InvSelectState;
import org.cdlib.mrt.formatter.FormatterAbs;
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.utility.PropertiesUtil;
/**
 * Load manifest.
 * @author  dloy
 */

public class InvMainSelect
{
    private static final String NAME = "InvMainSelect";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        InvService service = null;

        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/InvMainSelect.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            service = InvService.getInvService(storeLoadProp);
            String sql = storeLoadProp.getProperty("select");
            InvSelectState selectState = service.select(sql);
            dump(MESSAGE, selectState, service.getLogger());

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
                
        } finally {
            if (service != null) {
                try {
                    service.shutdown();
                } catch (Exception ex) { }
            }
        }
    }
    
    public static void add(Properties storeLoadProp, InvService service, ZooQueue zooQueue)
    {        
        try {
            System.out.println("***** ADD *****");
            for (int i=1; true; i++) {
                String manifestURL = storeLoadProp.getProperty("manifestURL." + i);
                if (manifestURL == null) break;
                Properties loadProp = new Properties();
                loadProp.setProperty("manifestURL", manifestURL);
                
                try {
                    service.addZoo(loadProp, zooQueue);
                    System.out.println("****************> ZOO ADD:" + manifestURL);
                } catch (Exception ex) {
                    System.out.println("Exception:" + ex);
                    continue;
                }
            }

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
        if (StringUtil.isEmpty(retVal)) {
            
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property:" + key);
        }
        return retVal;
    } 

    public static String formatIt(
            String formatTypeS,
            LoggerInf logger,
            StateInf responseState)
    {
        try {
            if (responseState == null) {
               return "NULL RESPONSE";
            }
            FormatterInf.Format formatType = null;
            formatType = FormatterInf.Format.valueOf(formatTypeS);
            FormatterInf formatter = FormatterAbs.getFormatter(formatType, logger);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(5000);
            PrintStream  stream = new PrintStream(outStream, true, "utf-8");
            formatter.format(responseState, stream);
            stream.close();
            byte [] bytes = outStream.toByteArray();
            String retString = new String(bytes, "UTF-8");
            return retString;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            System.out.println("Trace:" + StringUtil.stackTrace(ex));
            return null;
        }
    }
    
    public static void dump(String header, StateInf responseState, LoggerInf logger)
        throws TException
    {
        String xml = formatIt("xml", logger, responseState);
        System.out.println("*****" + header + " XML:\n" + xml + "\n");
    }
}
