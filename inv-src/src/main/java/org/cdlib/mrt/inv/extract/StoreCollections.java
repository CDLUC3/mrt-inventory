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
import java.util.ArrayList;
import java.util.List;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;

import org.cdlib.mrt.inv.content.ContentAbs;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for Storage ERC content
 * @author dloy
 */
public class StoreCollections
{
    private static final String NAME = "StoreCollection";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    protected LoggerInf logger = null;
    protected String collectionsS = null;
    protected ArrayList<String> list = null;
    

    
    public static StoreCollections getStoreCollections(
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
                        + "getStoreCollection - storageBase missing");
            }
            if (node < 1) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreCollection - node missing");
            }
            if (objectID == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreCollection - objectID missing");
            }
            if (logger == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                        + "getStoreCollection - logger missing");
            }
            
            urlS = storageBase + "/" + "content/" + node + "/"
                + URLEncoder.encode(objectID.getValue(), "utf-8") 
                + "/0/" 
                + URLEncoder.encode("system/mrt-membership.txt", "utf-8")
                + "?fixity=no"
                ;
            if (DEBUG) System.out.println("getStoreCollection:" + urlS);
            tempFile = FileUtil.url2TempFile(logger, urlS);
            String collectionsS = FileUtil.file2String(tempFile);
            if (DEBUG) System.out.println("collectionsS:" + collectionsS);
            StoreCollections storeCollection = new StoreCollections(collectionsS, logger);
            return storeCollection;
            
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
    
    public StoreCollections(LoggerInf logger)
        throws TException
    { 
        this.logger = logger;
    }
        
    public StoreCollections(String collectionsS, LoggerInf logger)
        throws TException
    {
        this.logger = logger;
        this.collectionsS = collectionsS;
        build();
    }

    public void build()
        throws TException
    {
        try {
            if (StringUtil.isEmpty(collectionsS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - collectionsS required");
            }
            list = extractCollections(collectionsS);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }


    public static ArrayList extractCollections(String erc)
        throws TException
    {
        ArrayList<String> list = new ArrayList<String>();
        try {
            if (StringUtil.isEmpty(erc)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - erc required");
            }
            String lines[] = erc.split("\\r?\\n");
            if (DEBUG) System.out.println("lines len=" + lines.length);
            for (String line : lines) {
                list.add(line);
            }
            if (list.size() > 0) return list;
            return null;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public ArrayList<String> getList() {
        return list;
    }
    
    public int size()
    {
        if (list == null) return 0;
        return list.size();
    }
    
    public String get(int inx)
    {
        if (inx >= list.size()) return null;
        if (inx < 0) return null;
        return list.get(inx);
    }
    

}

