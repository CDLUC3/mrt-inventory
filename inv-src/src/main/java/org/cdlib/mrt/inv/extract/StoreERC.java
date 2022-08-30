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
import java.util.List;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;
/**
 * Container class for Storage ERC content
 * @author dloy
 */
public class StoreERC
{
    private static final String NAME = "StoreERC";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    protected String erc = null;
    protected String who = null;
    protected String what = null;
    protected String when = null;
    protected String where = null;
    protected LoggerInf logger = null;
    protected LinkedHashList<String, String> list = null;
 
    public static StoreERC getStoreERC(
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
                + URLEncoder.encode("system/mrt-erc.txt", "utf-8")
                + "?fixity=no"
                ;
            if (DEBUG) System.out.println("getStoreERC:" + urlS);
            String ercS = StoreExtract.getString(urlS, logger, 3);
            if (DEBUG) System.out.println("ERC:" + ercS);
            StoreERC storeERC = new StoreERC(ercS, logger);
            return storeERC;
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
            
        }
    }   
 
    public static StoreERC getStoreERC(
            String storageBase, 
            int node, 
            Identifier objectID, 
            long versionID,
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
                + "/" + versionID + "/" 
                + URLEncoder.encode("system/mrt-erc.txt", "utf-8")
                + "?fixity=no"
                ;
            if (DEBUG) System.out.println("getStoreERC:" + urlS);
            String ercS = StoreExtract.getString(urlS, logger, 3);
            if (DEBUG) System.out.println("ERC:" + ercS);
            StoreERC storeERC = new StoreERC(ercS, logger);
            return storeERC;
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
            
        }
    }   
    
    public StoreERC(LoggerInf logger)
        throws TException
    { 
        this.logger = logger;
    }
        
    public StoreERC(String erc, LoggerInf logger)
        throws TException
    {
        this.logger = logger;
        this.erc = erc;
        build();
    }

    public void build()
        throws TException
    {
        try {
            if (StringUtil.isEmpty(erc)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - erc required");
            }
            list = extractERC(erc);
            if (list == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - erc empty");
                
            }
            set4W();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    

    public static LinkedHashList<String, String> extractERC(String erc)
        throws TException
    {
        LinkedHashList<String, String> list = new LinkedHashList<String, String>();
        try {
            if (StringUtil.isEmpty(erc)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "build - erc required");
            }
            String lines[] = erc.split("\\r?\\n");
            if (DEBUG) System.out.println("lines len=" + lines.length);
            for (String line : lines) {
                String [] parts = line.split("\\s*\\:\\s", 2);
                if (parts.length != 2) continue;
                list.put(parts[0], parts[1]);
            }
            if (list.size() > 0) return list;
            return null;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected void set4W()
        throws TException
    {
        try {
            who = getW("who");
            what = getW("what");
            when = getW("when");
            where = getW("where");
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected String getW(String w)
        throws TException
    {
        try {
            List<String> whoList = list.get(w);
            if (whoList == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "set4W - " + w + " required");
            }
            StringBuffer buf = new StringBuffer();
            for (String entry : whoList) {
                if (buf.length() > 0) buf.append(" ; ");
                buf.append(entry);
                if (DEBUG) System.out.println("Append - " + w + ":" + entry);
            }
            return buf.toString();
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public List<String> getWList(String w)
        throws TException
    {
        try {
            return list.get(w);
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }


    public String getErc() {
        return erc;
    }

    public void setErc(String erc) {
        this.erc = erc;
    }

    public LinkedHashList<String, String> getList() {
        return list;
    }

    public void setList(LinkedHashList<String, String> list) {
        this.list = list;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = who;
    }
    
    public String dump(String header) {
        StringBuffer buf = new StringBuffer();
        buf.append(header + "\n"
                + "who:" + who + "\n"
                + "what:" + what + "\n"
                + "when:" + when + "\n"
                + "where:" + where + "\n"
                );
        return buf.toString();
    }
    

}

