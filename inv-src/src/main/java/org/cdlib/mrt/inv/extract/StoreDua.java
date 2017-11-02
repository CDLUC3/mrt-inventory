/*
Copyright (c) 2005-2010, Regents of the University of California
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
**********************************************************/
package org.cdlib.mrt.inv.extract;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.apache.tika.detect.Detector;
import org.apache.tika.mime.MediaType;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.HTTPUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;
import org.cdlib.mrt.core.Tika;
/**
 * Container class for Storage ERC content
 * @author dloy
 */
public class StoreDua
    implements StoreConstInt
{
    private static final String NAME = "StoreDua";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    protected LoggerInf logger = null;
    protected String urlS = null;
    protected Properties duaProp = new Properties();
    protected String fileID = null;
 
    public static StoreDua getStoreDua(
            String storageBase, 
            int node, 
            Identifier objectID,
            long versionID,
            String fileID,
            LoggerInf logger)
        throws TException
    {
        String urlS = null;
        try {
            if (StringUtil.isEmpty(storageBase)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreDua - storageBase missing");
            }
            if (node < 1) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreDua - node missing");
            }
            if (objectID == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreDua - objectID missing");
            }
            if (StringUtil.isEmpty(fileID)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreDua - fileID missing");
            }
            if (logger == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreDua - logger missing");
            }
            
            urlS = storageBase + "/" + "content/" + node + "/"
                + URLEncoder.encode(objectID.getValue(), "utf-8") 
                + "/" + versionID 
                + "/" + URLEncoder.encode(fileID, "utf-8")
                + "?fixity=no"
                ;
            StoreDua storeDua = new StoreDua(urlS, fileID, logger);
            return storeDua;
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
            
        }
    }   
    
    public StoreDua(LoggerInf logger)
        throws TException
    { 
        this.logger = logger;
    }
        
    public StoreDua(String urlS, String fileID, LoggerInf logger)
        throws TException
    {
        this.logger = logger;
        this.urlS = urlS;
        this.fileID = fileID;
        extract();
    }

    private void extract()
        throws TException
    {
        try {
            if (urlS == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "extract - urlS required");
            }
            setNewIngestProperties();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }

    public void setNewIngestProperties()
        throws TException
    {
        InputStream inStream = null;
        try {
            if (StringUtil.isEmpty(urlS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "extract - urlS required");
            }
            
            inStream = HTTPUtil.getObject(urlS, EXTRACT_TIMEOUT, 3);
            duaProp.load(inStream);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception ex) { }
            }
        }
    }

    public Properties getDuaProp() {
        return duaProp;
    }

}

