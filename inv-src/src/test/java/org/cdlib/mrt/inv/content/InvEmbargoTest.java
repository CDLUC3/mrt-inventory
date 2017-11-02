/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cdlib.mrt.inv.content;

import java.util.Arrays;
import java.util.Properties;
import java.io.File;
import org.cdlib.mrt.utility.DateUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;


import java.util.Date;

/**
 *
 * @author dloy
 */
public class InvEmbargoTest {
    private static final int MAXW = 5394;
    private LoggerInf logger = null;
    public InvEmbargoTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);
        } catch (Exception ex) {
            logger = null;
        }
    }

    @After
    public void tearDown() {
    }



    @Test
    public void buildZ()
    {

        try {
            Properties txtProp = new Properties();
            txtProp.setProperty("embargoEndDate","2019-12-31T11:15:01Z");
            long objectseq =7777;
            InvEmbargo ie = InvEmbargo.getInvEmbargoFromTxt(objectseq, txtProp, logger);
            String d1 = ie.getEmbargoEndDatDB();
            System.out.println(PropertiesUtil.dumpProperties(d1, txtProp));
            System.out.println(ie.dump("buildZ"));

            assertTrue(true);

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            assertFalse("Exception:" + ex, true);
        }
    }
}