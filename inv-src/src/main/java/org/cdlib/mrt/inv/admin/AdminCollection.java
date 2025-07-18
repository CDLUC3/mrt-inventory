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
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;


/**
 * Run fixity
 * @author dloy
 */
public class AdminCollection
        extends AdminShare
{

    protected static final String NAME = "AdminCollection";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
        
    protected Identifier collectID = null;
    protected Identifier ownerID = null;
    protected String name = null;
    protected String mnemonic = null;
    protected List<Identifier> members = null;
    protected CollectionType collectionType = CollectionType.collection_private;
            
    protected int node = 0;
    protected int toNode = 0;
    protected long objectseq = 0;
    protected HashMap<String,String> mimeList = new HashMap<>();
    protected InvOwner invOwner = null;
    protected InvObject collectObject = null;
    protected InvCollection collectCollection = null;
    
    
    public static AdminCollection getAdminCollectionPublic(
            Identifier collectID,
            Identifier ownerID,
            String name,
            String mnemonic,
            String membersS,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        
        ArrayList<Identifier> members = getMembers(membersS);
        AdminCollection adminCollection = new AdminCollection(collectID, ownerID, name, mnemonic, members, CollectionType.collection_public, connection, logger);
        return adminCollection;
    }
    
    public static AdminCollection getAdminCollectionPrivate(
            Identifier collectID,
            Identifier ownerID,
            String name,
            String mnemonic,
            String membersS,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        
        ArrayList<Identifier> members = getMembers(membersS);
        AdminCollection adminCollection = new AdminCollection(collectID, ownerID, name, mnemonic, members, CollectionType.collection_private, connection, logger);
        return adminCollection;
    }
    
    public static AdminCollection getAdminCollection(
            Identifier collectID,
            Identifier ownerID,
            String name,
            String mnemonic,
            List<Identifier> members,
            CollectionType collectionType,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AdminCollection(collectID, ownerID, name, mnemonic, members, collectionType, connection, logger);
    }
    
    public static AdminCollection getAdminCollection(
            boolean privateCollection,
            Identifier collectID,
            Identifier ownerID,
            String name,
            String mnemonic,
            List<Identifier> members,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        CollectionType collectionType = null;
        if (privateCollection) collectionType = CollectionType.collection_private;
        else collectionType = CollectionType.collection_public;
        
        return new AdminCollection(collectID, ownerID, name, mnemonic, members, collectionType, connection, logger);
    }
    
    public AdminCollection(
            Identifier collectID,
            Identifier ownerID,
            String name,
            String mnemonic,
            List<Identifier> members,
            CollectionType collectionType,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(AdminShare.AdminType.collection, connection, logger);
        this.collectID = collectID;
        this.ownerID = ownerID;
        this.name = name;
        this.mnemonic = mnemonic;
        this.members = members;
        this.collectionType = collectionType;
        System.out.println("AdminCollection:" + this.collectionType.toString());
    }
    
    
    
    public void processCollection()
        throws TException
    {
        try {
            validateCollect();
            testExists();
            add();
            if (collectCollection.getRespStatus() != null) {
                return;
            }
            if (commit) {
                connection.commit();
                collectCollection.setRespStatus("commit");
            } else {
                connection.rollback();
                collectCollection.setRespStatus("rollback");
            }
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                ex.printStackTrace();
                
            } catch (Exception rex) {
                String msg = "Rollback fails:" + rex;
                log4j.info("Rollback fails:" + rex);
                System.out.println(MESSAGE + msg);
            }
        }
    }

    protected void validateCollect()
        throws TException
    {
        validObjectID(collectID,"collectID");
        validObjectID(ownerID,"ownerID");
        validString(name, "name");
        validString(mnemonic, "mnemonic");
        validMembers(members);
        validConnect(connection);
    }
    
    protected void testExists()
        throws TException
    {
        invOwner = getOwner(ownerID, connection, logger);
        System.out.println(invOwner.dump("testExists invOwner"));
        collectCollection = getCollection(collectID, connection, logger);
        if (collectCollection == null) {
            log4j.debug("CollectCollection null");
        } else {
            log4j.debug(collectCollection.dump("testExists collect"));
        }
    }
    
    public void add()
        throws TException
    {
        System.out.println("add entered");
        try {
            if (invOwner == null) {
                throw new TException.INVALID_OR_MISSING_PARM("Required owner missing:" + ownerID.getValue());
            }
            if (collectCollection != null) {
                collectCollection.setRespStatus("exists");
                return;
            }
            addCollectionObject();
            addCollection();
            addMembers(collectObject, members);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    public void addCollection()
        throws TException
    {
        System.out.println("addCollection entered");
        try {
            
            Properties collectCollectionProp = loadProperties(collectionType.toString());
            
            System.out.println(PropertiesUtil.dumpProperties("Load collection prop", collectCollectionProp));
            collectCollection = new InvCollection(collectCollectionProp, logger);
            collectCollection.setObjectID(collectObject.getId());
            collectCollection.setArk(collectID);
            collectCollection.setName(name);
            collectCollection.setMnemonic(mnemonic);
            long collectseq = dbAdd.insert(collectCollection);
            collectCollection.setId(collectseq);
            String msg = collectCollection.dump("addCollection");
            System.out.println(msg);
            log4j.debug(msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    
    public void addCollectionObject()
        throws TException
    {
        System.out.println("addCollectionObject entered");
        try {
            Properties objectProp = loadProperties("collection_ark");
            
            System.out.println(PropertiesUtil.dumpProperties("Load object prop", objectProp));
            
            collectObject = new InvObject(objectProp, logger);
            collectObject.setOwnerID(invOwner.getId());
            collectObject.setArk(collectID);
            collectObject.setWhat(name);
            collectObject.setWhere(collectID.getValue() +  " ; (:unas)");
            collectObject.setModified();
            collectObject.setAggregateRole(InvObject.AggregateRole.mrtCollection);
            collectObject.setType(InvObject.Type.mrtCuratorial);
            collectObject.setRole(InvObject.Role.mrtClass);
            //createObject.setVersionNumber(0);
            System.out.println(collectObject.dump("build"));
            //
            objectseq = dbAdd.insert(collectObject);
            collectObject.setId(objectseq);
            String msg = collectObject.dump("addCollectionObject");
            System.out.println(msg);
            log4j.debug(msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }
    

    public InvOwner getInvOwner() {
        return invOwner;
    }

    public InvObject getCollectObject() {
        return collectObject;
    }

    public InvCollection getCollectCollection() {
        return collectCollection;
    }
}
