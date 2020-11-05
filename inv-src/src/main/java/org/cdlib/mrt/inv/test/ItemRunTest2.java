
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.util.Properties;

import java.sql.Connection;

import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.inv.zoo.ItemRun;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.zoo.ItemInfo;

/**
 * Load manifest.
 * @author  dloy
 */

public class ItemRunTest2
{
    private static final String NAME = "ItemRunTest";
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
                "resources/Zookeeper.properties",
                "resources/InvTest.properties",
                "resources/Inv.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            
            InvService service = InvService.getInvService(InventoryConfig.useYaml());
            ItemInfo info = ItemInfo.getItemInfo("", ItemInfo.Status.PENDING, invProp);
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", invProp));
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            Connection connect = service.getInvServiceProperties().getConnection(false);
            ItemRun itemRun = ItemRun.getItemRun(info, connect, logger);
            System.out.println(itemRun.dump("test"));
            itemRun.process();

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
