
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.sql.Connection;
import java.util.Properties;


import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.utility.DBDelete;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;

/**
 * Load manifest.
 * @author  dloy
 */

public class TestDBDelete
{
    private static final String NAME = "TestDBDelete";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;
    //private static String arkS = "ark:/99999/xxxxx";
    private static String arkS = "ark:/99999/testlocal";

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
            Identifier objectID = new Identifier(arkS);
            System.out.println("TestDBDelete before:"
                    + " - objectID=" + objectID.getValue()
            );
            //Identifier objectID = new Identifier("ark:/99999/xxxxx");
            InvObject invObject = InvDBUtil.getObject(objectID, connect, logger);
            System.out.println("TestDBDelete before:"
                    + " - objectID=" + objectID.getValue()
                    + " - oid=" + invObject.getId()
            );
            DBDelete dbDelete = new DBDelete(invObject.getId(), connect, logger);
            int delCnt = dbDelete.delete();
            System.out.println("TestDBDelete after:"
                    + " - objectID=" + objectID.getValue()
                    + " - oid=" + invObject.getId()
                    + " - delCnt=" + delCnt
            );

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
}
