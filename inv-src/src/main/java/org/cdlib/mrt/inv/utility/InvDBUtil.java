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
import org.cdlib.mrt.inv.content.InvStorageMaint;
import org.cdlib.mrt.inv.content.InvStorageScan;
import org.cdlib.mrt.inv.content.InvVersion;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;


/**
 * This interface defines the functional API for a Curational Storage Service
 * @author dloy
 */
public class InvDBUtil
{

    protected static final String NAME = "InvDBUtil";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;

    protected static final String NL = System.getProperty("line.separator");

    
    protected InvDBUtil() {}

    public static InvObject getObject(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getObject entered");
        String sql = "select * from " + ContentAbs.OBJECTS + " where ark = \'" + objectID + "\';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvObject(propArray[0], logger);
    }

    public static InvObject getObject(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getObject entered");
        String sql = "select * from " + ContentAbs.OBJECTS + " where id=" + objectseq + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvObject(propArray[0], logger);
    }
    
    public static InvCollection getCollection(
            String collectionS,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getCollection entered");
        String sql = "select * from " + ContentAbs.COLLECTIONS + " where ark = \'" + collectionS + "\';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvCollection(propArray[0], logger);
    }
    
    public static InvEmbargo getEmbargo(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getEmbargo entered");
        String sql = "select * from " + ContentAbs.EMBARGOES + " where inv_object_id=" + objectseq + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvEmbargo(propArray[0], logger);
    }

    public static boolean deleteEmbargo(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("deleteEmbargo entered");
        String sql = "delete from inv_embargoes "
                + "where inv_object_id=" + objectseq;
        log("sql:" + sql);
        boolean works = DBUtil.exec(connection, sql, logger);
        return works;
    }

    public static boolean updateEmbargo(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("deleteEmbargo entered");
        String sql = "delete from inv_embargoes "
                + "where inv_object_id=" + objectseq;
        log("sql:" + sql);
        boolean works = DBUtil.exec(connection, sql, logger);
        return works;
    }

    public static InvStorageMaint getStorageMaint(
            long nodeid,
            String key,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String keymd5 = InvStorageMaint.buildMd5(key, logger);
        String sql = "select * from " + ContentAbs.STORAGE_MAINTS 
                + " where keymd5 = '" + keymd5 + "'"
                + " and inv_node_id=" + nodeid
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvStorageMaint(propArray[0], logger);
    }

    public static InvStorageMaint getStorageMaintAdmin(
            long nodeNum,
            
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "select sm.*" 
                + " FROM inv_nodes n,"
                + " inv_storage_maints sm"
                + " WHERE n.number=" + nodeNum
                + " AND sm.inv_node_id=n.id"
                + " AND sm.maint_type='admin'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvStorageMaint(propArray[0], logger);
    }

    public static InvStorageMaint getStorageMaintsFromId(
            long id,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "select *" 
                + " FROM inv_storage_maints "
                + " WHERE id=" + id
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        InvStorageMaint storageMaint = new InvStorageMaint(propArray[0], logger);
        return storageMaint;
    }

    public static InvStorageMaint[] getStorageMaints(
            long storageScanId,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "select *" 
                + " FROM inv_storage_maints "
                + " WHERE inv_storage_scan_id=" + storageScanId
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        InvStorageMaint[] storageMaints = new InvStorageMaint[propArray.length];
        for (int i=0; i < propArray.length; i++) {
            storageMaints[i] = new InvStorageMaint(propArray[i], logger);
        }
        return storageMaints;
    }

    public static InvStorageMaint[] getDeleteStorageMaints(
            long lastDelete,
            int maxout,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "select *" 
                + " FROM inv_storage_maints "
                + " WHERE maint_status='delete'" 
                + " AND id > " + lastDelete 
                + " LIMIT " + maxout
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        InvStorageMaint[] storageMaints = new InvStorageMaint[propArray.length];
        for (int i=0; i < propArray.length; i++) {
            storageMaints[i] = new InvStorageMaint(propArray[i], logger);
        }
        return storageMaints;
    }

    public static InvStorageScan getStorageScan(
            long storageScanId,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "select *" 
                + " FROM inv_storage_scans "
                + " WHERE id=" + storageScanId
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvStorageScan(propArray[0], logger);
    }

    public static InvStorageScan getStorageScanStarted(
            long nodeNum,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "SELECT ss.* "
               + " FROM inv_storage_scans ss,"
               + " inv_nodes n"
               + " WHERE n.number=" + nodeNum
               + " AND ss.inv_node_id=n.id"
               + " AND ss.scan_status='started'"
               + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvStorageScan(propArray[0], logger);
    }

