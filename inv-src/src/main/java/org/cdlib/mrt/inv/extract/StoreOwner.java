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
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;
/**
 * Container class for Storage ERC content
 * @author dloy
 */
public class StoreOwner
{
    private static final String NAME = "StoreOwner";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    protected Identifier ownerObjectID = null;
    protected LoggerInf logger = null;
    protected String fileS = null;
    
    
    public static StoreOwner getStoreOwner(
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
            
            urlS = storageBase + "/" + "content/" + node + "/"
                + URLEncoder.encode(objectID.getValue(), "utf-8") 
                + "/0/" 
                + URLEncoder.encode("system/mrt-owner.txt", "utf-8")
                + "?fixity=no"
                ;
            if (DEBUG) System.out.println("getStoreERC:" + urlS);
            String ownerS = StoreExtract.getString(urlS, logger, 3);
            if (DEBUG) System.out.println("ownerS:" + ownerS);
            StoreOwner storeOwner = new StoreOwner(ownerS, logger);
            return storeOwner;
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
            
        }
    }
    
    public StoreOwner(String fileS, LoggerInf logger)
        throws TException
    { 
        this.fileS = fileS;
        this.logger = logger;
        build();
    }


    public void build()
        throws TException
    {
        try {
            if (StringUtil.isEmpty(fileS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - ownerS required");
            }
            String ownerS = extractOwner(fileS);
            setOwnerObjectID(ownerS);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    public Identifier getOwnerObjectID() {
        return ownerObjectID;
    }

    public void setOwnerObjectID(Identifier ownerObjectID) {
        this.ownerObjectID = ownerObjectID;
    }

    public void setOwnerObjectID(String ownerObjectIDS)
        throws TException
    {
        try {
            this.ownerObjectID = new Identifier(ownerObjectIDS);
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public String dump(String header) {
        StringBuffer buf = new StringBuffer();
        buf.append(header + "\n"
                + "ownerObjectID:" + ownerObjectID + "\n"
                );
        return buf.toString();
    }
    
    public static String extractOwner(String ownerS)
        throws TException
    {
        try {
            if (StringUtil.isEmpty(ownerS)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - erc required");
            }
            String lines[] = ownerS.split("\\r?\\n");
            return lines[0];
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
}

