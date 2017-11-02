
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;


import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.db.DBUtil;
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

/**
 * Load manifest.
 * @author  dloy
 */

public class RDSFailover
{
    private static final String NAME = "RDSFailover";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;

    protected DPRFileDB db = null;
    protected LoggerInf logger = null;
    protected int maxTests = 10000;
    protected int testCnt = 0;
    
    public RDSFailover(Properties invProp, LoggerInf logger)
        throws TException
    {       
        try {
            this.logger = logger;
            db = new DPRFileDB(logger, invProp);
            //System.out.println(itemRun.dump("test"));

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
                throw new TException(e);
        }
        
    }
    /**
     * Main method
     */
    public static void main(String args[])
    {
    
        
        try {
            
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            String propertyList[] = {
                "resources/RDSFailover.properties"};
            TFrame tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            RDSFailover failover = new RDSFailover(invProp, logger);
            failover.test();

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        } 
    }
    
    protected void test()
        throws TException
    {
        try {
            for (int i=1; i <= maxTests; i++) {
                getId(i);
            }
            
            System.out.println("Valid");
            
        } catch (TException.EXTERNAL_SERVICE_UNAVAILABLE esu) {
            System.out.println("External Service error not corrected:");
            throw esu;
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            
        } finally {
            try {
                db.shutDown();
            } catch (Exception ex) {
                System.out.println("db Exception:" + ex);
            }
        }
    }
    
    public void getId(int id)
        throws TException
    {
        String sql = null;
        Connection connect = null;
        try {
            connect = db.getConnection(true);
            sql = "select current_timestamp,inv_object_id,inv_version_id from inv_files  where id = " + id + ";";
            System.out.println("\n***Test(" + id + ") sql=" + sql);
            
            if (id==10) connect.close();
            Properties [] props = DBUtil.cmd(connect, sql, logger);
            if ((props == null) || (props.length == 0)) {
                System.out.println(">>>No response");
                return;
            }
            System.out.println(PropertiesUtil.dumpProperties(">>>OK response",props[0]));
            
        } catch (Exception ex) {
            System.out.println(">>>EXCEPTION:" + ex);
            //ex.printStackTrace();
            
        } finally {
            try {
                connect.close();
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.out.println("Exception attempting to close connection" + ex);
            }
        }
    
    }
}
