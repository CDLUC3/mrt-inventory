
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.service.LocalContainerState;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.inv.content.InvAddLocalID;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.PropertiesUtil;

/**
 * Load manifest.
 * @author  dloy
 */

public class AddPrimaryLocal
{
    private static final String NAME = "AddPrimaryLocal";
    private static final String MESSAGE = NAME + ": ";
    protected static final Logger log4j = LogManager.getLogger(); 

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    private InvService service = null;
    DPRFileDB db = null;
    private long startObjectSeq = 0l;
    private long cnt = 0l;
    
    public AddPrimaryLocal(long startObjectSeq, DPRFileDB db, InvService service, LoggerInf logger)
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
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties invProp  = tFrame.getProperties();
            log4j.debug(PropertiesUtil.dumpProperties(MESSAGE + "main", invProp));
            InvService service = InvService.getInvService(InventoryConfig.useYaml());
            
            db = new DPRFileDB(logger, invProp);
            AddPrimaryLocal pl = new AddPrimaryLocal(0l, db, service, logger);
            pl.process();
            
            //service.shutdown();
            

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


    protected static String get(Properties prop, String key)
        throws TException
    {
        String retVal = prop.getProperty(key);
        if (StringUtil.isEmpty(retVal)) return null;
        return retVal;
    } 
    
    private void process() 
            throws TException
    {
        
        Connection connect = null;
        try {
            long objectseq = startObjectSeq;
            while(true) {
                connect = db.getConnection(true);
                InvAddLocalID local = InvDBUtil.getNextLocal(objectseq, connect, logger);
                if (local == null) break;
                processAdd(local);
                objectseq = local.getObjectseq();
                //if (cnt > 20) break;
                connect.close();
            }
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            log4j.error("TException:" + tex, tex);
            throw tex;
            
        } catch (Exception ex) {
            log4j.error("Exception:" + ex, ex);
            throw new TException(ex);
            
        } finally {
            try {
                connect.close();
            } catch (Exception dex) {} 
        }
    }
    
    private void processAdd(InvAddLocalID local)
        throws TException
    {
        try {
            log4j.debug(PropertiesUtil.dumpProperties("dump", local.getProp()));
            add(local.getObjectArk(), local.getOwnerArk(), local.getLocalIDs());
            cnt++;
            if ((cnt % 100) == 0) {
                System.out.println(PropertiesUtil.dumpProperties("dump(" + cnt + ")", local.getProp()));
            }
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            System.out.println("throw tex;" + tex);
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    private void add(Identifier objectID, Identifier ownerID, String localIDs)
        throws TException
    {
        try {
            localIDs = stripOne(localIDs);
            LocalContainerState lcs = service.addPrimary(objectID, ownerID, localIDs);
            
            if (DEBUG) {
                System.out.println("\nAdd%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                InvFormatter formatter = new InvFormatter(FormatterInf.Format.xml, logger);
                System.out.println(formatter.formatIt(lcs));
            }
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    
    
    private String stripOne(String localIDs)
        throws TException
    {
        try {
            String [] ids = localIDs.split("\\s*\\;\\s*");
            StringBuffer buf = new StringBuffer();
            boolean first = true;
            for (String id : ids) {
                if (first) {
                    first = false;
                    continue;
                }
                if (buf.length() > 0) {
                    buf.append(" ; ");
                }
                buf.append(id);
            }
            return buf.toString();
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    private ArrayList getLocalList(String localIDs)
        throws TException
    {
        try {
            String [] ids = localIDs.split("\\s*\\;\\s*");
            ArrayList<String> list = new ArrayList<>();
            boolean first = true;
            for (String id : ids) {
                if (first) {
                    first = false;
                    continue;
                }
                list.add(id);
            }
            return list;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
}
