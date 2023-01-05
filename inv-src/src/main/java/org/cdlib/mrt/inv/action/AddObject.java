/******************************************************************************
Copyright (c) 2005-2012, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 *
- Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
- Neither the name of the University of California nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
package org.cdlib.mrt.inv.action;

import java.sql.Connection;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class AddObject
        extends InvActionAbs
        implements Runnable
{

    protected static final String NAME = "AddObject";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    
    SaveObject saveObject = null;
 
    
    public static AddObject getAddObject(
            String storageBase,
            int node,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AddObject(storageBase, node, objectID, connection, logger);
    }
    
    protected AddObject(
            String storageBase,
            int node,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        this.saveObject = SaveObject.getSaveObject(
            storageBase,
            node,
            objectID,
            connection,
            logger);
    } 
    
    public static AddObject getAddObject(
            String manifestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AddObject(manifestURL, connection, logger);
    }
    
    protected AddObject(
            String manifestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        this.saveObject = SaveObject.getSaveObject(
            manifestURL,
            connection,
            logger);
    }

    public void run()
    {

    }

    public void process()
        throws TException
    {
        TException processException = null;
        int maxRetries = 3;
        for (int i=1; i<=maxRetries; i++) {
            try {
                processException = null;
                this.saveObject.process();
                if (i>1) logger.logMessage("Deadlock Retry[" + i + "] OK", 2);
                return;
                
            } catch (TException ex) {
                processException = ex;
                if (i==maxRetries) break;
                String exl = ex.toString().toLowerCase();
                logger.logError("exl:" + exl, 15);
                if (exl.contains("deadlock found")) {
                    int sleepTime = i * 120000;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception ext) { }
                    logger.logMessage(MESSAGE +"Deadlock Retry[" + i + "," + sleepTime + "]:" + ex, 2);
                    continue;
                }
                throw ex;
            } catch (Exception ex) {
                throw new TException(ex);
            }
        }
        throw processException;
        
    }
    
    public boolean isCommit()
    {
        return true;
    }
}

