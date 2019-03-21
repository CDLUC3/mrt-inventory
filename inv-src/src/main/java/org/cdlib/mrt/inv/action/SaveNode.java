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
package org.cdlib.mrt.inv.action;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.extract.StoreState;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class SaveNode
        extends InvActionAbs
{

    protected static final String NAME = "SaveNode";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;

    protected InvNode sourceInvNode = null;
    protected InvNode canInvNode = null;
    protected int nodeNumber = 0;
    protected DBAdd dbAdd = null;
    protected String storageBase = null;
    
    public static SaveNode getSaveNode(
            int nodeNumber,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new SaveNode(nodeNumber, connection, null, logger);
    }
    
    public static SaveNode getSaveNode(
            int nodeNumber,
            Connection connection,
            String storageBase,
            LoggerInf logger)
        throws TException
    {
        return  new SaveNode(nodeNumber, connection, storageBase, logger);
    }
    
    protected SaveNode(
            int nodeNumber,
            Connection connection,
            String storageBase,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        try {
            this.nodeNumber = nodeNumber;
            this.storageBase = storageBase;
            dbAdd = new DBAdd(connection, logger);
            validate();
        
        } catch (Exception ex) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ex2) { }
            
            if (ex instanceof TException) {
                throw (TException) ex;
            }
            else throw new TException(ex);
        }
    }
    
    private void validate()
        throws TException
    {
        if (connection == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "connection missing");
        }
        try {
            boolean valid = connection.isValid(20);
            if (!valid) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "invalid connection");
            }
        } catch (Exception ex) {
            throw new TException(ex);
        }
        if (nodeNumber < 1) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "nodeNumber invalid:"  + nodeNumber);
        }
        extractNode();
    }
    
    protected void extractNode()
        throws TException
    {
        try {
            sourceInvNode = InvDBUtil.getNode(nodeNumber, connection, logger);
            if (sourceInvNode == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "Existing db invNode required for this process");
            }
            
            String baseUrl = storageBase;
            if (baseUrl == null) {
                baseUrl = sourceInvNode.getBaseURL();
            }
            System.out.println("***SaveNode:"
                    + " - baseUrl=" + baseUrl
                    + " - nodeNumber=" + nodeNumber
            );
            StoreState storeState = StoreState.getStoreState(baseUrl, nodeNumber, logger);
            canInvNode = new InvNode(logger);
            canInvNode.setState(storeState);
            canInvNode.setBaseURL(baseUrl);
            canInvNode.setId(sourceInvNode.getId());
            System.out.println(PropertiesUtil.dumpProperties("canInvNode", canInvNode.retrieveProp()));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }
    
    public void resetInvNode()
        throws TException
    {
        try {
            System.out.println("resetInvNode entered");
            dbAdd.update(canInvNode);
            this.connection.commit();

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (Exception testex) { }
            if (ex instanceof TException) {
                throw (TException) ex;
            }
            throw new TException(ex);
            
        } finally {
            if (connection != null) {
                try {
                    this.connection.close();
                } catch (Exception ex) { }
            }
        }
    }
    
    public Properties getCanProp()
        throws TException
    {
        if (canInvNode == null) {
            return null;
        }
        return canInvNode.retrieveProp();
    }

}

