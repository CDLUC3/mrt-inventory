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
public class InvObjectTest {
    private static final int MAXW = 5394;
    private LoggerInf logger = null;
    public InvObjectTest() {
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
    public void match()
    {

        try {
            System.out.println("entered");
            for (int i=(MAXW-10); i < MAXW + 10; i++) {
                testIt(i);
            }

            assertTrue(true);

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            assertFalse("Exception:" + ex, true);
        }
    }
    
    protected String getStringWithLengthAndFilledWithCharacter(int length, char charToFill) {
      if (length > 0) {
        char[] array = new char[length];
        Arrays.fill(array, charToFill);
        return new String(array);
      }
      return "";
    }

    public void testIt(int len)
        throws TException
    {
 
            String tval = getStringWithLengthAndFilledWithCharacter(len,'X');
            String out = InvObject.setW(tval);
            String disp = out.substring(out.length() - 10);
            System.out.println("testIt"
                    + " - tval.length()=" + tval.length()
                    + " - out.length()=" + out.length()
                    + " - disp=" + disp
                    );
            if (len > MAXW) {
                assertTrue(out.endsWith("..."));
            }
            assertTrue(out.length() <= MAXW);
    }

}