    public static ArrayList<InvStorageScan> getStorageScanStatus(
            long nodeNum,
            String status,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
        String sql = "SELECT ss.* "
               + " FROM inv_storage_scans ss,"
               + " inv_nodes n"
               + " WHERE n.number=" + nodeNum
               + " AND ss.inv_node_id=n.id"
               + " AND ss.scan_status='" + status + "'"
               + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvStorageScan> list = new ArrayList<>();
        for (Properties prop : propArray) {
            InvStorageScan scan = new InvStorageScan(prop, logger);
            list.add(scan);
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return list;
    }

    public static ArrayList<InvStorageScan> getDeleteStorageScan(
            String status,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        
        log("getStorageMaint entered");
        String sql = "SELECT * "
               + " FROM inv_storage_scans "
               + " WHERE scan_type='delete' "
               + " AND scan_status='" + status + "' "
               + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvStorageScan> list = new ArrayList<>();
        for (Properties prop : propArray) {
            InvStorageScan scan = new InvStorageScan(prop, logger);
            list.add(scan);
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return list;
    }

    public static InvStorageScan getStorageScanId(
            int scanId,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageMaint entered");
         String sql = "SELECT * "
               + " FROM inv_storage_scans "
               + " WHERE id=" + scanId + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvStorageScan(propArray[0], logger);
    }

    
    public static InvCollection getCollectionFromMnemonic(
            String mnemonic,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getCollection entered");
        String sql = "select * from " + ContentAbs.COLLECTIONS + " where mnemonic= \'" + mnemonic + "\';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvCollection(propArray[0], logger);
    }
    
    public static String getStorageBase(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getStorageBase entered");
        String sql = "select inv_nodes.base_url from inv_nodes, inv_nodes_inv_objects, inv_objects" 
            + " where inv_objects.ark='" + objectID.getValue() + "'"
            + " and inv_nodes_inv_objects.inv_object_id = inv_objects.id"
            + " and inv_nodes_inv_objects.inv_node_id=inv_nodes.id;";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        return propArray[0].getProperty("base_url");
    }
    
    public static Long getNodeSeq(
            Long nodeId,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getCollection entered");
        if (nodeId == null) return null;
        String sql = "select id from " + ContentAbs.NODES + " where number = \'" + nodeId + "\';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        String nodeSeqS = propArray[0].getProperty("id");
        if (StringUtil.isAllBlank(nodeSeqS)) return null;
        Long nodeSeq = Long.parseLong(nodeSeqS);
        log("nodeSeq:" + nodeSeq);
        return nodeSeq;
    }
    
    public static Long getNodeNumber(
            Long nodeSeq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getCollection entered");
        if (nodeSeq == null) return null;
        String sql = "select number from " + ContentAbs.NODES + " where id = \'" + nodeSeq + "\';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        String nodeNumS = propArray[0].getProperty("number");
        if (StringUtil.isAllBlank(nodeNumS)) return null;
        Long nodeNum = Long.parseLong(nodeNumS);
        log("nodeNum:" + nodeNum);
        return nodeNum;
    }
            
    public static InvObject updateObject(
            Connection connection,
            InvObject entry,
            LoggerInf logger)
        throws TException
    {
        log("updateObjectSQL entered");
        String sql = null;
         try {
            sql = updateContent(connection, "objects", entry, logger);
            Identifier objectID = entry.getArk();
            return getObject(objectID, connection, logger); 

        } catch (Exception ex) {
            System.out.println("getEntry Exception:" + ex);
            ex.printStackTrace();
            return null;
        }
    }

    public static InvOwner getOwner(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getOwner entered");
        String sql = "select * from " + ContentAbs.OWNERS + " where ark = \'" + objectID + "\';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvOwner(propArray[0], logger);
    }

    public static InvOwner getOwnerFromObject(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getOwnerFromObject entered");
        String sql = "select inv_owners.* from inv_objects, inv_owners"
                + " where inv_objects.ark=\'" + objectID + "\'" 
                + " and inv_objects.inv_owner_id=inv_owners.id;";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvOwner(propArray[0], logger);
    }


    public static InvNode getNode(
            int node,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNode entered");
        String sql = "select * from " + ContentAbs.NODES + " where number = " + node + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        if (DEBUG) System.out.println("DUMP getNode" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvNode(propArray[0], logger);
    }
    
    public static InvNode getNodeFromId(
            long nodeseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNode entered");
        String sql = "select * from " + ContentAbs.NODES + " where id = " + nodeseq + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvNode(propArray[0], logger);
    }


    public static InvNodeObject getNodeObject(
            long nodeseq,
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeObject entered");
        String sql = "select * from " + ContentAbs.NODES_OBJECTS + " where inv_node_id = " + nodeseq + " and inv_object_id=" + objectseq + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvNodeObject(propArray[0], logger);
    }


    public static InvNodeObject getNodeObject(
            int nodeNum,
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeObject entered");
        String sql = 
            "select inv_nodes_inv_objects.* "
                    + "from inv_nodes_inv_objects, inv_nodes, inv_objects "
                    + "where inv_nodes_inv_objects.inv_node_id=inv_nodes.id "
                    + "and  inv_nodes_inv_objects.inv_object_id=inv_objects.id "
                    + "and inv_nodes.number=" + nodeNum + " "
                    + "and inv_objects.ark='" + objectID.getValue() + "';";
        
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvNodeObject(propArray[0], logger);
    }

    public static InvNodeObject getNodeObjectPrimary(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeObject entered");
        String sql = 
           "select inv_nodes_inv_objects.* from inv_objects,inv_nodes_inv_objects "
                   + "where inv_objects.id = inv_nodes_inv_objects.inv_object_id "
                   + "and inv_nodes_inv_objects.role='primary'  "
                   + "and inv_objects.ark='" + objectID.getValue() + "';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvNodeObject(propArray[0], logger);
    }
    
    public static Properties[] getNodeObjects(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeObjects entered");
        String sql = 
           "select inv_nodes_inv_objects.*,inv_nodes.number from inv_objects,inv_nodes_inv_objects, inv_nodes "
                   + "where inv_objects.id = inv_nodes_inv_objects.inv_object_id "
                   + "and inv_nodes_inv_objects.inv_node_id = inv_nodes.id "
                   + "and inv_objects.ark='" + objectID.getValue() + "';";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return propArray;
    }


    public static InvNodeObject getNodeObject(
            long nodeseq,
            long objectseq,
            String storageRole,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeObject entered");
        String sql = "select * from " + ContentAbs.NODES_OBJECTS + " where inv_node_id = " + nodeseq + " and inv_object_id=" + objectseq + " and role=\"" + storageRole + "\";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvNodeObject(propArray[0], logger);
    }


    public static InvNodeObject[] getNodeObjects(
            long objectseq,
            String storageRole,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeObject entered");
        String sql = "select * from " + ContentAbs.NODES_OBJECTS + " where inv_object_id=" + objectseq + " and role=\"" + storageRole + "\";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        InvNodeObject [] invNodeObjects = new InvNodeObject[propArray.length];
        int cnt = 0;
        for (Properties prop : propArray) {
            invNodeObjects[cnt] = new InvNodeObject(prop, logger);
            System.out.println("DUMP" + PropertiesUtil.dumpProperties("prop", prop));
            cnt++;
        }
        return invNodeObjects;
    }
    
    public static InvCollectionObject getCollectionObject(
            long objectseq,
            long collectionseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getOwner entered");
        String sql = "select * from " + ContentAbs.COLLECTIONS_OBJECTS + " where inv_collection_id = \'" + collectionseq + "\'"
                + " and inv_object_id = \'" + objectseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvCollectionObject(propArray[0], logger);
    }

    public static InvVersion getVersion(
            long objectseq,
            long versionID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getVersion entered");
        String sql = "select * from " + ContentAbs.VERSIONS + " where inv_object_id = \'" + objectseq + "\'"
                + " and number = \'" + versionID + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvVersion(propArray[0], logger);
    }

    public static InvVersion getVersionFromId(
            long versionseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getVersionFromId entered");
        String sql = "select * from " + ContentAbs.VERSIONS + " where id = " + versionseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvVersion(propArray[0], logger);
    }

    public static long getMaxVersion(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getVersion entered");
        String sql = "select max(number) from " + ContentAbs.VERSIONS + " where inv_object_id=" + objectseq
                + ";";
        log("sql:" + sql);
        //System.out.println("***sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        //System.out.println("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        String longNumS =  propArray[0].getProperty("max(number)");
        if (longNumS == null) return 0;
        return Long.parseLong(longNumS);
    }

    public static InvIngest getIngest(
            long objectseq,
            long versionID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getIngest entered");
        String sql = "select * from " + ContentAbs.INGESTS + " where inv_object_id = \'" + objectseq + "\'"
                + " and inv_version_id = \'" + versionID + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        InvIngest ingest = new InvIngest(logger);
        ingest.setProp(propArray[0]);
        return ingest;
    }

    public static InvIngest getIngest(
            long versionseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getIngest entered");
        String sql = "select * from " + ContentAbs.INGESTS + " where inv_version_id = \'" + versionseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        InvIngest ingest = new InvIngest(logger);
        ingest.setProp(propArray[0]);
        return ingest;
    }

    public static InvDua getDua(
            long objectseq,
            long collectionseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getIngest entered");
        String sql = "select * from " + ContentAbs.DUAS + " where inv_object_id = \'" + objectseq + "\'"
                + " and inv_collection_id = \'" + collectionseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        InvDua dua = new InvDua(logger);
        dua.setProp(propArray[0]);
        return dua;
    }

    public static InvDua getDua(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getIngest entered");
        String sql = "select * from " + ContentAbs.DUAS + " where inv_object_id = \'" + objectseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        InvDua dua = new InvDua(logger);
        dua.setProp(propArray[0]);
        return dua;
    }
    
    public static InvMeta getMeta(
            long objectseq,
            long versionID,
            String fileID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getMeta entered");
        String sql = "select * from " + ContentAbs.METADATAS + " where inv_object_id = \'" + objectseq + "\'"
                + " and inv_version_id = \'" + versionID + "\'"
                + " and filename = \'" + fileID + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        InvMeta meta = new InvMeta(logger);
        meta.setProp(propArray[0]);
        return meta;
    }
    
    public static InvFile getFile(
            long objectseq,
            long versionseq,
            String pathName,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getFile entered");
        String sql = "select * from " +  ContentAbs.FILES
                + " where inv_object_id = \'" + objectseq + "\'"
                + " and inv_version_id = \'" + versionseq + "\'"
                + " and BINARY pathname = \'" + mySQLEsc(pathName) + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvFile(propArray[0], logger);
    }
    
    public static InvFile getFile(
            long fileseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getFile entered:" + fileseq);
        String sql = "select * from " +  ContentAbs.FILES
                + " where id = \'" + fileseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvFile(propArray[0], logger);
    }
    
    public static InvFile getFile(
            String objectName,
            long versionNum,
            String pathName,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getFile entered");
        String sql = "select inv_files.* " 
            + "from inv_files,inv_versions \'" + objectName + "\' "
            + "and inv_versions.number=\'" + versionNum + "\' "
            + "and inv_files.pathname=\'" + pathName + "\' "
            + "and inv_versions.id = inv_files.inv_version_id"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvFile(propArray[0], logger);
    }
    
    public static long getAccessVersionNum(
            Identifier ark,
            long versionNum,
            String pathname,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getAccessVersionNum entered");
        if (versionNum == 1) return 1;
        String arkS = ark.getValue();
        String sql = "select inv_versions.number"
           + " from inv_objects, inv_files, inv_versions"
           + " where inv_objects.ark=" + "'" + arkS + "'"
           + " and inv_files.pathname='" + pathname + "'"
           + " and inv_versions.number <= " + versionNum
           + " and inv_files.billable_size > 0"
           + " and inv_files.inv_object_id = inv_objects.id"
           + " and inv_versions.id = inv_files.inv_version_id"
           + " ORDER BY inv_versions.number DESC"
           + " LIMIT 1"
           + ";"   ;
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        Properties prop = propArray[0];
        String numberS = prop.getProperty("number");
        if (numberS == null) return 0;
        return Long.parseLong(numberS);
    }
    
    public static String getAccessNodeVersionKey(
            Identifier ark,
            long versionNum,
            String pathname,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getAccessNodeVersionKey entered");
        String arkS = ark.getValue();
        
        if (versionNum == 0) {
            versionNum = 100000;
        }
        
        String sql = "select inv_nodes.number as nnbr,inv_objects.ark, inv_versions.number as vnbr, inv_files.pathname, inv_files.billable_size"
          + " from inv_files,inv_versions,inv_objects,inv_nodes_inv_objects,inv_nodes"
          + " where inv_objects.ark=" + "'" + arkS + "'"
          + " and inv_versions.number <= " + versionNum
          + " and inv_files.pathname='" + pathname + "'"
          + " and inv_nodes_inv_objects.role = 'primary'"
          + " and inv_files.billable_size > 0"
          + " and inv_nodes_inv_objects.inv_object_id=inv_objects.id"
          + " and inv_nodes.id = inv_nodes_inv_objects.inv_node_id"
          + " and inv_files.inv_object_id=inv_objects.id"
          + " and inv_versions.id=inv_files.inv_version_id"
          + " and inv_versions.inv_object_id=inv_objects.id"
          + " ORDER BY inv_versions.number DESC LIMIT 1";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        Properties prop = propArray[0];
        if ((prop == null) || (prop.size() == 0)) {
            return null;
        }
        String nodeS = prop.getProperty("nnbr");
        if (nodeS == null) return null;
        String versionS = prop.getProperty("vnbr");
        if (versionS == null) return null;
        String retval = nodeS + "#" + arkS + "|" + versionS + "|" + pathname;
        return retval;
    }
    
    
    public static long getAccessNodeNumber(
            Identifier ark,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getAccessNodeManifest entered");
        String arkS = ark.getValue();
        
        String sql = "select inv_nodes.number"
         + " from inv_nodes_inv_objects, inv_nodes, inv_objects"
         + " where inv_objects.ark=" + "'" + arkS + "'"
         + " and inv_nodes_inv_objects.inv_object_id=inv_objects.id"
         + " and inv_nodes_inv_objects.role='primary'"
         + " and inv_nodes.id=inv_nodes_inv_objects.inv_node_id;";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        Properties prop = propArray[0];
        if ((prop == null) || (prop.size() == 0)) {
            return 0;
        }
        String nodeS = prop.getProperty("number");
        long node = Long.parseLong(nodeS);
        return node;
    }
    
    public static List<InvFile> getFiles(
            Identifier ark,
            long versionNum,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getFiles entered");
        String arkS = ark.getValue();
        String sql = "select inv_files.* "
            + "from inv_objects,inv_files,inv_versions "
            + "where inv_objects.ark='" + arkS + "' "
            + "and inv_versions.number=" + versionNum + " "
            + "and inv_files.inv_object_id=inv_objects.id "
            + "and inv_files.inv_version_id=inv_versions.id"
            + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvFile> files = new ArrayList<>();
        for (int f = 0; f<propArray.length; f++) {
            InvFile invFile =  new InvFile(propArray[f], logger);
            files.add(invFile);
            log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[f]));
        }
        
        return files;
    }

    public static Properties[] getObjectUrl(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        if (objectID == null) {
            throw new TException.INVALID_OR_MISSING_PARM("getObjectUrl - objectID required");
        }
        String arkS = objectID.getValue();
        log("getObjectUrl entered:" + arkS);
        String sql = "select base_url,number,ark,inv_nodes_inv_objects.role from inv_objects,inv_nodes,inv_nodes_inv_objects"
            + " where inv_objects.ark=\'" + arkS + "\'" 
            + " and inv_nodes_inv_objects.inv_object_id=inv_objects.id " 
            + " and inv_nodes.id=inv_nodes_inv_objects.inv_node_id"
            + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return propArray;
    }

    public static Properties getVersionsStuff(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        if (objectID == null) {
            throw new TException.INVALID_OR_MISSING_PARM("getObjectUrl - objectID required");
        }
        String arkS = objectID.getValue();
        log("getVersionsStuff entered:" + arkS);
        
        
        String sql = "select inv_nodes.number, inv_objects.ark, inv_nodes.logical_volume, inv_objects.md5_3"
           + " from inv_nodes, inv_objects, inv_nodes_inv_objects"
           + " where inv_objects.ark=" + "'" + arkS+ "'"
           + " and inv_nodes_inv_objects.inv_object_id=inv_objects.id"
           + " and inv_nodes_inv_objects.role='primary'"
           + " and inv_nodes.id = inv_nodes_inv_objects.inv_node_id"
           + ";"   ;
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return propArray[0];
    }

    public static Properties getVersionsFileStuff(
            Identifier objectID,
            long version,
            String fileID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        if (objectID == null) {
            throw new TException.INVALID_OR_MISSING_PARM("getObjectUrl - objectID required");
        }
        String arkS = objectID.getValue();
        log("getVersionsStuff entered:" + arkS);
        String versionsql = "";
        if (version > 0) {
            versionsql = " AND v.number <= " + version;
        }
        
        if (fileID.contains("'")) {
            fileID = fileID.replace("'", "''");
        }
        
        String sql = "SELECT n.NUMBER AS node, n.logical_volume, o.version_number, o.md5_3, f.billable_size, v.ark, v.NUMBER AS key_version, f.pathname "
           + " FROM inv_versions AS v,"
           + " inv_nodes_inv_objects AS NO,"
           + " inv_nodes AS n,"
           + " inv_files AS f,"
           + " inv_objects AS o"
           + " WHERE v.ark='" + arkS + "'"
           + versionsql
           + " AND f.pathname='" + fileID + "'"
           + " AND o.id=v.inv_object_id"
           + " AND NO.role='primary'"
           + " AND f.inv_version_id=v.id"
           + " AND NO.inv_object_id=v.inv_object_id"
           + " AND n.id=NO.inv_node_id"
           + " AND f.billable_size > 0"
           + ";" ;        
        
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        
        return propArray[propArray.length-1];
    }
    
    public static InvGCopy getFromFileKey(
            String fileKey,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getFileKey entered:" + fileKey);
        String sql = "select * from " +  ContentAbs.GCOPIES
                + " where copy_key=\'" + fileKey + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvGCopy(propArray[0], logger);
    }
    
    public static List<InvGCopy> getGCopies(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getOwner entered");
        String sql = "select * from " + ContentAbs.GCOPIES + " where inv_object_id = \'" + objectseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvGCopy> copiesList = new ArrayList<>(propArray.length);
        
        for (Properties prop : propArray) {
            InvGCopy copy = new InvGCopy(prop, logger);
            copiesList.add(copy);
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return copiesList;
    }

    
    public static InvGCopy getGCopy(
            long fileseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getOwner entered");
        String sql = "select * from " + ContentAbs.GCOPIES + " where inv_file_id = \'" + fileseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvGCopy(propArray[0], logger);
    }

    
    public static Long getDBCnt(
            String table,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getOwner entered");
        String sql = "select count(*) from " + table + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length != 1) {
            log("InvDBUtil - length != 1:" + propArray.length);
            return null;
        }
        String cntS = propArray[0].getProperty("count(*)");
        Long size = Long.parseLong(cntS);
        return size;
    }

    public static boolean deleteGCopy(
            long copyseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("deleteGCopy entered");
        String sql = "delete from inv_glacier_copies "
                + "where id=" + copyseq;
        log("sql:" + sql);
        boolean works = DBUtil.exec(connection, sql, logger);
        return works;
    }
    

    public static InvDKVersion getDKVersion(
            long objectseq,
            long versionseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getDC entered");
        String sql = "select * from " + ContentAbs.DUBLINKERNELS + " where inv_object_id = \'" + objectseq + "\'"
                + " and inv_version_id = \'" + versionseq + "\'"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvDKVersion(objectseq, versionseq, propArray, logger);
    }
    
    public static String updateContent(
            Connection connection,
            String table,
            ContentAbs entry,
            LoggerInf logger)
        throws TException
    {
        log("updateContent SQL entered");
        Properties prop = entry.retrieveProp();
        return updateEntry(connection, table, prop, logger);
    }

    public static String updateEntry(
            Connection connection,
            String table,
            Properties inProp,
            LoggerInf logger)
        throws TException
    {
        try {
            String update = DBUtil.buildModifyNull(inProp);
            String sql = "REPLACE INTO " + table + " SET " + update;
            Properties[] propArray = DBUtil.cmd(connection, sql, logger);
            return sql;

        } catch (Exception ex) {
            System.out.println("getEntry Exception:" + ex);
            ex.printStackTrace();
            return null;
        }
    }

    public static long updateText(
            ContentAbs content, 
            String key,
            String value,
            Connection connection, 
            LoggerInf logger)
        throws TException
    {
        long autoID = 0;
        PreparedStatement pst = null;
        String sql = "UPDATE " 
                + content.getDBName() 
                + " SET " + key + " = ?"
                + " WHERE id=" + content.getId() + ";";
        try {
            if (DEBUG) System.out.println(MESSAGE + "updateText sql=" + sql);         
            pst = connection.prepareStatement(sql);  
            pst.setBytes(1, value.getBytes("utf-8"));
            pst.execute(); 
            content.setNewEntry(true);
            return content.getId();
            
        } catch (TException tex) {
            logger.logError(MESSAGE + "Exception:" + tex, 0);
            logger.logError(StringUtil.stackTrace(tex), 5);
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.logError(MESSAGE
                        + "Fail sql=" + sql + "CException:" + ex, 0);
            logger.logError(StringUtil.stackTrace(ex), 5);
            throw new TException.SQL_EXCEPTION(ex);
        }
    
    }
    protected static void log(String msg)
    {
        if (!DEBUG) return;
        System.out.println(MESSAGE + msg);
    }
    
    public static String mySQLEsc(String in)
    {
        if (StringUtil.isAllBlank(in)) return in;
        String ina = in.replace("'", "\\'");
        String inb = ina.replace("\"", "\\\"");
        return inb;
    }

    public static ArrayList<InvFile> getObjectFiles(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getObjectFiles entered");
        String sql = "select * "
                + "from inv_files "
                + "where inv_object_id=" + objectseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvFile> list = new ArrayList<>();
        for (Properties prop : propArray) {
            System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            String billableSizeS = prop.getProperty("billable_size");
            if (StringUtil.isAllBlank(billableSizeS)) {
                continue;
            }
            long billableSize = Long.parseLong(billableSizeS);
            if (billableSize == 0) {
                continue;
            }
            InvFile file = new InvFile(prop, logger);
            list.add(file);
        }
        return list;
    }

    public static ArrayList<InvFile> getVersionFiles(
            Identifier objectID,
            long versionID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getVersionFiles entered");
        String sql = "select inv_files.* "
            + "from inv_versions, inv_files "
            + "where inv_versions.ark='" + objectID.getValue() + "' "
            + "and inv_versions.number=" + versionID + " "
            + "and inv_files.inv_version_id=inv_versions.id"
            + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvFile> list = new ArrayList<>();
        for (Properties prop : propArray) {
            //System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            InvFile file = new InvFile(prop, logger);
            list.add(file);
        }
        return list;
    }

    public static ArrayList<InvNodeObject> getObjectNodes(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getObjectNodes entered");
        String sql = "select * "
                + "from inv_nodes_inv_objects "
                + "where inv_object_id=" + objectseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<InvNodeObject> list = new ArrayList<>();
        for (Properties prop : propArray) {
            log(PropertiesUtil.dumpProperties("***out dump***", prop));
            InvNodeObject nodeObject = new InvNodeObject(prop, logger);
            list.add(nodeObject);
        }
        return list;
    }

    public static ArrayList<InvNodeObject> getObjectNodesReplication(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getObjectNodes entered");
        String sql = "select inv_nodes_inv_objects.* "
                + "from inv_nodes_inv_objects, inv_collections_inv_nodes, inv_collections_inv_objects "
                + "where inv_collections_inv_objects.inv_collection_id = inv_collections_inv_nodes.inv_collection_id "
                + "and inv_nodes_inv_objects.inv_object_id = inv_collections_inv_objects.inv_object_id " 
                + "and ((inv_nodes_inv_objects.role='secondary' " 
                + "and inv_collections_inv_nodes.inv_node_id=inv_nodes_inv_objects.inv_node_id) " 
                + "or inv_nodes_inv_objects.role='primary') " 
                + "and inv_nodes_inv_objects.inv_object_id=" + objectseq
                + ";";
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            System.out.println("InvDBUtil - prop null");
            System.out.println("sql:" + sql);
            return null;
        } else if (propArray.length == 0) {
            System.out.println("InvDBUtil - length == 0");
            System.out.println("sql:" + sql);
            return null;
        }
        ArrayList<InvNodeObject> list = new ArrayList<>();
        for (Properties prop : propArray) {
            System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            InvNodeObject nodeObject = new InvNodeObject(prop, logger);
            list.add(nodeObject);
        }
        return list;
    }


