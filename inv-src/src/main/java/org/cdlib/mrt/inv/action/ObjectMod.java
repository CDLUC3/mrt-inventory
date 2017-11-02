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

import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.ComponentContent;
import org.cdlib.mrt.cloud.MatchMap;
import org.cdlib.mrt.cloud.VersionMap;
import org.cdlib.mrt.inv.content.ContentAbs;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvCollectionObject;
import org.cdlib.mrt.inv.content.InvDK;
import org.cdlib.mrt.inv.content.InvDKVersion;
import org.cdlib.mrt.inv.content.InvDua;
import org.cdlib.mrt.inv.content.InvFile;
import org.cdlib.mrt.inv.content.InvAudit;
import org.cdlib.mrt.inv.content.InvIngest;
import org.cdlib.mrt.inv.content.InvMeta;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvNodeObject;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.inv.extract.StoreCollections;
import org.cdlib.mrt.inv.extract.StoreDua;
import org.cdlib.mrt.inv.extract.StoreDuaTemplate;
import org.cdlib.mrt.inv.extract.StoreERC;
import org.cdlib.mrt.inv.extract.StoreIngest;
import org.cdlib.mrt.inv.extract.StoreMeta;
import org.cdlib.mrt.inv.extract.StoreMom;
import org.cdlib.mrt.inv.extract.StoreOwner;
import org.cdlib.mrt.inv.extract.StoreState;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.DBDelete;
import org.cdlib.mrt.inv.extract.StoreFile;
import org.cdlib.mrt.inv.service.InvProcessState;
import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.core.Tika;
import static org.cdlib.mrt.inv.action.InvActionAbs.getVersionMap;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;

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
            ex.printStackTrace();
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
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
}

