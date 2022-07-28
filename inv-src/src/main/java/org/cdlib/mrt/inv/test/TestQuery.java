
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
import org.cdlib.mrt.inv.service.InventoryConfig;

/**
 * Load manifest.
 * @author  dloy
 */

public class TestQuery
{
    private static final String NAME = "ItemNodeTest";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;

    /**
     * Main method
     */
    public static void main(String args[])
            throws Exception
    {

        TFrame tFrame = null;
        DPRFileDB db = null;
        LoggerInf logger = null;
        //Identifier ark = new Identifier("ark:/13030/m57x1fhb");
        //long versionNum = 2;
        //String pathname = "producer/O'BrienEtAl_HammingDistanceValues.xlsx";
        
        try {
            InventoryConfig config = InventoryConfig.useYaml();
            config.dbStartup();
            db = config.getDb();
            Connection connection = db.getConnection(true);
            logger=config.getLogger();
            test ("ark:/20775/bb0000989b", 1, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 2, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 3, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 4, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 5, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 6, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 7, "producer/mets.xml", connection, logger);
            test ("ark:/20775/bb0000989b", 8, "producer/mets.xml", connection, logger);
            //test ("ark:/20775/bb05468972", 5, "producer/1-1.pdf", connection, logger);
            

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

    public static void test(
            String arkS,
            long versionNum,
            String pathname,
            Connection connection,
            LoggerInf logger)
        throws Exception
    {
        
            Identifier ark = new Identifier(arkS);
            long vercnt = InvDBUtil.getAccessVersionNum(ark, versionNum, pathname, connection, logger);
            System.out.println("***TEST - "
                    + " - arkS=" + arkS
                    + " - versionNum=" + versionNum
                    + " - pathname=" + pathname
                    + " - vercnt=" + vercnt
            );
    }
}