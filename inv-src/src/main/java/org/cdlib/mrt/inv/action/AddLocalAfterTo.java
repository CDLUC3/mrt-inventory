
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.action;

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
import org.cdlib.mrt.inv.content.InvAddLocalID;
import org.cdlib.mrt.inv.service.LocalAfterToState;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.utility.InvFormatter;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
/**
 * Load manifest.
 * @author  dloy
 */

public class AddLocalAfterTo
{
    private static final String NAME = "AddPrimaryLocal";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = false;
    
    private LoggerInf logger = null;
    private InvService service = null;
    DPRFileDB db = null;
    private long afterSeq = 0l;
    private long toSeq = 0l;
    private long cnt = 0l;
    private long existErrors = 0l;
    private LocalAfterToState state = null;
    
    public static AddLocalAfterTo getAddLocalAfterTo(
            long afterSeq, long toSeq, DPRFileDB db, InvService service, LoggerInf logger)
        throws TException
    {
        return new AddLocalAfterTo (afterSeq, toSeq, db, service, logger);
    }
    
    protected AddLocalAfterTo(long afterSeq, long toSeq, DPRFileDB db, InvService service, LoggerInf logger)
        throws TException
    {
        this.afterSeq = afterSeq;
        this.toSeq = toSeq;
        this.db = db;
        this.service = service;
        this.logger = logger;
        state = LocalAfterToState.buildLocalAfterToState(afterSeq, toSeq);
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
                "resources/AddPrimaryLocalLogger.properties",
                "resources/AddPrimaryLocal.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            LoggerInf logger = tFrame.getLogger();
            Properties invProp  = tFrame.getProperties();
            File localInfo = new File("local-info.txt");
            if (localInfo.exists()) {
                invProp.load(new FileInputStream(localInfo));
            }
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", invProp));
            InvService service = InvService.getInvService(invProp);
            service.shutdownZoo();
            
            db = new DPRFileDB(logger, invProp);
            AddLocalAfterTo alat = new AddLocalAfterTo(0l, 1500l,db, service, logger);
            LocalAfterToState state = alat.process();
            System.out.println("\nLocalAfterToState%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            InvFormatter formatter = new InvFormatter(FormatterInf.Format.xml, logger);
            System.out.println(formatter.formatIt(state));
            
            //service.shutdown();
            

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


    protected static String get(Properties prop, String key)
        throws TException
    {
        String retVal = prop.getProperty(key);
        if (StringUtil.isEmpty(retVal)) return null;
        return retVal;
    } 
    
    public LocalAfterToState process() 
            throws TException
    {
        Connection connect = null;
        try {
            long objectseq = afterSeq;
            InvAddLocalID local = null;
            while(true) {
                connect = db.getConnection(true);
                local = InvDBUtil.getNextLocal(objectseq, connect, logger);
                if (local == null) break;
                if (local.getObjectseq() > toSeq) break;
                processAdd(local);
                objectseq = local.getObjectseq();
                connect.close();
                state.setLastID(local.getObjectseq());
            }
            return state;
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            System.out.println("Exception:" + tex);
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
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
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("dump", local.getProp()));
            add(local.getObjectArk(), local.getOwnerArk(), local.getLocalIDs());
            cnt++;
            if ((cnt % 100) == 0) {
                logger.logMessage(PropertiesUtil.dumpProperties("dump(" + cnt + ")", local.getProp()), 0 , true);
            }
            state.bumpAdded();
 
	//Construct BufferedReader from InputStreamReader
        } catch (TException tex) {
            logger.logError("Add error:" + tex, 0);
            state.bumpExistErrors();
            
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
