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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.cdlib.mrt.cloud.ManifestSAX;

import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.cloud.ManifestXML;
import org.cdlib.mrt.cloud.VersionMap;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Abstract for performing a inv
 * @author dloy
 */
public class InvActionAbs
{

    protected static final String NAME = "InvActionAbs";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final String  STATUS_PROCESSING = "processing";

    protected LoggerInf logger = null;
    protected Connection connection = null;
    protected Exception exception = null;


    protected InvActionAbs(
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        this.logger = logger;
        this.connection = connection;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public LoggerInf getLogger() {
        return logger;
    }

    protected void log(String msg)
    {
        if (!DEBUG) return;
        System.out.println(MESSAGE + msg);
    }
    

    public static VersionMap getVersionMap(
            String storageBase, 
            int node, 
            Identifier objectID, 
            LoggerInf logger)
        throws TException
    {
        File tempFile = null;
        String urlS = null;
        try {
            if (StringUtil.isEmpty(storageBase)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getVersionMap - storageBase missing");
            }
            if (node < 1) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getVersionMap - node missing");
            }
            if (objectID == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getVersionMap - objectID missing");
            }
            if (logger == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getVersionMap - logger missing");
            }
            urlS = storageBase + "/" + "manifest/" + node + "/"
                + URLEncoder.encode(objectID.getValue(), "utf-8");
            if (DEBUG) System.out.println("getVersionMap:" + urlS);
            tempFile = FileUtil.url2TempFile(logger, urlS);
            InputStream xmlStream = new FileInputStream(tempFile);
            return ManifestSAX.buildMap(xmlStream, logger);
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
            
        } finally {
            if (tempFile != null) {
                try {
                    tempFile.delete();
                } catch (Exception ex) { }
            }
        }
    }
    

}

