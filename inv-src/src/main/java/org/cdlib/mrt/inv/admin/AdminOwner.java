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
import java.util.List;
import java.util.Properties;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;


/**
 * Run fixity
 * @author dloy
 */
public class AdminOwner
        extends AdminShare
{

    protected static final String NAME = "AdminOwner";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
        
    protected Identifier ownerID = null;
    protected Identifier ownerArkOwnerID = null;
    protected String name = null;
    //protected String mnemonic = null;
    protected List<Identifier> members = null;
            
    protected int node = 0;
    protected int toNode = 0;
    protected long objectseq = 0;
    protected HashMap<String,String> mimeList = new HashMap<>();
    protected InvOwner invOwner = null;
    protected InvObject ownerObject = null;
    protected InvOwner newOwner = null;
    
    public static AdminOwner getAdminOwner(
            Identifier ownerID,
            Identifier ownerArkOwnerID,
            String name,
            String membersS,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        ArrayList<Identifier> members = getMembers(membersS);
        return new AdminOwner(ownerID, ownerArkOwnerID, name, members, connection, logger);
    }
    
    public static AdminOwner getAdminOwner(
            Identifier ownerID,
            Identifier ownerArkOwnerID,
            String name,
            List<Identifier> members,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AdminOwner(ownerID, ownerArkOwnerID, name, members, connection, logger);
    }
    
    public AdminOwner(
            Identifier ownerID,
            Identifier ownerArkOwnerID,
            String name,
            List<Identifier> members,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(AdminShare.AdminType.sla, connection, logger);
        this.ownerID = ownerID;
        this.ownerArkOwnerID = ownerArkOwnerID;
        this.name = name;
        this.members = members;
    }
    
    
    
    public void processOwner()
        throws TException
    {
        try {
            validateOwner();
            testExists();
            System.out.println("after testExists");
            add();
            if (commit) {
                connection.commit(); 
            } else {
                connection.rollback();
            }
            System.out.println("!commit:" + commit);
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
        }
    }

    protected void validateOwner()
        throws TException
    {
        validObjectID(ownerID,"ownerID");
        validObjectID(ownerArkOwnerID,"ownerArkOwnerID");
        validString(name, "name");
        validMembers(members);
        validConnect(connection);
    }
    
    protected void testExists()
        throws TException
    {
        invOwner = getOwner(ownerArkOwnerID, connection, logger);
        System.out.println(invOwner.dump("testExists invOwner"));
        newOwner = getOwner(ownerID, connection, logger);
        if (newOwner == null) {
            System.out.println("newOwner null");
        } else {
            System.out.println(newOwner.dump("testExists Owner"));
        }
    }
    
    public void add()
        throws TException
    {
        System.out.println("add entered");
        try {
            if (invOwner == null) {
                throw new TException.INVALID_OR_MISSING_PARM("Required owner missing:" + ownerArkOwnerID.getValue());
            }
            if (newOwner != null) {
                throw new TException.INVALID_OR_MISSING_PARM("Add process and slaCollection exists:" + ownerID.getValue());
            }
            addOwnerObject();
            addOwner();
            addMembers(ownerObject, members);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    public void addOwner()
        throws TException
    {
        System.out.println("addOwner entered");
        try {
            
            newOwner = new InvOwner(logger);
            newOwner.setObjectID(ownerObject.getId());
            newOwner.setArk(ownerID);
            newOwner.setName(name);
            System.out.println(newOwner.dump("***newOwner dump***"));
            
            long ownerseq = dbAdd.insert(newOwner);
            newOwner.setId(ownerseq);
            System.out.println(newOwner.dump("owner build"));
            String msg = newOwner.dump("addOwner");
            System.out.println(msg);
            log4j.debug(msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    public void addOwnerObject()
        throws TException
    {
        System.out.println("addOwnerObject entered");
        try {
            Properties objectProp = loadProperties("owner_ark");
            
            System.out.println(PropertiesUtil.dumpProperties("Load object prop", objectProp));
            
            ownerObject = new InvObject(objectProp, logger);
            ownerObject.setOwnerID(invOwner.getId());
            ownerObject.setArk(ownerID);
            ownerObject.setWhat(name + " owner object");
            ownerObject.setWhere(ownerID.getValue() +  " ; (:unas)");
            ownerObject.setModified();
            ownerObject.setAggregateRole(InvObject.AggregateRole.mrtOwner);
            ownerObject.setType(InvObject.Type.mrtSystem);
            ownerObject.setRole(InvObject.Role.mrtClass);
            //createObject.setVersionNumber(0);
            System.out.println(ownerObject.dump("build"));
            //
            objectseq = dbAdd.insert(ownerObject);
            ownerObject.setId(objectseq);
            String msg = ownerObject.dump("addOwnerObject");
            System.out.println(msg);
            log4j.debug(msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    

    public InvObject getOwnerObject() {
        return ownerObject;
    }

    public InvOwner getNewOwner() {
        return newOwner;
    }
    
}
