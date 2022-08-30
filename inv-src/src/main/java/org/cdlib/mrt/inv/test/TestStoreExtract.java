
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
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;

import org.cdlib.mrt.inv.extract.StoreExtract;

/**
 * Load manifest.
 * @author  dloy
 */

public class TestStoreExtract
{
    private static final String NAME = "TestStoreExtract";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    private InvService service = null;
    
    public TestStoreExtract(InvService service, LoggerInf logger)
        throws TException
    {
        this.service = service;
        this.logger = logger;
    }

    /**
     * Main method
     */
    public static void main(String args[])
    {

        try {
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            //test("http://storage.cdlib.org:35121/content/9501/ark%3A%2F13030%2Fm5f245xs/0/system%2Fmrt-owner.txt?fixity=no", logger);
            //test("http://storage.cdlib.org:35121/content/9501/ark%3A%2F13030%2Fm5f245xs/0/system%2Fmrt-xxxxx.txt?fixity=no", logger);
            test("http://storage.cdlib.org:35121/content/3041/ark%3A%2F13030%2Fm5sr4hcr/2/producer%2Fmrt-oaidc.xml?fixity=no", logger);
            
            //service.shutdown();
            

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }

    public static void test(String urlS, LoggerInf logger)
        throws TException
    {
        String result = StoreExtract.getString(urlS, logger, 3);
        System.out.println("Test\n"
                + " - urlS=" + urlS + "\n"
                + " - result=\n" + result + "\n"
        );
    }
}
