
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.admin;

import org.cdlib.mrt.inv.test.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.PrimaryLocalState;
import org.cdlib.mrt.inv.service.LocalContainerState;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.inv.action.AddLocalAfterTo;
import org.cdlib.mrt.inv.content.InvAddLocalID;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.inv.service.LocalAfterToState;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.json.JSONObject;

/**
 * Load manifest.
 * @author  dloy
 */

public class TestAdminServiceCollection
{
    private static final String NAME = "TestAddLocalAfterTo";
    private static final String MESSAGE = NAME + ": ";
    
    public static void main(String args[])
    {

        try {
            InventoryConfig invConfig = InventoryConfig.useYaml();
            invConfig.dbStartup();
            InvService service = InvService.getInvService(invConfig);
            service.shutdownZoo();
            
           
            Identifier pubCollectionID = new Identifier("ark:/99999/testcollectpub"); // new Owner ID
            String pubName = "test admin Collection public";
            String pubMnemonic = "ta-collectpub";
            JSONObject jsonPub = service.addAdminCollection(false, pubCollectionID, pubName, pubMnemonic);
            System.out.println(jsonPub.toString(2));
            System.out.println("*******************************************");
            Identifier prvCollectionID = new Identifier("ark:/99999/testcollectprv"); // new Owner ID
            String prvName = "test admin Collection private";
            String prvMnemonic = "ta-collectprv";
            JSONObject jsonPrv = service.addAdminCollection(true, prvCollectionID, prvName, prvMnemonic);
            System.out.println(jsonPrv.toString(2));
            //service.shutdown();
            

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        } 
    }
}