    public static InvAudit getAudit(
            long nodeseq,
            long objectseq,
            long fileseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getObjectNodes entered");
        String sql = "select * "
                + "from inv_audits "
                + "where inv_node_id=" + nodeseq
                + " and inv_object_id=" + objectseq
                + " and inv_file_id=" + fileseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        } else if (propArray.length > 1) {
            log("InvDBUtil - length > 1");
            return null;
        }
        Properties prop = propArray[0];
        InvAudit audit = new InvAudit(prop, logger);
        return audit;
    }


    public static List<Integer> getNodes(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodes entered");
        InvObject invObject = getObject(objectID, connection, logger);
        if (invObject == null) return null;
        long objectseq = invObject.getId();
        String sql = "select inv_nodes.number "
                + "from inv_objects,inv_nodes_inv_objects,inv_nodes "
                + "where inv_objects.ark=\'" + objectID.getValue() + "\' "
                + "and inv_objects.id = inv_nodes_inv_objects.inv_object_id "
                + "and inv_nodes_inv_objects.inv_node_id = inv_nodes.id"
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<Integer> list = new ArrayList<>();
        for (Properties prop : propArray) {
            System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            String nodeS = prop.getProperty("number");
            if (StringUtil.isAllBlank(nodeS)) continue;
            Integer ival = Integer.parseInt(nodeS);
            list.add(ival);
        }
        return list;
    }
    
    public static String getNodesString(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodes entered");
        List<Integer> nodes = getNodes(objectID, connection, logger);
        StringBuffer buf = new StringBuffer();
        for (int node : nodes) {
            if (buf.length() > 0) buf.append(",");
            buf.append(node);
        }
        return buf.toString();
    }



    public static List<Long> getNodesList(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodeseq entered");
        String sql = "select inv_nodes_inv_objects.inv_node_id "
                + "from inv_nodes_inv_objects "
                + "where inv_object_id=" + objectseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<Long> list = new ArrayList<>();
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***id dump***", prop));
            String nodeS = prop.getProperty("inv_node_id");
            if (StringUtil.isAllBlank(nodeS)) continue;
            Long ival = Long.parseLong(nodeS);
            list.add(ival);
        }
        if (list.isEmpty()) return null;
        return list;
    }
    
    public static int getVersionCnt(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getVersionCnt entered");
        String sql = "select max(number) "
                + "from inv_versions "
                + "where " + "ark=\"" + objectID.getValue() + "\""
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***ver dump***", prop));
            String nodeS = prop.getProperty("max(number)");
            if (StringUtil.isAllBlank(nodeS)) continue;
            return Integer.parseInt(nodeS);
        }
        return 0;
    }
    
    public static boolean entry2File(
            String sql,
            String column,
            File outFile,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getEntryValue");
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return false;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return false;
        }
        
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***ver dump***", prop));
            String retValue = prop.getProperty(column);
            if (StringUtil.isAllBlank(retValue)) continue;
            FileUtil.string2File(outFile, retValue);
            return true;
        }
        return false;
    }

    public static ArrayList<Long> getCollectionNodes(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getCollectionMapNodes entered");
        String sql = "select inv_collections_inv_nodes.inv_node_id "
                + "from inv_collections_inv_objects, inv_collections_inv_nodes "
                + "where inv_collections_inv_objects.inv_collection_id = inv_collections_inv_nodes.inv_collection_id " 
                + "and inv_collections_inv_objects.inv_object_id=" + objectseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<Long> list = new ArrayList<>();
        for (Properties prop : propArray) {
            String nodeseqS = prop.getProperty("inv_node_id");
            if (nodeseqS == null) continue;
            System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            Long nodeseq = Long.parseLong(nodeseqS);
            list.add(nodeseq);
        }
        return list;
    }

    public static InvCollectionNode getCollectionNode(
            long collectionseq,
            long nodeseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getCollectionMap entered");
        String sql = "select * from " + ContentAbs.COLLECTION_NODES + " where inv_node_id=" + nodeseq
                + " AND inv_collection_id=" + collectionseq + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvCollectionNode(propArray[0], logger);
    }
    
    public static InvLocalID getPrimaryFromLocal(
            Identifier ownerArk,
            String localID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getPrimaryFromLocal entered");
        String sqlID = null;
        if (!localID.contains("'")) {
            sqlID = " AND local_id='" + localID + "' ";
        } else {
            sqlID = " AND local_id=\"" + localID + "\" ";
        }
        String sql = "select * from " + ContentAbs.LOCALS 
                + " where inv_owner_ark='" + ownerArk.getValue() + "' "
                + sqlID
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        } else if (propArray.length > 1) {
            log("InvDBUtil - length > 1");
            return null;
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvLocalID(propArray[0], logger);
    }
    
    public static InvAddLocalID getNextLocal(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNextLocal entered");
        String sql = "select inv_objects.id as objectseq, "
            + "inv_objects.ark as object_ark, "
            + "inv_owners.ark as owner_ark, "
            + "inv_objects.erc_where as locals "
            + "from inv_objects, inv_owners "
            + "where  inv_objects.id > " + objectseq + " "
            + "and not inv_objects.erc_where like '%:unas%' "
            + "and inv_owners.id = inv_objects.inv_owner_id "
            + "ORDER BY inv_objects.id LIMIT 1 "
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        } else if (propArray.length > 1) {
            log("InvDBUtil - length > 1");
            return null;
        }
        //log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return new InvAddLocalID(propArray[0]);
    }
    
    public static ArrayList<InvLocalID> getLocalFromPrimary(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getLocalFromPrimary entered");
        String sql = "select * from " + ContentAbs.LOCALS 
                + " where inv_object_ark='" + objectID.getValue() + "'"
                + ";";
        ArrayList<InvLocalID> localList = new ArrayList<>();
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        for (Properties prop : propArray) {
            InvLocalID local = new InvLocalID(prop, logger);
            localList.add(local);
        }
        log("DUMP" + PropertiesUtil.dumpProperties("prop", propArray[0]));
        return localList;
    }
    
    public static long getPrimaryNodeseq(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getPrimaryNodeseq entered");
        
        
        String sql = "select inv_node_id "
                + "from inv_nodes_inv_objects "
                + "where role='primary' "
                + "and inv_object_id=" + objectseq;
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***ver dump***", prop));
            String nodeS = prop.getProperty("inv_node_id");
            if (StringUtil.isAllBlank(nodeS)) continue;
            return Integer.parseInt(nodeS);
        }
        return 0;
    }
    
    public static int getAuditVersionCnt(
            long nodeseq,
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getAuditVersionCnt entered");
        String sql = "select distinct inv_version_id from inv_audits"
                + " where inv_object_id=" + objectseq
                + " and inv_node_id=" + nodeseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        return propArray.length;
    }
    
    public static int getSecondaryVersionCnt(
            long nodeseq,
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getSecondaryVersionCnt entered");
        
        String sql = "select version_number from inv_nodes_inv_objects"
                + " where inv_object_id=" + objectseq
                + " and inv_node_id=" + nodeseq
                + ";";
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return 0;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return 0;
        }
        String versionS = propArray[0].getProperty("version_number");
        if (versionS == null) {
            throw new TException.INVALID_ARCHITECTURE( "getSecondaryVersionCnt"
                    + " - objectseq=" + objectseq
                    + " - nodeseq=" + nodeseq
            );
        }
        return Integer.parseInt(versionS);
    }


    public static List<Properties> getAudits(
            long nodeseq,
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodes entered");
        String sql = "select * from inv_audits "
                + "where inv_node_id=" + nodeseq 
                + " and inv_object_id=" + objectseq;
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<Properties> list = new ArrayList<>();
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            list.add(prop);
        }
        return list;
    }

    public static List<Properties> getAudits(
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("getNodes entered");
        String sql = "select * from inv_audits "
                + "where inv_object_id=" + objectseq;
        log("sql:" + sql);
        Properties[] propArray = DBUtil.cmd(connection, sql, logger);
        
        if ((propArray == null)) {
            log("InvDBUtil - prop null");
            return null;
        } else if (propArray.length == 0) {
            log("InvDBUtil - length == 0");
            return null;
        }
        ArrayList<Properties> list = new ArrayList<>();
        for (Properties prop : propArray) {
            if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("***out dump***", prop));
            list.add(prop);
        }
        return list;
    }
    

    public static boolean deleteAudits(
            long nodeseq,
            long objectseq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        log("deleteFixity entered");
        String sql = "delete from inv_audits "
                + "where inv_node_id=" + nodeseq 
                + " and inv_object_id=" + objectseq;
        log("sql:" + sql);
        boolean works = DBUtil.exec(connection, sql, logger);
        return works;
    }
    
    public static boolean resetAuditNode(
            long sourceNodeSeq, 
            long targetNodeSeq, 
            long objectSeq,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        try {
            if (sourceNodeSeq <= 0) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "resetAuditNode - sourceNodeSeq not valid:" + sourceNodeSeq);
            }
            if (targetNodeSeq <= 0) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "resetAuditNode - targetNodeSeq not valid:" + targetNodeSeq);
            }
            if (objectSeq <= 0) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "resetAuditNode - objectseq not valid:" + objectSeq);
            }
            String sql =
                    "UPDATE inv_audits set inv_node_id=" + targetNodeSeq 
                    + " WHERE inv_node_id=" + sourceNodeSeq 
                    + " and inv_object_id=" + objectSeq
                    + ";";
            
            log("sql:" + sql);
            return DBUtil.exec(connection, sql, logger);
            
        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    public static Integer currentNode(List<Integer> nodeList, Integer inputNode)
        throws TException 
    {
        if ((inputNode == null) || (inputNode <= 0)) {
            throw new TException.INVALID_OR_MISSING_PARM("currentNode - inputNode not supplied");
        }
        if ((nodeList == null) || nodeList.isEmpty()) {
            return inputNode;
        }
        StringBuffer buf = new StringBuffer();
        for (Integer retNode : nodeList) {
            if (retNode == inputNode) {
                return inputNode;
            }
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("" + retNode);
        }
        throw new TException.REQUEST_ITEM_EXISTS("Node exists but not matched:" 
                + " - inputNode=" + inputNode
                + " - db Nodes=" + buf.toString()
                );
    }
    
    public static int deleteNodeObject(
            long nodeseq,
            long objectseq, 
            Connection connection, 
            LoggerInf logger)
        throws TException
    {
        try {
            if (objectseq <= 0) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "delete - objectseq not valid:" + objectseq);
            }
            String sql =
                    "DELETE FROM inv_nodes_inv_objects WHERE inv_node_id=" + nodeseq 
                    + " and inv_object_id=" + objectseq;
            
            int delCnt= DBDelete.delete(connection, sql, logger);
            if (DEBUG) System.out.println(MESSAGE + "delete:" 
                    + " - sql=" + sql
                    + " - delCnt=" + delCnt
                    );
            return delCnt;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
}

