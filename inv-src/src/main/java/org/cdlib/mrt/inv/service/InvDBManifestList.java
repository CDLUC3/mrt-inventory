
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;


import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;


import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.utility.PropertiesUtil;
/**
 * Load manifest.
 * @author  dloy
 */

public class InvDBManifestList
{
    private static final String NAME = "InvDBList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;
    protected LoggerInf logger = null;
    //protected FixityItemDB db = null;
    protected ComponentContent content = null;
    //protected Manifest manifest = null;
    protected File listFile = null;
    protected InvService service = null;
    protected BufferedReader reader = null;
    protected Vector <String> exArr = new Vector <String> (100);
    protected int maxExceptions = 100;
    //protected int nodeID = 0;
    protected int start = 0;
    protected int last = 1000000;
    protected Properties runProp = null;
    
    public String dump(String header) 
        throws TException
    {
        StringBuffer buf = new StringBuffer();
        buf.append("***" + header + "***\n"
                + " - start" + start + "\n"
                + " - last" + last + "\n"
                );
        return buf.toString();
    }

    public InvDBManifestList(
            Properties runProp,
            InvService service,
            LoggerInf logger)
        throws TException
    {
        try {
            log(PropertiesUtil.dumpProperties(MESSAGE, runProp));
            start = getInv(runProp, "start", 0);
            last = getInv(runProp, "last", 1000000);
            String listFileS = get(runProp, "listFile");
            listFile = new File(listFileS);
            if (!listFile.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "file does not exist:" + listFile.getCanonicalFile()
                        );
            }

            this.logger = logger;
            this.listFile = listFile;
            this.service = service;
            set(start, last, listFile, service, logger);


        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public InvDBManifestList(
            int start,
            int last,
            File listFile,
            InvService service,
            LoggerInf logger)
        throws TException
    {
        set(start, last, listFile, service, logger);
    }

    public void set(
            int start,
            int last,
            File listFile,
            InvService service,
            LoggerInf logger)
        throws TException
    {
        this.start = start;
        this.last = last;
        this.logger = logger;
        this.listFile = listFile;
        this.service = service;
        validate();
    }
    
    protected static int getInv(Properties prop, String key, int defaultVal)
        throws TException
    {
        String retValS = prop.getProperty(key);
        if (StringUtil.isEmpty(retValS)) {
            if (defaultVal >= 0) return defaultVal;
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "missing property:" + key);
        }
        try {
            return Integer.parseInt(retValS);
            
        } catch (Exception ex) {
            throw new TException.INVALID_DATA_FORMAT("Invalid numeric:" + retValS);
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
    protected void validate()
        throws TException
    {
        setList();
    }

    protected void setList()
        throws TException
    {
        try {
            FileInputStream inStream = new FileInputStream(listFile);
            DataInputStream in = new DataInputStream(inStream);
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));


        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public void run()
        throws TException
    {
        try {
            int linecnt = 0;
            for (int iOut=0; true; iOut++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("#")) continue;
                linecnt++;
                if (linecnt < start) continue;
                logger.logMessage("Line(" + linecnt + "):" + line, 0, true);
                
                for (int itry=0; itry < 4; itry++) {
                    try {
                        service.add(line);
                        break;
                        
                    } catch (TException.EXTERNAL_SERVICE_UNAVAILABLE esu) {
                        logger.logError("Unavailable(" + itry + "):"
                                + " - line=" + line
                                ,0);

                    } catch (TException.REQUESTED_ITEM_NOT_FOUND rinf) {
                        logger.logError("Item not found(" + itry + "):"
                                + " - line=" + line
                                ,0);
                        
                    } catch (TException.SQL_EXCEPTION sqle) {
                        try {
                            if (itry == 0) {
                                Thread.sleep(600000); // 10 minutes
                                continue;
                            }
                            Thread.sleep(7200000); // 2hours
                    
                        } catch (Exception ex) {
                            logger.logMessage("SLEEP Exception: " + ex, 0, true);
                        }
                    }
                    
                }
                if (linecnt >= last) break;
            }

        } catch (TException fe) {
            throw fe;

        } catch(Exception e)  {
            if (logger != null)
            {
                logger.logError(
                    "Main: Encountered exception:" + e, 0);
                logger.logError(
                        StringUtil.stackTrace(e), 10);
            }
            throw new TException(e);

        } finally {
            try {
                reader.close();
            } catch (Exception ex) { }
        }
    }

    protected void dumpException()
    {
        for (int i=0; i<exArr.size(); i++) {
            String dmp = exArr.get(i);
            System.out.println("exc(" + i + "):" + dmp);
        }
    }
    protected void log(String msg)
    {
        if (!DEBUG) return;
        System.out.println(msg);
    }
}
