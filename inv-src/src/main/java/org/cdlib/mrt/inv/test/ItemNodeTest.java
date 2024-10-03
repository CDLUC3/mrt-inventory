
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;


import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.action.SaveObject;

/**
 * Load manifest.
 * @author  dloy
 */

public class ItemNodeTest
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
            Identifier objectID = new Identifier("ark:/99999/fk4dr3bzf");
            //Identifier objectID = new Identifier("ark:/99999/xxxxx");
            List<Integer> nodes = InvDBUtil.getNodes(objectID, connect, logger);
            if (nodes == null) {
                System.out.println("nodes null");
            } else {
                
                System.out.println("Nodes size=" + nodes.size());
                for (Integer node : nodes) {
                    System.out.println("Node=" + node);
                }
            }
            
            test(connect, new Identifier("ark:/99999/fk4dr3bzf"), 910, logger);          
            test(connect, new Identifier("ark:/99999/fk4dr3bzf"), 1001, logger);      
            test(connect, new Identifier("ark:/99999/xxxx"), 1001, logger);
            //System.out.println(itemRun.dump("test"));

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
