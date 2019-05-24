
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;


import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.inv.action.SaveNode;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.inv.zoo.ItemRun;
import org.cdlib.mrt.inv.action.SaveObject;

/**
 * Load manifest.
 * @author  dloy
 */

public class SaveNodeTest
{
    private static final String NAME = "SaveNodeTest";
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
                "resources/SaveNodeTest.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            System.out.println(PropertiesUtil.dumpProperties("invProp", invProp));
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            db = new DPRFileDB(logger, invProp);
            String storageBase=
                    "http://uc3-mrtreplic2-dev:35121";
            
            //test(db, 9513, logger, storageBase, false);
            test(db, 9513, logger, storageBase, true);
            //test(db, 9513, logger, null, true);
            //test(db, 9666, logger, storageBase, true);
            //test(db, 8001, logger, true);
            //test(db, 7001, logger, true);

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
            DPRFileDB db,
            int nodeNumber,
            LoggerInf logger,
            String storageBase,
            boolean reset
            )
        throws TException
    {
        Connection connect = null;
        try {
            connect = db.getConnection(false);
            System.out.println(MESSAGE + "TEST"
                    + " - nodeNumber=" + nodeNumber + "\n"
            );
            SaveNode saveNode = SaveNode.getSaveNode(nodeNumber, connect, storageBase, logger);
            if (reset) {
                saveNode.resetInvNode();
            }
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            
        } finally {
            if (connect != null) {
                try {
                    connect.close();
                    
                } catch (Exception ex) { }
            }
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
