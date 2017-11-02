
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.InvServiceState;
import org.cdlib.mrt.formatter.FormatterAbs;
import org.cdlib.mrt.formatter.FormatterInf;
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

public class InvMainState
{
    private static final String NAME = "InvMain";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/InvMainState.properties"};
            tFrame = new TFrame(propertyList, "InvState");
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            InvService service = InvService.getInvService(storeLoadProp);
            LoggerInf logger = service.getLogger();
            dump("initial state", service.getInvServiceState(), logger);
            dump("shutdown", service.shutdown(), logger);
            dump("startup", service.startup(), logger);
            dump("shutdown", service.shutdownZoo(), logger);
            dump("startupZoo", service.startupZoo(), logger);

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
