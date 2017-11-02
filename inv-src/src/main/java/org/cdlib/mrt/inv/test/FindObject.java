
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

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
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.inv.zoo.ItemRun;
import org.cdlib.mrt.inv.action.SaveObject;
import org.cdlib.mrt.inv.content.InvObject;

/**
 * Load manifest.
 * @author  dloy
 */

public class FindObject
{
    private static final String NAME = "ItemNodeTest";
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
                "resources/InvTest.properties",
                "resources/Inv.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            db = new DPRFileDB(logger, invProp);
            Connection connect = db.getConnection(true);
            Identifier objectID = new Identifier("ark:/99999/fk46m3qmz");
            InvObject invObject = InvDBUtil.getObject(objectID, connect, logger);
            if (invObject == null) {
                System.out.println("Not found:" + objectID.getValue());
            } else {
                System.out.println("Found:" + invObject.getId());
            }
            

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
            Connection connection,
            Identifier objectID,
            int testNode,
            LoggerInf logger
            )
        throws TException
    {
        try {
            System.out.println("***test:"
                    + " - testNode=" + testNode
                    + " - objectID=" + objectID.getValue()
                    );
            SaveObject.isValidNode(testNode, objectID, connection, logger);
            System.out.println("Valid");
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
}
