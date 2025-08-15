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
package org.cdlib.mrt.inv.admin;


import org.cdlib.mrt.inv.action.*;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cdlib.mrt.cloud.ManifestSAX;

import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.cloud.VersionMap;
import org.cdlib.mrt.core.Identifier;

import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvCollectionObject;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.URLEncoder;
import org.json.JSONObject;

/**
 * Abstract for performing a inv
 * @author dloy
 */
public class AdminShare
{

    protected static final String NAME = "InvActionAbs";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final String  STATUS_PROCESSING = "processing";  
    public enum AdminType { sla, owner, collection, init }
    public enum CollectionType { collection_public, collection_private }

    protected LoggerInf logger = null;
    protected Connection connection = null;
    protected Exception exception = null;
    protected AdminType processType = null;
    protected DBAdd dbAdd = null;
    protected Boolean commit = null;

    protected static final Logger log4j = LogManager.getLogger(); 
    
    
    protected AdminShare(
            AdminType processType,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        this.processType = processType;
        this.logger = logger;
        this.connection = connection;
        this.dbAdd = new DBAdd(connection, logger);
    }

    public AdminType getProcessType() {
        return processType;
    }

    public void setProcessType(AdminType processType) {
        this.processType = processType;
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
   

    public static void validObjectID(Identifier id, String type)
        throws TException
    {
        if (id == null) {
            throw new TException.INVALID_OR_MISSING_PARM(type + " not provided");
        }
    }

    public static void validString(String val, String type)
        throws TException
    {
        if (StringUtil.isAllBlank(val)) {
            throw new TException.INVALID_OR_MISSING_PARM(type + " not provided");
        }
    }

    public static void validMembers(List<Identifier> ids)
        throws TException
    {
        if ((ids == null) || (ids.isEmpty())) {
            throw new TException.INVALID_OR_MISSING_PARM("members not provided");
        }
    }
    
    public static void validConnect(Connection connection)
        throws TException
    {
        try {
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
                System.out.println("reset autocommit false");
            }
        } catch (Exception e) {
            throw new TException.INVALID_OR_MISSING_PARM("unable to turn off autocommit:" + e);
        }
        try {
            connection.isValid(20);
        } catch (Exception e) {
            throw new TException.INVALID_OR_MISSING_PARM("connection is invalid:" + e);
        }
    }
    
