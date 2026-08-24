
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.inv.utility.DPRFileDB;

/**
 * Load manifest.
 * @author  dloy
 */

public class BuildConfig
{
    private static final String NAME = "AddPrimaryLocal";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    protected static final Logger log4j = LogManager.getLogger(); 
    
    private LoggerInf logger = null;
    private InvService service = null;
    DPRFileDB db = null;
    private long startObjectSeq = 0l;
    private long cnt = 0l;
    
    public BuildConfig(long startObjectSeq, DPRFileDB db, InvService service, LoggerInf logger)
        throws TException
    {
        this.startObjectSeq = startObjectSeq;
        this.db = db;
        this.service = service;
        this.logger = logger;
    }
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
                "resources/AddPrimaryLocal.properties"};
            InventoryConfig config = InventoryConfig.useYaml();
            

        } catch(Exception e) {
            log4j.error("Exception:" + e, e);
            
        } finally {
            try {
                db.shutDown();
            } catch (Exception ex) {
                System.out.println("db Exception:" + ex);
            }
        }
    }
}
