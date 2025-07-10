

/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.admin;

import java.util.ArrayList;
import java.sql.Connection;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.service.InventoryConfig;

import org.json.JSONObject;


public class TestAdminMulti
{
    private static final String NAME = "TestAdminObject";    
    
    
     
    public static void main(String[] argv) { // owner
        
       
        InventoryConfig config = null;
        Connection connection = null;
        LoggerInf logger = null;
    	try {
            
            config = InventoryConfig.useYaml();
            config.dbStartup();
            connection = config.getConnection(false);
            logger = config.getLogger();
            //buildSLA(connection, logger);
            //buildOwner(connection, logger, true);
            //buildCollectPub(connection, logger, false);
            buildCollectPrv(connection, logger, false);
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
                
        } finally {
            close(connection);
        } 
      
    }
    
    public static void buildCollectPrv(Connection connection, LoggerInf logger, Boolean commit)     
    {
        
    	try {
            
            Identifier collectCollection  = new Identifier("ark:/13030/j27p88qw"); // Collection collection
            String membersS = collectCollection.getValue();
            System.out.println("memberS=" + membersS);
            Identifier newCollectionID = new Identifier("ark:/99999/testcollectprv"); // new Owner ID
            Identifier ownerOwnerID = new Identifier("ark:/13030/j2rn30xp"); // Owner Merritt Admin
            String name = "test admin Collection private";
            String mnemonic = "ta-collectprv";
            AdminCollection adminCollection = AdminCollection.getAdminCollectionPrivate(newCollectionID, 
                    ownerOwnerID, name, mnemonic, membersS, connection, logger)
                .setCommit(commit);
            
            adminCollection.processCollection();
            InvCollection respCollection = adminCollection.getCollectCollection();
            JSONObject respjson = respCollection.dumpJson(name);
            System.out.println(respjson.toString(2));
          
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        } 
    }   
    
    public static void buildCollectPub(Connection connection, LoggerInf logger, Boolean commit)     
    {
        
    	try {
            
            Identifier collectCollection  = new Identifier("ark:/13030/j27p88qw"); // Collection collection
            String membersS = collectCollection.getValue();
            System.out.println("memberS=" + membersS);
            Identifier newCollectionID = new Identifier("ark:/99999/testcollectpub"); // new Owner ID
            Identifier ownerOwnerID = new Identifier("ark:/13030/j2rn30xp"); // Owner Merritt Admin
            String name = "test admin Collection public";
            String mnemonic = "ta-collectpub";
            AdminCollection adminCollection = AdminCollection.getAdminCollectionPublic(newCollectionID, 
                    ownerOwnerID, name, mnemonic, membersS, connection, logger)
                .setCommit(commit);
            
            adminCollection.processCollection();
 
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        } 
    }
     
    public static void buildOwner(Connection connection, LoggerInf logger, Boolean commit) 
    { 
        try {
            String  processTypeS = "owner";
            Identifier ownerCollection  = new Identifier("ark:/13030/j2cc0900"); // Owner collection
            Identifier slaID = new Identifier("ark:/99999/testsla"); // CDL UC3
            String membersS = slaID.getValue() + ";" + ownerCollection.getValue();
            System.out.println("memberS=" + membersS);
            Identifier newOwnerID = new Identifier("ark:/99999/testowner"); // new Owner ID
            Identifier ownerOwnerID = new Identifier("ark:/13030/j2rn30xp"); // Owner Merritt Admin
            String name = "test admin Owner";

            AdminOwner adminOwner = AdminOwner.getAdminOwner(newOwnerID, 
                    ownerOwnerID, name, membersS, connection, logger)
                .setCommit(commit);
            adminOwner.processOwner();


        } catch (Exception ex) {
                // TODO Auto-generated catch block

                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        }
      
    }
    
    public static void buildSLA(Connection connection, LoggerInf logger) 
    {
        
    	try {
            Identifier slaID = new Identifier("ark:/99999/testsla"); // CDL UC3
            Identifier ownerID = new Identifier("ark:/13030/j2rn30xp"); // Owner Merritt Admin
            String name = "test admin SLA";
            String mnemonic = "ta-sla";
            String mem1S = "ark:/13030/j2h41690"; //Merritt service level agreements
            Identifier mem1 = new Identifier(mem1S);
            ArrayList<Identifier> members = new ArrayList<>();
            members.add(mem1);
            AdminSLA adminSLA = AdminSLA.getAdminSLA(slaID,ownerID, name, mnemonic, members, connection, logger)
                .setCommit(true);
            adminSLA.processSla();
 
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
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
    private static void log(String msg)
    {
        System.out.println(msg);
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