    public static InvObject getObject (
            Identifier objectID, 
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        InvObject invObject = null;
        try {
            invObject = InvDBUtil.getObject(objectID, connection, logger);
            if (invObject == null) {
                return null;
            }
            return invObject;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public static InvOwner getOwner (
            Identifier ownerID, 
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        InvOwner invOwner = null;
        try {
            log4j.debug("getOwner.ownerID=" + ownerID.getValue());
            invOwner = InvDBUtil.getOwner(ownerID, connection, logger);
            return invOwner;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public static InvCollection getCollection (
            Identifier collectionID, 
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        String collectionArk = collectionID.getValue();
        InvCollection invCollection = null;
        try {
            invCollection = InvDBUtil.getCollection(collectionArk, connection, logger);
            return invCollection;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    public static AdminType getAdminType(String adminTypeS)
        throws TException
    {
        AdminType adminType = null;
        if (StringUtil.isAllBlank(adminTypeS)) {
            throw new TException.INVALID_OR_MISSING_PARM("Object type empty");
        }
        adminTypeS = adminTypeS.toLowerCase();
        
        try {
            adminType = AdminType.valueOf(adminTypeS);
        } catch (Exception ex) {
            throw new TException.INVALID_OR_MISSING_PARM("Object type not valid:" + adminTypeS);
        }
        return adminType;
    }
    
    public static ArrayList<Identifier> getMembers(String membersS)
        throws TException
    {
        ArrayList<Identifier> members = new ArrayList<Identifier>();
        try {
            if (StringUtil.isAllBlank(membersS)) {
                throw new TException.INVALID_OR_MISSING_PARM("membersS is empty");
            }
            String [] membersA = membersS.split("\\s*\\;\\s*");
            for (String memberS : membersA) {
                Identifier member = new Identifier(memberS);
                members.add(member);
            }
            return members;
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected static Properties loadProperties(String propLookup)
        throws TException
    {
        Properties properties = new Properties();
        String propName = "resources/properties/admin/" + propLookup + ".properties";
        System.out.println("Propname=" + propName);
        try {
            InputStream input = AdminShare.class.getClassLoader().getResourceAsStream(propName);
            if(input != null) {
                log4j.debug(PropertiesUtil.dumpProperties("loadPropertie:" + propLookup, properties));
                properties.load(input);
                return properties;
            } else {
                throw new TException.INVALID_OR_MISSING_PARM("adminType invalid:" + propLookup);
            }
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    protected static HashMap<String, Identifier> loadIdentifiers(String propLookup)
        throws TException
    {
        HashMap<String,Identifier> idMap = new HashMap<>();
        try {
            Properties prop = loadProperties(propLookup);
            Set keys = prop.keySet();
            for (Object keyO : keys) {
                String key = (String)keyO;
                String val = prop.getProperty(key);
                Identifier id = new Identifier(val);
                idMap.put(key, id);
            }
            return idMap;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    protected void addMembers(InvObject invObject, List<Identifier> collectionIDs)
        throws TException
    {
        log4j.debug("addMembers entered");
        for(Identifier collectionID : collectionIDs) {
            addMember(invObject, collectionID);
        }
    }
    
    protected InvCollectionObject addMember(InvObject invObject, Identifier collectionID)
        throws TException
    {
        System.out.println(invObject.dump("memberObject"));
        try {
            InvCollection memberCollection = getCollection(collectionID, connection, logger);
            if (memberCollection == null) {
                throw new TException.INVALID_OR_MISSING_PARM("Collection for member does not exist:" + collectionID.getValue());
            }
            System.out.println(memberCollection.dump("memberCollection"));
            InvCollectionObject invCollectionObject 
                    = InvDBUtil.getCollectionObject(invObject.getId(), memberCollection.getId(), connection, logger);
            if (invCollectionObject != null) {
                System.out.println("CollectionObject already exists for:" 
                        + " - collectionID:" + collectionID.getValue()
                        + " - invObject:" + invObject.getArk().getValue()
                );
                return invCollectionObject;
            }
            long objectseq = invObject.getId();
            long collectionseq = memberCollection.getId();
            invCollectionObject = InvDBUtil.getCollectionObject(objectseq, collectionseq, connection, logger);
            if (invCollectionObject == null) {
                invCollectionObject = new InvCollectionObject(logger);
                invCollectionObject.setObjectID(objectseq);
                invCollectionObject.setCollectionID(collectionseq);
                long id = dbAdd.insert(invCollectionObject);
                invCollectionObject.setId(id);
                System.out.println(invCollectionObject.dump("***Add member***"));
            } else {
                System.out.println(invCollectionObject.dump("***Member Exists***"));
            }
            
            return invCollectionObject;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    public static JSONObject setJsonPublic()
        throws TException
    {
        JSONObject jsonObj = new JSONObject();
        try { 
            jsonObj.put("read_privilege", "public");
            jsonObj.put("write_privilege", "restricted");
            jsonObj.put("download_privilege", "public");
            jsonObj.put("storage_tier", "standard");
            jsonObj.put("harvest_privilege", "public");
            return jsonObj;

        } catch (Exception ex) {
            throw new TException.INVALID_OR_MISSING_PARM("Bad json:" + ex);
        }
    }

    public AdminShare setCommit(Boolean commit) {
        this.commit = commit;
        System.out.println("setCommit called:" + commit);
        return this;
    }

    public Boolean getCommit() {
        return commit;
    }
    
    protected static void close(Connection connect)
    {
        try {
            connect.close();
        } catch (Exception e) {
                System.out.println("close connection exception (no biggy):" + e);
        }
    }
}

