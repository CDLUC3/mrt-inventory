
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.Properties;
import java.util.Set;


import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.formatter.FormatterAbs;
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.action.Versions;
import org.cdlib.mrt.inv.service.VersionsState;
import org.cdlib.mrt.utility.StateInf;

/**
 * Load manifest.
 * @author  dloy
 */

public class VersionsTest3
{
    private static final String NAME = "VersionsTest3";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        DPRFileDB db = null;
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/VersionTest.properties",
                "resources/Inv.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            db = new DPRFileDB(logger, invProp);
            Connection connect = db.getConnection(true);
            Identifier objectID = new Identifier("ark:/99999/fk4x92x0c"); // stg
            objectID = new Identifier("ark:/99999/fk42n5jrg"); //dev
            objectID = new Identifier("ark:/99999/fk4h71nh38"); //dev 5001
            objectID = new Identifier("ark:/99999/testinv9502"); //stg 9502
            objectID = new Identifier("ark:/99999/fk4xs60c0j"); //dev 9001
            objectID = new Identifier("ark:/99999/fk4h71nh38"); //dev 5001
            Long testVersion = null; //3L;
            //Identifier objectID = new Identifier("ark:/99999/xxxxx");
            
            
            test(objectID, testVersion, connect, logger);

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
    
    protected static void test(
            Identifier objectID,
            Long version,
            Connection connection,
            LoggerInf logger
            )
        throws TException
    {
        try {
            System.out.println("\n\n***test:"
                    + " - objectID=" + objectID.getValue()
                    + " - version=" + version
                    );
            
            Versions versions = Versions.getVersions(objectID, version, connection, logger);
            VersionsState state = versions.process();
            dump("Dump", state, logger);
            //dumpJson("Dump", state, logger);
            Set<String> keys = state.retrieveKeys();
            for (String key : keys) {
                System.out.println("key=" + key);
            }
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
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
    
    public static void dumpJson(String header, StateInf responseState, LoggerInf logger)
        throws TException
    {
        String xml = formatIt("json", logger, responseState);
        System.out.println("*****" + header + " XML:\n" + xml + "\n");
    }
}
