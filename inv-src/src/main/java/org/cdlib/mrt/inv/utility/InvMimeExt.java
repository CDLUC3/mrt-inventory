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
package org.cdlib.mrt.inv.utility;

import java.io.File;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.db.DBUtil;
import org.cdlib.mrt.inv.content.ContentAbs;
import org.cdlib.mrt.inv.content.InvAudit;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvCollectionNode;
import org.cdlib.mrt.inv.content.InvCollectionObject;
import org.cdlib.mrt.inv.content.InvGCopy;
import org.cdlib.mrt.inv.content.InvDKVersion;
import org.cdlib.mrt.inv.content.InvDua;
import org.cdlib.mrt.inv.content.InvEmbargo;
import org.cdlib.mrt.inv.content.InvFile;
import org.cdlib.mrt.inv.content.InvIngest;
import org.cdlib.mrt.inv.content.InvAddLocalID;
import org.cdlib.mrt.inv.content.InvLocalID;
import org.cdlib.mrt.inv.content.InvMeta;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvNodeObject;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.inv.service.InvService;
import org.cdlib.mrt.inv.test.TestPrimaryLocal;
import static org.cdlib.mrt.inv.utility.InvDBUtil.DEBUG;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;


/**
 * This interface defines the functional API for a Curational Storage Service
 * @author dloy
 */
public class InvMimeExt
{

    protected static final String NAME = "InvMimeExt";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;


    /**
     * Main method
     */
    public static void main_save(String args[])
    {

        TFrame tFrame = null;
        DPRFileDB db = null;
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/Mysql.properties",
                "resources/Inv.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            db = new DPRFileDB(logger, invProp);
            Connection connect = db.getConnection(true);
            String fileExtension = "pdf";
            String mimeType = "application/pdf";
            List<KeyContent> keys = InvMimeExt.getKeys(mimeType, fileExtension, 10, connect, logger);
            for (KeyContent key : keys) {
                key.dump("test");
            }
            KeyContent minKey = getMinimum(keys);
            minKey.dump("minKey");
            System.out.println("key=" + getKey(minKey));
            String baseURLS = "http://uc3-mrtstore2-dev:35121/content";
            String curl = getCurl(baseURLS, minKey);
            System.out.println("curl=" + curl);

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        } finally {
            try {
                db.shutDown();
            } catch (Exception ex) {
                System.out.println("db Exception:" + ex);
            }
        }
    }
    

    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        DPRFileDB db = null;
        try {
            String propertyList[] = {
                "resources/InvLogger.properties",
                "resources/Mysql.properties",
                "resources/Inv.properties"};
            tFrame = new TFrame(propertyList, "InvLoad");
            Properties invProp  = tFrame.getProperties();
            LoggerInf logger = new TFileLogger("testFormatter", 10, 10);
            db = new DPRFileDB(logger, invProp);
            Connection connect = db.getConnection(true);
            String fileExtension = "pdf";
            String mimeType = "application/pdf";
            String baseURLS = "http://uc3-mrtstore2-dev:35121/content";
            String curl  = InvMimeExt.extractCurlMin(baseURLS, mimeType, fileExtension, 10, connect, logger);
            System.out.println("curl=" + curl);

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        } finally {
            try {
                db.shutDown();
            } catch (Exception ex) {
                System.out.println("db Exception:" + ex);
            }
        }
    }
    
    public static String extractCurlMin(
            String baseURLS,
            String mimeType,
            String fileExtension,
            int maxReturn,
            Connection connect,
            LoggerInf logger)
        throws TException
    {
        List<KeyContent> keys = InvMimeExt.getKeys(mimeType, fileExtension, 10, connect, logger);
        if (keys == null) return null;
        KeyContent minKey = getMinimum(keys);
        if (minKey == null) return null;
        return getCurl(baseURLS, minKey);
    }
    
    public static List<KeyContent> getKeys(
            String mimeType,
            String fileExtension,
            int maxReturn,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodes entered");
        String sql = "select inv_nodes.number as node_number, inv_versions.ark, " +
            "inv_versions.number as version_number,inv_files.pathname,  billable_size " +
            "from inv_versions, inv_files, inv_nodes_inv_objects, inv_nodes " +
            "where inv_files.inv_version_id=inv_versions.id " +
            "and inv_nodes_inv_objects.inv_object_id=inv_files.inv_object_id " +
            "and inv_nodes_inv_objects.role='primary' " +
            "and inv_nodes.id = inv_nodes_inv_objects.inv_node_id " +
            "and inv_files.billable_size > 0 " +
            "and inv_files.mime_type='" + mimeType + "' " +
            "and inv_files.pathname like '%." + fileExtension + "' " +
            "limit " + maxReturn + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<KeyContent> list = new ArrayList();
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            list.add(new KeyContent(mimeType, fileExtension, prop));
        }
        return list;
    }
    
    public static KeyContent getMinimum(List<KeyContent> keyList)
    {
        if ((keyList == null) || (keyList.size() == 0)) {
            return null;
        }
        KeyContent minKey = keyList.get(0);
        for (KeyContent key : keyList) {
            if (minKey.size > key.size) {
                minKey = key;
            }
        }
        return minKey;
    }
    
    public static String getKey(KeyContent entry)
    {
        if (entry == null) {
            return null;
        }
        return entry.arkS + "|" + entry.version + "|" + entry.pathname;
    }
    
    public static String getFileName(KeyContent entry)
    {
        if (entry == null) {
            return null;
        }
        String normMime = entry.mime.replace('/', '_');
        String fileName = normMime + "." + entry.extension;
        return fileName;
    }
    
    public static String getCurl(String baseURL, KeyContent entry)
    {
        if (entry == null) {
            return null;
        }
        String arkE = null;
        String pathE = null;
        try {
            arkE = URLEncoder.encode(entry.arkS, "utf-8");
            pathE = URLEncoder.encode(entry.pathname, "utf-8");
            String fileName = getFileName(entry);
            String curl = "curl -X GET '" 
                    + baseURL + "/" + entry.node + "/" + arkE + "/" + entry.version+ "/" + pathE + "' > " + fileName;
            return curl;
            
        } catch (Exception ex) {
            return null;
        }
    }
    
    protected static void log(String msg)
    {
        if (!DEBUG) return;
        System.out.println(MESSAGE + msg);
    }
    
    public static class KeyContent
    {
        public long node = 0;
        public String key = null;
        public String arkS = null;
        public int version = 0;
        public String pathname = null;
        public long size = 0;
        public String mime = null;
        public String extension = null;
        
        public KeyContent(String mime, String extension, Properties prop)
        {
            this.mime = mime;
            this.extension = extension;
            String nodeS = prop.getProperty("node_number");
            node = Long.parseLong(nodeS);
            arkS = prop.getProperty("ark");
            String versionS = prop.getProperty("version_number");
            version = Integer.parseInt(versionS);
            pathname = prop.getProperty("pathname");
            String sizeS = prop.getProperty("billable_size");
            if (sizeS != null) {
                size = Long.parseLong(sizeS);
            }
        }
        
        public void dump(String header)
        {
            System.out.println("KeyContent: " + header + "\n"
                    + " - mime=" + mime + "\n"
                    + " - extension=" + extension + "\n"
                    + " - node=" + node + "\n"
                    + " - arkS=" + arkS + "\n"
                    + " - pathname=" + pathname + "\n"
                    + " - size=" + size + "\n"
            );
        }
    }
}

