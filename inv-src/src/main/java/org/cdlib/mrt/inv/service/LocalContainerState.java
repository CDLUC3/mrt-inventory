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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvLocalID;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.utility.TException;

/**
 *
 * @author dloy
 */
public class LocalContainerState
        implements StateInf
{
    protected static final String NAME = "LocalContainerState";
    protected static final String MESSAGE = NAME + ": ";

    protected Identifier primaryIdentifier = null;
    protected Identifier ownerID = null;
    protected String localIDs = null;
    protected DateState  timestamp = new DateState();
    protected Long deleteCnt = null;
    protected Boolean match = null;
    protected Boolean exists = null;
    protected Vector<PrimaryLocalState> localList = new Vector();
    protected Vector<PrimaryLocalState> local = null;
    
    public static LocalContainerState retrieveEmptyLocalContainerState()
        throws TException
    {
        return new LocalContainerState();
    }

    public LocalContainerState() { }
    
    public static LocalContainerState buildLocalContainerState(
            Identifier primaryIdentifier,
            Identifier ownerID,
            String localIDs,
            boolean match,
            List<InvLocalID> invLocalIDList)
        throws TException
    {
        return new LocalContainerState(
                primaryIdentifier,
                ownerID,
                localIDs,
                match, 
                invLocalIDList);
    }
    
    public LocalContainerState(
            Identifier primaryIdentifier,
            Identifier ownerID,
            String localIDs,
            boolean match,
            List<InvLocalID> invLocalIDList)
        throws TException
    {
        this.primaryIdentifier = primaryIdentifier;
        this.ownerID = ownerID;
        this.localIDs = localIDs;
        this.match = match;
        addLocalState(invLocalIDList);
        local = null;
        
    }
    
    public static LocalContainerState buildLocalContainerState(
            Identifier primaryIdentifier,
            Identifier ownerID,
            Identifier collectionID,
            String localIDs,
            List<InvLocalID> invLocalIDList)
        throws TException
    {
        return new LocalContainerState(
                primaryIdentifier,
                ownerID,
                localIDs,
                invLocalIDList);
    }
    
    public LocalContainerState(
            Identifier primaryIdentifier,
            Identifier ownerID,
            String localIDs,
            List<InvLocalID> invLocalIDList)
        throws TException
    {
        this.primaryIdentifier = primaryIdentifier;
        this.ownerID = ownerID;
        this.localIDs = localIDs;
        addLocalState(invLocalIDList);
        local = localList;
    }
    
    public static LocalContainerState buildLocalContainerState(
            Identifier primaryIdentifier,
            List<InvLocalID> invLocalIDList)
        throws TException
    {
        return new LocalContainerState(primaryIdentifier, invLocalIDList);
    }
    
    public LocalContainerState(
            Identifier primaryIdentifier,
            List<InvLocalID> invLocalIDList)
        throws TException
    {
        this.primaryIdentifier = primaryIdentifier;
        addLocalState(invLocalIDList);
        local = localList;
    }

    private void addLocalState(List<InvLocalID> invLocalIDList)
        throws TException
    {
        if (invLocalIDList == null) {
            localList = new Vector();
            return;
        }
         
        for (InvLocalID invLocalID : invLocalIDList) {
            PrimaryLocalState primaryState = PrimaryLocalState.getPrimaryLocalState(invLocalID);
            localList.add(primaryState);
        }
    }


    public DateState getTimestamp() {
        return timestamp;
    }

    public Identifier getPrimaryIdentifier() {
        return primaryIdentifier;
    }

    public Identifier getOwnerID() {
        return ownerID;
    }

    public String getLocalIDs() {
        return localIDs;
    }
    
    public boolean isExists() {
        if (exists != null) return exists;
        if (localList.size() > 0) return true;
        return false;
    }

    public int getCountLocalIDs()
    {
        return localList.size();
    }

    public Boolean getMatch() {
        return match;
    }

    public Long getDeleteCnt() {
        return deleteCnt;
    }

    public Vector<PrimaryLocalState> getLocal()
    {
        if (local.size() == 0) return null;
        return local;
    }

    public Vector<PrimaryLocalState> retrieveLocalList()
    {
        if (localList.size() == 0) return null;
        return localList;
    }

    public void setPrimaryIdentifier(Identifier primaryIdentifier) {
        this.primaryIdentifier = primaryIdentifier;
    }

    public void setPrimaryIdentifier(String primaryIdentifierS) 
        throws TException
    {
        if (StringUtil.isAllBlank(primaryIdentifierS)) primaryIdentifier = null;
        this.primaryIdentifier = new Identifier(primaryIdentifierS);
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void setDeleteCnt(Long deleteCnt) {
        this.deleteCnt = deleteCnt;
    }

    public void setOwnerID(Identifier ownerID) {
        this.ownerID = ownerID;
    }

    public void setLocalIDs(String localIDs) {
        this.localIDs = localIDs;
    }

    public String dump(String header)
        throws TException
    {
        StringBuffer buf = new StringBuffer();
        buf.append(header);
        int i=0;
        buf.append("\nLocalContainerState - Primary:" );
        if (primaryIdentifier == null) {
            buf.append("empty");
                    
        } else {
            buf.append(primaryIdentifier.getValue());
        }
        buf.append("\nsize:" + getCountLocalIDs());
        buf.append("\nisExists:" + isExists());
        for (PrimaryLocalState id: localList) {
            i++;
            buf.append(id.dump("\n***>(" + i + ")<***"));
        }
        return buf.toString();
    }
}

