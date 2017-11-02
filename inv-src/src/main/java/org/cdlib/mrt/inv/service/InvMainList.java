
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import org.cdlib.mrt.inv.service.InvDBList;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.TallyTable;
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

public class InvMainList
{
    private static final String NAME = "InvMainList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/InvTest.properties",
                "resources/InvMainList.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
            InvService service = InvService.getInvService(storeLoadProp);
            LoggerInf logger = service.getLogger();
            String runPropS  = null;
            if (args.length == 0) {
                runPropS  = get(storeLoadProp, "runProps");
            } else {
                runPropS = args[0];
            }
            if (StringUtil.isEmpty(runPropS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "runPropS missing");
            }
            File runPropF = new File(runPropS);
            if (!runPropF.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "runPropF missing:" + runPropF.getCanonicalPath());
            }
            Properties runProp = PropertiesUtil.loadFileProperties(runPropF);
            InvDBManifestList runList = new InvDBManifestList(runProp, service, logger);
            if (DEBUG) System.out.println(runList.dump(MESSAGE));
            runList.run();

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
}
