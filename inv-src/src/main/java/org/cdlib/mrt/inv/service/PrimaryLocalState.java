/*
Copyright (c) 2005-2016, Regents of the University of California
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
**********************************************************/
package org.cdlib.mrt.inv.service;


import org.cdlib.mrt.inv.content.*;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class PrimaryLocalState
        implements StateInf
{
    private static final String NAME = "PrimaryLocalState";
    private static final String MESSAGE = NAME + ": ";
    

    public Long id = 0L;
    public Identifier objectArk = null;
    public Identifier ownerArk = null;
    public String localID = null;
    public DateState created = null;
    
    public static PrimaryLocalState getPrimaryLocalState(
            InvLocalID invLocalID)
        throws TException
    {
        return new PrimaryLocalState(invLocalID);
    }
    
    public PrimaryLocalState(
        InvLocalID invLocalID)
        throws TException
    {
        setId(invLocalID.getId());
        setObjectArk(invLocalID.getObjectArk());
        setOwnerArk(invLocalID.getOwnerArk());
        setLocalID(invLocalID.getLocalID());
        setCreated(invLocalID.getCreated());
        validateFullMap();
    }
    
    private void validateFullMap()
        throws TException
    {
        if (getObjectArk() == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "objectArk missing");
        }
        if (getOwnerArk() == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "ownerArk missing");
        }
        if (StringUtil.isAllBlank(getLocalID())) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "localID missing");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        if (id == 0) {
            this.id = null;
        } else {
            this.id = id;
        }
    }

    public Identifier getObjectArk() {
        return objectArk;
    }

    public void setObjectArk(Identifier objectArk) {
        this.objectArk = objectArk;
    }

    public Identifier getOwnerArk() {
        return ownerArk;
    }

    public void setOwnerArk(Identifier ownerArk) {
        this.ownerArk = ownerArk;
    }

    public String getLocalID() {
        return localID;
    }

    public void setLocalID(String localID) {
        this.localID = localID;
    }
    
    public DateState getCreated()
    {
        return created;
    }

    public void setCreated(DateState created) {
        this.created = created;
    }
    
    public String dump(String header)
        throws TException
    {
        if (getId() == null) id = 0L;
        StringBuffer buf = new StringBuffer(header + "\n"
            + " - id:" + id
            + " - objectArk:" + getObjectArk().getValue()
            + " - ownerArk:" + getOwnerArk().getValue()
            + " - localID:" + getLocalID()
                );
        return buf.toString();
    }
}

