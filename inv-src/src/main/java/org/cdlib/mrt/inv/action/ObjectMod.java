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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class ObjectMod
        extends InvActionAbs
{

    protected static final String NAME = "ObjectMod";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
    protected static final Logger log4j = LogManager.getLogger(); 
    
    protected DBAdd dbAdd = null;
    protected int nodeNumber = 0;
    protected Identifier objectID = null;
    protected long objectseq = 0;
    protected int versionNumber = 0;
    protected String ingestURL = null;
    protected String storageBase = null;
    protected InvObject invObject = null;
    
    public static ObjectMod getObjectMod(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new ObjectMod(objectseq, connection, logger);
    }
    
    protected ObjectMod(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        try {
            this.objectseq = objectseq;
            dbAdd = new DBAdd(connection, logger);
            String msg = "IngestMod URL:"
                        + " - objectseq=" + objectseq
                    ;
            if (DEBUG) {
                System.out.println(msg);
            }
            logger.logMessage(msg, 2, true);
        
        } catch (Exception ex) {
            log4j.error("Exception:" + ex, ex);
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ex2) { }
            
            if (ex instanceof TException) {
                throw (TException) ex;
            }
            else throw new TException(ex);
        }
    }

    public void process()
        throws TException
    {
        try {
            setObject(objectseq);
            connection.commit();

        } catch (Exception ex) {
            String msg = MESSAGE + "Exception for entry id=" + objectID.getValue()
                    + " - Exception:" + ex
                    ;
            System.out.println("EXception:" + msg);
            logger.logError(msg, 2);
            logger.logError(StringUtil.stackTrace(ex),3);
            try {
                connection.rollback();
            } catch (Exception cex) {
                System.out.println("WARNING: rollback Exception:" + cex);
            }
            if (ex instanceof TException) {
                throw (TException) ex;
            } else {
                throw new TException (ex);
            }

        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }

    }
    
    public void setObject(long objectseq)
        throws TException
    {
        try {
            log("setObject entered:"
                    + " - objectseq=" + objectseq
                    );
            
        
            if (objectseq == 0) {
                throw new TException.INVALID_OR_MISSING_PARM("setObject-missing versionseq");
            }
            invObject = InvDBUtil.getObject(objectseq, connection, logger);
            long id = dbAdd.update(invObject);
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            log4j.error("Exception:" + ex, ex);
            throw new TException(ex);
        }
    }
    
}

