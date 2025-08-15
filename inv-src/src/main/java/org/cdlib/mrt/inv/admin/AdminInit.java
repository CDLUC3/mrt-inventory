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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.service.InventoryConfig;
import org.cdlib.mrt.log.utility.AddStateEntryGen;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.json.JSONObject;


/**
 * Run fixity
 * @author dloy
 */
public class AdminInit
        extends AdminShare
{

    protected static final String NAME = "AdminInit";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
        
    protected Identifier ownerID = null;
    protected Identifier ownerArkOwnerID = null;
    protected InvOwner ownerOwnerInv = null;
    protected InvObject ownerOwnerObjectInv = null;
    protected InvCollection systemCollection = null;
    protected InvCollection slaCollection = null;
    protected InvCollection ownerCollection = null;
    protected InvCollection collectionCollection = null;
    
    protected String name = null;
    //protected String mnemonic = null;
    protected List<Identifier> members = null;
            
    protected int node = 0;
    protected int toNode = 0;
    protected long objectseq = 0;
    protected HashMap<String,Identifier> initList = null;
    protected AdminOwnerOwner adminOwnerOwner = null;
    protected InvOwner ownerOwner = null;
    protected JSONObject buildResponseJson = null;
    
    public static void main(String[] argv) { // owner
        
       
        InventoryConfig config = null;
        Connection connection = null;
        LoggerInf logger = null;
        AdminInit adminInit = null;
    	try {
            config = InventoryConfig.useYaml();
            LinkedHashMap<String,Identifier> map = config.getAdminMap();
            config.dbStartup();
            connection = config.getConnection(false);
            logger = config.getLogger();
            adminInit = AdminInit.getAdminInit(map,connection,logger);
            //adminInit.setCommit(true);
            adminInit.setCommit(false);
            adminInit.build();
            JSONObject buildResponse = adminInit.getBuildResponseJson();
            System.out.println("AFTER MAIN");
            System.out.println(buildResponse.toString(2));
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
                
        } finally {
            close(connection);
        } 
      
    }
    
    public static AdminInit getAdminInit(
            HashMap<String,Identifier> initList,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AdminInit(initList, connection, logger);
    }
    
    public AdminInit(
            HashMap<String,Identifier> initList,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(AdminShare.AdminType.init, connection, logger);
        this.initList = initList;
    }
    
    protected void setDb()
        throws TException
    {
        
    }
    
    public void build()
        throws TException
    {
        try {
            buildOwnerOwner();
            buildSystemCollection();
            buildSLACollection();
            buildOwnerCollection();
            buildCollectionCollection();
            addOwnerOwnerToCollections();
            buildResponseJson = buildResponse();
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException)ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    public JSONObject buildResponse()
        throws TException
    {
        JSONObject response = new JSONObject();
        
        try {
            JSONObject ownerOwnerJSON = ownerOwnerInv.dumpJson("ownerOwner");
            response.put("ownerOwner", ownerOwnerJSON);
            JSONObject systemCollectionJSON = systemCollection.dumpJson("systemCollection");
            response.put("systemCollection", systemCollectionJSON);
            JSONObject slaCollectionJSON = slaCollection.dumpJson("slaCollection");
            response.put("slaCollection", slaCollectionJSON);
            JSONObject ownerCollectionJSON = ownerCollection.dumpJson("ownerCollection");
            response.put("ownerCollection", ownerCollectionJSON);
            JSONObject collectionCollectionJSON = collectionCollection.dumpJson("collectionCollection");
            response.put("collectionCollection", collectionCollectionJSON);
            if (commit == null) {
                connection.rollback();
                response.put("respStatus", "unknown");
                System.out.println("AdminInit rollback 1");
            } else if (commit) {
                connection.commit();
                System.out.println("AdminInit commit.1");
                response.put("respStatus", "commit");
            } else {
                connection.rollback();
                response.put("respStatus", "rollback");
                System.out.println("AdminInit rollback 2");
            }
            response.put("header", "AdminInit");
            AddStateEntryGen.addLogStateEntry("AdminInit",response);
            return response;
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof  TException) {
                throw (TException) ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    public void buildOwnerOwner()
        throws TException
    {
        try {
            System.out.println("***Start buildOwnerOwner\n");
            Identifier ownerOwnerID = initList.get("ownerOwner");
            String name = "Merritt administrative owner";
            
            AdminOwnerOwner adminOwner = AdminOwnerOwner.getAdminOwnerOwner( 
                    ownerOwnerID, name, connection, logger);
            adminOwner.setCommit(null);
            adminOwner.buildOwner();
            ownerOwnerInv = adminOwner.getNewOwner();
            ownerOwnerObjectInv = adminOwner.getOwnerObject();
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException)ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    public void buildSystemCollection()
        throws TException
    {
        try {
            System.out.println("***Start buildSystemCollection\n");
            Identifier systemClassesID = initList.get("systemClasses");
            Identifier ownerOwnerID = initList.get("ownerOwner");
            String name = "Merritt administrative systemClasses";
            String mnemonic = "mrt_system_classes";
            ArrayList<Identifier> membersOf = new ArrayList<>();
            membersOf.add(systemClassesID); 
            
            AdminCollection adminCollection = AdminCollection.getAdminCollection(
                    true, systemClassesID, ownerOwnerID, name, mnemonic, membersOf, connection, logger);
            adminCollection.setCommit(null);
            adminCollection.processCollection();
            systemCollection = adminCollection.getCollectCollection();
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                System.out.println("AdminInit rollback 4");
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException)ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    public void buildSLACollection()
        throws TException
    {
        try {
            System.out.println("***Start buildSLACollection\n");
            Identifier systemClassesID = initList.get("systemClasses");
            Identifier ownerOwnerID = initList.get("ownerOwner");
            Identifier slaCollectionID = initList.get("slaCollection");
            String name = "Merritt service level agreement collection";
            String mnemonic = "mrt_sla_col";
            ArrayList<Identifier> membersOf = new ArrayList<>();
            membersOf.add(systemClassesID); 
            
            AdminCollection adminCollection = AdminCollection.getAdminCollection(
                    true, slaCollectionID, ownerOwnerID, name, mnemonic, membersOf, connection, logger);
            adminCollection.setCommit(null);
            adminCollection.processCollection();
            slaCollection = adminCollection.getCollectCollection();
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                System.out.println("AdminInit rollback 5");
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException)ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    
    public void buildOwnerCollection()
        throws TException
    {
        try {
            System.out.println("***Start buildOwnerCollection\n");
            Identifier systemClassesID = initList.get("systemClasses");
            Identifier ownerOwnerID = initList.get("ownerOwner");
            Identifier ownerCollectionID = initList.get("ownerCollection");
            String name = "Merritt owners";
            String mnemonic = "mrt_owners";
            ArrayList<Identifier> membersOf = new ArrayList<>();
            membersOf.add(systemClassesID); 
            
            AdminCollection adminCollection = AdminCollection.getAdminCollection(
                    true, ownerCollectionID, ownerOwnerID, name, mnemonic, membersOf, connection, logger);
            adminCollection.setCommit(null);
            adminCollection.processCollection();
            ownerCollection = adminCollection.getCollectCollection();
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                System.out.println("AdminInit rollback 6");
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException)ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    
    public void buildCollectionCollection()
        throws TException
    {
        try {
            System.out.println("***Start buildCollectionCollection\n");
            Identifier systemClassesID = initList.get("systemClasses");
            Identifier ownerOwnerID = initList.get("ownerOwner");
            Identifier ownerCollectionID = initList.get("collectionCollection");
            String name = "Merritt curatorial classes";
            String mnemonic = "mrt_curatorial_classes";
            ArrayList<Identifier> membersOf = new ArrayList<>();
            membersOf.add(systemClassesID); 
            
            AdminCollection adminCollection = AdminCollection.getAdminCollection(
                    true, ownerCollectionID, ownerOwnerID, name, mnemonic, membersOf, connection, logger);
            adminCollection.setCommit(null);
            adminCollection.processCollection();
            collectionCollection = adminCollection.getCollectCollection();
            if (collectionCollection.getRespStatus() == null) {
                collectionCollection.setRespStatus("ok");
            }
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                System.out.println("AdminInit rollback 7");
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException)ex;
            } else {
                throw new TException(ex);
            }
        }
    }
    
    
    
    /*
    protected void handleCommit()
    {
        if (commit) {
                connection.commit();
                newOwner.setRespStatus("commit");
            } else {
                connection.rollback();
                newOwner.setRespStatus("rollback");
    
    }
*/

    public void addOwnerOwnerToCollections()
        throws TException
    {
        try {
            ArrayList<Identifier> toCollection = new ArrayList<>();
            toCollection.add(ownerCollection.getArk());
            toCollection.add(slaCollection.getArk());
            toCollection.add(systemCollection.getArk());
            addMembers(ownerOwnerObjectInv,toCollection);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                System.out.println("AdminInit rollback 8");
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
            log4j.error(ex.toString(), ex);
            if (ex instanceof TException) {
                throw (TException) ex;
            } else {
                throw new TException(ex);
            }
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
    
    public JSONObject getBuildResponseJson() {
        return buildResponseJson;
    }
    
}
