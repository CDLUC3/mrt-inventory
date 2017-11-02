
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
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.inv.zoo.ItemRun;
import org.cdlib.mrt.inv.action.IngestMod;

/**
 * Load manifest.
 * @author  dloy
 */

public class IngestModTest
{
    private static final String NAME = "IngestModTest";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;

    /**
     * Main method
     */
    public static void main(String args[])
    {
        long versionseq = 1;
        String ingestURL = "http://uc3-mrt-store-stg.cdlib.org:35121/content/2111/ark%3A%2F13030%2Fc80000t8/1/system%2Fmrt-ingest.txt?fixity=no";
        TFrame tFrame = null;
        DPRFileDB db = null;
        Connection connect = null;
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/IngestMod.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            db = new DPRFileDB(logger, invProp);
            
            connect = db.getConnection(false);
            IngestMod ingestMod = IngestMod.getIngestMod(
                versionseq,
                ingestURL,
                connect,
                logger);
            ingestMod.process();
            connect.close();
            

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
