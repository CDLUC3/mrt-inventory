
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
import java.util.Properties;
import java.util.Vector;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.extract.StoreState;
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

public class InvMainNode
{
    private static final String NAME = "InvMainNode";
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
                "resources/InvNode.properties",
                "resources/Inv.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties storeLoadProp  = tFrame.getProperties();
            LoggerInf logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);;
            String storeStateBase = storeLoadProp.getProperty("storeStateBase");
            StoreState storeState = StoreState.getStoreState(storeStateBase, 125, logger);
            System.out.println(PropertiesUtil.dumpProperties("StoreProp", storeState.getStateProp()));
            InvNode invNode = new InvNode(logger);
            invNode.setState(storeState);
            Properties retrieveProp = invNode.retrieveProp();
            System.out.println(PropertiesUtil.dumpProperties("InvMainNode", retrieveProp));

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
