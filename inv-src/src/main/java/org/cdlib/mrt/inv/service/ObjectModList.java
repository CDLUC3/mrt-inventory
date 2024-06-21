
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
import java.sql.Connection;
import java.util.Properties;
import java.util.Vector;

import org.cdlib.mrt.inv.action.IngestMod;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;


import org.cdlib.mrt.core.Identifier;
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
//import org.cdlib.mrt.inv.zoo.ItemRun;
import org.cdlib.mrt.inv.action.ObjectMod;
/**
 * Load manifest.
 * @author  dloy
 */

public class ObjectModList
{
    private static final String NAME = "ObjectModList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;
    
    protected LoggerInf logger = null;
    protected DPRFileDB db = null;
    //protected FixityItemDB db = null;
    protected ComponentContent content = null;
    //protected Manifest manifest = null;
    protected File listFile = null;
    protected BufferedReader reader = null;
    protected Vector <String> exArr = new Vector <String> (100);
    protected int maxExceptions = 100;
    //protected int nodeID = 0;
    protected int start = 0;
    protected int last = 1000000;
    protected String startAfter = null;
    protected Properties runProp = null;

    public ObjectModList(
            Properties startupProp,
            Properties runProp,
            LoggerInf logger)
        throws TException
    {
        try {
            log(PropertiesUtil.dumpProperties(MESSAGE, runProp));
            
            db = new DPRFileDB(logger, startupProp);
            start = getInt(runProp, "start", 0);
            last = getInt(runProp, "last", 1000000);
            String listFileS = get(runProp, "listFile");
            listFile = new File(listFileS);
            if (!listFile.exists()) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "file does not exist:" + listFile.getCanonicalFile()
                        );
            }
            startAfter = runProp.getProperty("startAfter");

            this.logger = logger;
            validate();


        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected static int getInt(Properties prop, String key, int defaultVal)
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
    
    public String dump(String header) 
        throws TException
    {
        StringBuffer buf = new StringBuffer();
        buf.append("***" + header + "***\n"
                + " - start=" + start + "\n"
                + " - last=" + last + "\n"
                + " - listFile=" + listFile + "\n"
                + " - startAfter=" + startAfter + "\n"
                );
        return buf.toString();
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
                if (line.length() == 0) break;
                if (line.startsWith("#")) continue;
                linecnt++;
                if (linecnt < start) continue;
                if (startAfter != null) {
                    if (line.indexOf(startAfter) >= 0) {
                        startAfter = null;
                        System.out.println("***startAfter found at "  + linecnt);
                    }
                    continue;
                }
                logger.logMessage("Line(" + linecnt + "):" + line, 0, true);
                
                for (int itry=0; itry < 4; itry++) {
                    try {
                        process(line);
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
    
    protected void process(String line)
        throws TException
    {
        Connection connect = null;
        try {
            connect = db.getConnection(false);
            long objectseq = Long.parseLong(line);
            if (DEBUG) System.out.println("IngestModList:"
                    + " - objectseq=" + objectseq
            );
            ObjectMod objectMod = ObjectMod.getObjectMod(
                objectseq,
                connect,
                logger);
            objectMod.process();
            connect.close();
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        DPRFileDB db = null;
        Properties runProp = null;
        
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/ObjectModList.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties startupProp  = tFrame.getProperties();
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties(MESSAGE + "main", startupProp));
            LoggerInf logger = tFrame.getLogger();
            String runPropS  = null;
            String form = null;
            
            if (args.length == 0) {
                    runPropS  = startupProp.getProperty("runProps");
                    if (!StringUtil.isAllBlank(runPropS)) form = "runProps";
                    
            } else {
                runPropS = args[0];
                form = "arg[0]";
            }
            if (StringUtil.isEmpty(runPropS)) {
                String resource = tFrame.getProperty("resource");
                if (StringUtil.isAllBlank(resource)) {
                    throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "No runtime parm found");
                }
                runProp = PropertiesUtil.loadProperties(resource);
                form = "resource";
                
            } else {
                File runPropF = new File(runPropS);
                if (!runPropF.exists()) {
                    throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                            + "runPropF missing:" + runPropF.getCanonicalPath());
                }
                runProp = PropertiesUtil.loadFileProperties(runPropF);
            }
            if (runProp == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "No runtime found");
            }
            runProp.setProperty("form", form);
            System.out.println(PropertiesUtil.dumpProperties("runProp", runProp));
            ObjectModList runList = new ObjectModList(startupProp, runProp, logger);
            if (DEBUG) System.out.println(runList.dump(MESSAGE));
            runList.run();

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }
}
