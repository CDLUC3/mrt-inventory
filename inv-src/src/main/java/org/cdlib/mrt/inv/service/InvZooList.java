
/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.service;

import java.io.File;
import java.util.Properties;


import org.cdlib.mrt.inv.action.AddZoo;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;



import org.cdlib.mrt.zoo.ZooQueue;
/**
 * Load manifest.
 * @author  dloy
 */

public class InvZooList
    extends ListAbs
{
    private static final String NAME = "InvZooList";
    private static final String MESSAGE = NAME + ": ";

    private static final String NL = System.getProperty("line.separator");
    private static final boolean DEBUG = true;
    protected ZooQueue queue = null;

    public InvZooList(
            Properties runProp,
            ZooQueue queue,
            LoggerInf logger)
        throws TException
    {
        super(runProp, null, logger);
        this.queue = queue;
        if (queue == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "ZooQueue missing");
        }
    }

    public InvZooList(
            int start,
            int last,
            File listFile,
            ZooQueue queue,
            LoggerInf logger)
        throws TException
    {
        super(start, last, listFile, null, logger);
        this.queue = queue;
        if (queue == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "ZooQueue missing");
        }
    }
    
    
    @Override
    protected void processLine(String line, int linecnt)
        throws TException
    {
        try {
            System.out.println("Add"
                    + " - node:" + queue.getZooNode()
                    + " - connect:" + queue.getZooManager().getQueueConnectionString()
                    + " - line:" + line
                    );
            if (StringUtil.isAllBlank(line)) return;
            for (int itry=0; itry < 3; itry++) {
                try {
                    Properties zooProp = new Properties();
                    zooProp.setProperty("manifestURL", line);
                    AddZoo addZoo = AddZoo.getAddZoo(queue, zooProp);
                    addZoo.process();
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
                        Thread.sleep(7200000); // 2hours

                    } catch (Exception ex) {
                        logger.logMessage("SLEEP Exception: " + ex, 0, true);
                    }
                }

            }

        } catch (TException fe) {
            throw fe;

        } catch(Exception e)  {
            if (logger != null)
            {
                logger.logError(
                    "Main: Encountered exception:" + e, 0);
                logger.logError(
                        StringUtil.stackTrace(e), 0);
            }
            throw new TException(e);

        }
    }
}
