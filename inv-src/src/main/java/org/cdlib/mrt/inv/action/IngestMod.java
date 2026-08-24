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
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvIngest;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.inv.extract.StoreIngest;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class IngestMod
        extends InvActionAbs
{

    protected static final String NAME = "IngestMod";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
    
    protected DBAdd dbAdd = null;
    protected int nodeNumber = 0;
    protected Identifier objectID = null;
    protected long versionseq = 0;
    protected int versionNumber = 0;
    protected String ingestURL = null;
    protected String storageBase = null;
    protected InvVersion invVersion = null;
    
    public static IngestMod getIngestMod(
            long versionseq,
            String ingestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new IngestMod(versionseq, ingestURL, connection, logger);
    }
    
    protected IngestMod(
            long versionseq,
            String ingestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        try {
            this.versionseq = versionseq;
            this.ingestURL = ingestURL;
            invVersion = InvDBUtil.getVersionFromId(versionseq, connection, logger);
            extractParts();
            dbAdd = new DBAdd(connection, logger);
            String msg = "IngestMod URL:"
                        + " - storageBase=" + storageBase
                        + " - nodeNumber=" + nodeNumber
                        + " - objectID=" + objectID.getValue()
                    ;
            if (DEBUG) {
                System.out.println(msg);
            }
            logger.logMessage(msg, 2, true);
        
        } catch (Exception ex) {
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
    
    
    public void extractParts()
        throws TException
    {
        URL url = null;
        try {
            String urlS = ingestURL;
            if (StringUtil.isAllBlank(urlS)) {
                logger.logError("extractParts - storage_url missing", 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processItem - storage_url missing");
            }
            try {
                url = new URL(urlS);
                log4j.debug("manifestURL=" + url);
            } catch (Exception ex) {
                String msg = "extractParts - URL invalid:" + urlS;
                logger.logError(msg, 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            }
            String urlPath = url.getPath();
            this.storageBase = 
                    url.getProtocol() 
                    + "://" + url.getHost()
                    + ":" + url.getPort()
                    ;
            
            log4j.debug("storageBase=" + storageBase);
            String parts[] = urlPath.split("\\/");
            log4j.debug("parts[] length=" + parts.length);        
            if (parts.length < 4) {
                String msg = "processItem - URL format invalid:" + urlS;
                logger.logError(msg, 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            }
            for (int i=0; i < parts.length; i++) {
                if (parts[i].length() == 0) continue;
                log4j.debug("part[" + i + "]:" + parts[i]);
                        
                if (parts[i].equals("content")) {
                    extractManifestParts(parts, i);
                    return;
                }
            }
            String msg = "processItem - URL format invalid:" + urlS;
            logger.logError(msg, 0);
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    private void extractManifestParts(String [] parts, int manifestInx)
        throws TException
    {
        
        try {
            String nodeS = parts[manifestInx + 1];
            String objectIDSE = parts[manifestInx + 2];
            String versionS = parts[manifestInx + 3];
            this.nodeNumber = Integer.parseInt(nodeS);
            String objectIDS = URLDecoder.decode(objectIDSE, "utf-8");
            this.objectID = new Identifier(objectIDS);
            this.versionNumber = Integer.parseInt(versionS);
            log4j.debug("extractManifestParts(:"
                    +  " - nodeNumber:" + this.nodeNumber
                    +  " - objectID:" + this.objectID.getValue()
                    +  " - versionNumber:" + this.versionNumber
            );
            
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public void process()
        throws TException
    {
        try {
            setInvIngests(ingestURL);
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
    
    public void setInvIngests(String storageURL)
        throws TException
    {
        try {
            log("setInvIngests entered:"
                    + " - versionseq=" + versionseq
                    );
            if (versionseq == 0) {
                throw new TException.INVALID_OR_MISSING_PARM("setInvIngests-missing versionseq");
            }
            
            InvIngest invIngestsOld = InvDBUtil.getIngest(versionseq, connection, logger);
            if (invIngestsOld == null) {
                throw new TException.REQUEST_INVALID("inv_ingests not found:"
                    + " - versionseq=" + versionseq
                    );
            }
            log4j.debug(PropertiesUtil.dumpProperties("invIngestOld", invIngestsOld.retrieveProp()));
            StoreIngest storeIngest = StoreIngest.getStoreIngest(
                storageBase, 
                nodeNumber, 
                objectID,
                versionNumber,
                "system/mrt-ingest.txt",
                logger);
            InvIngest invIngests = new InvIngest(logger);
            invIngests.setIngestProp(storeIngest.getIngestProp(), storageURL);
            invIngests.setId(invIngestsOld.getId());
            invIngests.setObjectID(invIngestsOld.getObjectID());
            invIngests.setVersionID(invIngestsOld.getVersionID());
            log4j.debug(PropertiesUtil.dumpProperties("invIngests", invIngests.retrieveProp()));
            long id = dbAdd.update(invIngests);
            
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            log4j.error("Exception:" + ex, ex);
            throw new TException(ex);
        }
    }
    
}

