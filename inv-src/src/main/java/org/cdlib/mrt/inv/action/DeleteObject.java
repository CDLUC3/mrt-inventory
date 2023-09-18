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
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.logging.LogInvDelete;
import org.cdlib.mrt.inv.utility.DBDelete;
import org.cdlib.mrt.inv.service.InvDeleteState;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

/**
 * Delete inv object rows
 * @author dloy
 */
public class DeleteObject
        extends InvActionAbs
{

    protected static final String NAME = "DeleteObject";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;

    protected Identifier objectID = null;
    protected long objectseq = 0;
    protected InvDeleteState deleteState = null;
    
    public static DeleteObject getDeleteObject(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new DeleteObject(objectID, connection, logger);
    }
    
    protected DeleteObject(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        this.objectID = objectID;
    }

    public InvDeleteState process()
        throws TException
    {
        try {
            log("delete entered - objectID:" + objectID.getValue());
            long startMs = System.currentTimeMillis();
            int delCnt = deleteObject();
            long durationMs = System.currentTimeMillis() - startMs;
            deleteState = new InvDeleteState(objectID, delCnt);
            LogInvDelete logDeleteEntry = LogInvDelete.getLogInvDelete(
                "invdel",
                "InvDelete", 
                durationMs, 
                deleteState);
            logDeleteEntry.addEntry();
            return deleteState;

        } catch (Exception ex) {
            logger.logError("Delete fails:" + objectID, 0);
            throw new TException(ex);
        }

    }
    
    protected int deleteObject()
        throws TException
    {
        try {
            InvObject invObject = InvDBUtil.getObject(objectID, connection, logger);
            if (invObject == null) {
                log("object not found in db:" + objectID.getValue());
                return 0;
                
            } else {
                objectseq = invObject.getId();
            }
            log("objectseq=" + objectseq);
            DBDelete dbDelete = new DBDelete(objectseq, connection, logger);
            return dbDelete.delete();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            try {
                connection.close();
                
            } catch (Exception ex) { }
        }
    }
    
}

