
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.service;

import java.io.File;
import java.util.Properties;

import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.zoo.ZooManager;
import org.cdlib.mrt.zoo.ZooQueue;
import org.cdlib.mrt.utility.PropertiesUtil;

/**
 * Load manifest.
 * @author  dloy
 */

public class InvMainZooList
{
    private static final String NAME = "InvMainZooList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;
    private static ZooManager zooManager = null;
    private static ZooQueue queue = null;

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
                "resources/InvZooList.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties storeLoadProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", storeLoadProp));
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
            LoggerInf logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);
            zooManager = ZooManager.getZooManager(logger, storeLoadProp);
            queue = ZooQueue.getZooQueue(zooManager);
            InvZooList runList = new InvZooList(runProp, queue, logger);
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
