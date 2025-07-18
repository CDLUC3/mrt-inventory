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
package org.cdlib.mrt.inv.content;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;


import org.json.JSONObject;

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;

/**
 * Container class for DC content
 * @author dloy
 */
public abstract class ContentAbs 
{
    private static final String NAME = "ContentAbs";
    private static final String MESSAGE = NAME + ": ";
    private static boolean DEBUG = false;
    
    public static final String OBJECTS = "inv_objects";
    public static final String VERSIONS = "inv_versions";
    public static final String OWNERS = "inv_owners";
    public static final String INGESTS = "inv_ingests";
    public static final String DUBLINKERNELS = "inv_dublinkernels";
    public static final String FILES = "inv_files";
    public static final String AUDITS = "inv_audits";
    public static final String LOCALS = "inv_localids";
    public static final String EMBARGOES = "inv_embargoes";
    public static final String STORAGE_MAINTS = "inv_storage_maints";
    public static final String METADATAS = "inv_metadatas";
    public static final String NODES = "inv_nodes";
    public static final String NODES_OBJECTS = "inv_nodes_inv_objects";
    public static final String DUAS = "inv_duas";
    public static final String COLLECTIONS = "inv_collections";
    public static final String COLLECTION_NODES = "inv_collections_inv_nodes";
    public static final String COLLECTIONS_OBJECTS = "inv_collections_inv_objects";
    public static final String GCOPIES = "inv_glacier_copies";
    public static final String GTRANS = "inv_glacier_transactions";
    protected LoggerInf logger = null;
    public enum RespStatus {ok, fail, commit, rollback, exists, processing, unknown }
    protected RespStatus respStatus = null;
    
    protected ContentAbs(LoggerInf logger) 
    {
        this.logger = logger;
    }
    
    public static long setNum(String val)
    {
        if (StringUtil.isAllBlank(val)) return 0;
        return Long.parseLong(val);
    }
    
    public static Long setNumLong(String val)
    {
        if (StringUtil.isAllBlank(val)) return null;
        return Long.parseLong(val);
    }
    
    public abstract void setProp(Properties prop)
        throws TException;

    public abstract Properties retrieveProp()
        throws TException;

    public abstract String getDBName()
        throws TException;

    public abstract long getId()
        throws TException;

    public abstract void setNewEntry(boolean newEntry)
        throws TException;

    public abstract boolean isNewEntry()
        throws TException;

    public LoggerInf getLogger() {
        return logger;
    }
    
    public String dump(String header) 
    {
        try {
            return PropertiesUtil.dumpProperties(header, retrieveProp());
        } catch (Exception ex) {
            return ex.toString();
        }
    }
    
    public JSONObject dumpJson(String header) 
    {
        JSONObject jsonhead = new JSONObject();
        try {
            jsonhead.put("header", header);
            jsonhead.put("dbName", getDBName());
            if (respStatus != null) {
                jsonhead.put("respStatus", respStatus.toString());
            }
            
            JSONObject json = new JSONObject();
            Properties prop = retrieveProp();
            Enumeration e = prop.propertyNames();
            String key = null;
            String value = null;

            while( e.hasMoreElements() )
            {
               key = (String)e.nextElement();
               value = prop.getProperty(key);
               if (value != null) {
                   json.put(key,value);
               }
            }
            jsonhead.put("content", json);
            return jsonhead;
            
        } catch (Exception ex) {
            return null;
        }
    }

    protected void log(String msg)
    {
        if (!DEBUG) return;
        System.out.println(MESSAGE + msg);
    }

    public RespStatus getRespStatus() {
        return respStatus;
    }

    public void setRespStatus(RespStatus respStatus) {
        this.respStatus = respStatus;
    }

    public void setRespStatus(String respStatusS) {
        if (StringUtil.isAllBlank(respStatusS)) {
            this.respStatus = RespStatus.unknown;
        } else {
            this.respStatus = RespStatus.valueOf(respStatusS);
        }
    }
    
}

