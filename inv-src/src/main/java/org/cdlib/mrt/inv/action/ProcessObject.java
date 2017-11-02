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

import java.sql.Connection;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.service.InvProcessState;
import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class ProcessObject
        extends InvActionAbs
        implements Runnable
{

    protected static final String NAME = "ProcessObject";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    
    Role role = null;
    Role copyRole = null;
    SaveObject saveObject = null;
    boolean doCheckVersion = true;
    
    public static ProcessObject getProcessObject(
            Role role,
            Role copyRole,
            String manifestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new ProcessObject(role, copyRole, manifestURL, connection, logger);
    }
    
    protected ProcessObject(
            Role role,
            Role copyRole,
            String manifestURL,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        this.role = role;
        this.copyRole = copyRole;
        this.saveObject = SaveObject.getSaveObject(
            manifestURL,
            connection,
            role,
            copyRole,
            logger);
    }

    public void run()
    {

    }

    public void process()
        throws TException
    {
        this.saveObject.process(doCheckVersion);
        return;
    }
    
    public InvProcessState getState()
    {
        return saveObject.getProcessState();
    }

    public boolean isDoCheckVersion() {
        return doCheckVersion;
    }

    public void setDoCheckVersion(boolean doCheckVersion) {
        this.doCheckVersion = doCheckVersion;
    }
    
    
}

