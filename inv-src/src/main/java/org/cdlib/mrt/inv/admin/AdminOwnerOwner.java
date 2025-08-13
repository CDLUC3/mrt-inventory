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
public class AdminOwnerOwner
        extends AdminShare
{

    protected static final String NAME = "AdminInit";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean DUMPTALLY = false;
    protected static final boolean EACHTALLY = true;
        
    protected Identifier ownerID = null;
    protected String name = null;
            
    protected int node = 0;
    protected int toNode = 0;
    protected long objectseq = 0;
    protected HashMap<String,String> mimeList = new HashMap<>();
    protected InvOwner invOwner = null;
    protected InvObject ownerObject = null;
    //protected InvOwner invOwner = null;
    
    public static AdminOwnerOwner getAdminOwnerOwner(
            Identifier ownerID,
            String name,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new AdminOwnerOwner(ownerID, name, connection, logger);
    }
    
    public AdminOwnerOwner(
            Identifier ownerID,
            String name,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(AdminShare.AdminType.sla, connection, logger);
        this.ownerID = ownerID;
        this.name = name;
    }
    
    
    
    public void buildOwner()
        throws TException
    {
        try {
            invOwner = getOwner(ownerID, connection, logger);
            ownerObject = getObject(ownerID, connection, logger);
            addOwner();
            addOwnerObject();
            if (invOwner.getObjectID() < 1) {
                invOwner.setObjectID(ownerObject.getId());
            }
            
                        
            if (commit == null) {
                System.out.println("AdminOwnerOwner wait commit");
                
            } else if (commit) {
                connection.commit();
                invOwner.setRespStatus("commit");
                System.out.println("AdminOwnerOwner commit.1");
                
            } else {
                connection.rollback();
                invOwner.setRespStatus("rollback");
                System.out.println("AdminOwnerOwner rollback");
            }
            System.out.println("!commit:" + commit);
            
        } catch (Exception ex) {
            try {
                connection.rollback();
                log4j.info("Exception rollback:" + ex);
                System.out.println("AdminOwnerOwner rollback 3");
                
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
    
    public void addOwner()
        throws TException
    {
        log4j.debug("addOwnerOne entered");
        if (invOwner != null) {
            invOwner.setRespStatus("exists");
            log4j.info(invOwner.dump("invOwnerOwner exists"));
            return;
        }
        try {
            
            invOwner = new InvOwner(logger);
            //invOwner.setObjectID(ownerObject.getId());
            invOwner.setArk(ownerID);
            invOwner.setName(name);
            
            
            long ownerseq = dbAdd.insert(invOwner);
            invOwner.setId(ownerseq);
            invOwner.setRespStatus("ok");
            String msg = invOwner.dump("addOwner");
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
        log4j.debug("addOwnerObject entered");
        if (ownerObject != null) {
            ownerObject.setRespStatus("exists");
            String msg = ownerObject.dump("ownerObject exists");
            System.out.println(msg);
            log4j.info(msg);
            return;
        }
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
            //System.out.println(ownerObject.dump("build"));
            //
            objectseq = dbAdd.insert(ownerObject);
            ownerObject.setId(objectseq);
            ownerObject.setRespStatus("ok");
            String msg = ownerObject.dump("ownerObject built");
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
        return invOwner;
    }
    
}
