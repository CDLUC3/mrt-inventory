

/*********************************************************************
    Copyright 2003 Regents of the University of California
    All rights reserved
*********************************************************************/

package org.cdlib.mrt.inv.admin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.sql.Connection;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.inv.service.InventoryConfig;

import org.json.JSONObject;


public class TestAdminConfig
{
    private static final String NAME = "TestAdminObject";    
    
    
     
    public static void main(String[] argv) { // owner
        
       main_config(argv);
        
      
    }
     
     
    public static void main_config(String[] argv) { // owner
        

        InventoryConfig config = null;
        Connection connection = null;
        LoggerInf logger = null;
    	try {
            
            config = InventoryConfig.useYaml();
            LinkedHashMap<String,String> map = config.getAdminMap();
            Set<String> keys = map.keySet();
            for (String key : keys) {
                String val = map.get(key);
                System.out.println(key + " = " + val);
            }
 
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (Exception ex)  { }
            
        
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