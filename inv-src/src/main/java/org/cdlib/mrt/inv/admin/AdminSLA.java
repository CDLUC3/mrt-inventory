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
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.content.InvOwner;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.service.Role;
//import org.cdlib.mrt.queue.DistributedLock.Ignorer;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.log.utility.AddStateEntryGen;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TallyTable;
import org.cdlib.mrt.utility.TException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.cdlib.mrt.inv.admin.AdminShare.AdminType.sla;
import static org.cdlib.mrt.inv.admin.AdminShare.getMembers;


/**
 * Run fixity
 * @author dloy
 */
public class AdminSLA
        extends AdminShare
{

    protected static final String NAME = "AdminSLA";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
        
    protected Identifier slaID = null;
    protected Identifier ownerID = null;
    protected String name = null;
    protected String mnemonic = null;
    protected List<Identifier> members = null;
            
    protected Boolean commit = false;
    protected int node = 0;
    protected int toNode = 0;
    protected long objectseq = 0;
    protected HashMap<String,String> mimeList = new HashMap<>();
    protected InvOwner invOwner = null;
    protected InvObject slaObject = null;
    protected InvCollection slaCollection = null;
    
    public static AdminSLA getAdminSLA(
            Identifier slaID,
            Identifier ownerID,
            String name,
            String mnemonic,
            String membersS,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        ArrayList<Identifier> members = getMembers(membersS);
        return new AdminSLA(slaID, ownerID, name, mnemonic, members, connection, logger);
    }
    
    public static AdminSLA getAdminSLA(
            Identifier slaID,
            Identifier ownerID,
            String name,
            String mnemonic,
            List<Identifier> members,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AdminSLA(slaID, ownerID, name, mnemonic, members, connection, logger);
    }
    
    public AdminSLA(
            Identifier slaID,
            Identifier ownerID,
            String name,
            String mnemonic,
            List<Identifier> members,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(AdminShare.AdminType.sla, connection, logger);
        this.slaID = slaID;
        this.ownerID = ownerID;
        this.name = name;
        this.mnemonic = mnemonic;
        this.members = members;
    }
    
    
    
    public void processSla()
        throws TException
    {
        try {
            validateSla();
            testExists();
            System.out.println("after testExists");
            add();
            if (commit) connection.commit();
            else connection.rollback();
            
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

    protected void validateSla()
        throws TException
    {
        validObjectID(slaID,"slaID");
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
        slaCollection = getCollection(slaID, connection, logger);
        if (slaCollection == null) {
            System.out.println("SlaCollection null");
        } else {
            System.out.println(slaCollection.dump("testExists sla"));
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
            if (slaCollection != null) {
                throw new TException.INVALID_OR_MISSING_PARM("Add process and slaCollection exists:" + slaID.getValue());
            }
            addCollectionObject();
            addCollection();
            addMembers(slaObject, members);
            
        } catch (TException tex) {
            tex.printStackTrace();
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
            Properties slaCollectionProp = loadProperties("sla");
            
            System.out.println(PropertiesUtil.dumpProperties("Load collection prop", slaCollectionProp));
            slaCollection = new InvCollection(slaCollectionProp, logger);
            slaCollection.setObjectID(slaObject.getId());
            slaCollection.setArk(slaID);
            slaCollection.setName(name);
            slaCollection.setMnemonic(mnemonic);
            System.out.println(slaCollection.dump("***sla collection dump***"));
            
            long collectseq = dbAdd.insert(slaCollection);
            slaCollection.setId(collectseq);
            System.out.println(slaCollection.dump("build"));
            String msg = slaCollection.dump("addCollection");
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
            Properties objectProp = loadProperties("sla_ark");
            
            System.out.println(PropertiesUtil.dumpProperties("Load object prop", objectProp));
            
            slaObject = new InvObject(objectProp, logger);
            slaObject.setOwnerID(invOwner.getId());
            slaObject.setArk(slaID);
            slaObject.setWhat(name);
            slaObject.setWhere(slaID.getValue() +  " ; (:unas)");
            slaObject.setModified();
            slaObject.setAggregateRole(InvObject.AggregateRole.mrtServiceLevelAgreement);
            slaObject.setType(InvObject.Type.mrtSystem);
            slaObject.setRole(InvObject.Role.mrtClass);
            //createObject.setVersionNumber(0);
            System.out.println(slaObject.dump("build"));
            //
            objectseq = dbAdd.insert(slaObject);
            slaObject.setId(objectseq);
            String msg = slaObject.dump("addCollectionObject");
            System.out.println(msg);
            log4j.debug(msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(e);
        }
    }

    public AdminSLA setCommit(Boolean commit) {
        this.commit = commit;
        System.out.println("setCommit called:" + commit);
        return this;
    }

    public Boolean getCommit() {
        return commit;
    }

    public InvObject getSlaObject() {
        return slaObject;
    }

    public InvCollection getSlaCollection() {
        return slaCollection;
    }
    
}
