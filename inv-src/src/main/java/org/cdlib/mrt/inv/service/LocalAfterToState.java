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
  contributors may be used to endorse or promote products derived after
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

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.utility.TException;

/**
 *
 * @author dloy
 */
public class LocalAfterToState
        implements StateInf
{
    protected static final String NAME = "LocalContainerState";
    protected static final String MESSAGE = NAME + ": ";

    protected long after = 0L;
    protected long to = 0L;
    protected long added = 0L;
    protected long lastID = 0L;
    protected long existErrors = 0L;
    protected DateState  timestamp = new DateState();
    
    public static LocalAfterToState retrieveEmptyLocalAfterToState()
        throws TException
    {
        return new LocalAfterToState();
    }

    public LocalAfterToState() { }
    
    public static LocalAfterToState buildLocalAfterToState(
            long after,
            long to,
            long added,
            long lastID)
        throws TException
    {
        return new LocalAfterToState(
                after, to, added, lastID);
    }
    
    public static LocalAfterToState buildLocalAfterToState(
            long after,
            long to)
        throws TException
    {
        return new LocalAfterToState(
                after, to, 0L, 0L);
    }
    
    protected LocalAfterToState(
            long after,
            long to,
            long added,
            long lastID)
        throws TException
    {
        this.after = after;
        this.to = to;
        this.added = added;
        this.lastID = added;
        
    }


    public DateState getTimestamp() {
        return timestamp;
    }

    public long getAfter() {
        return after;
    }

    public void setAfter(long after) {
        this.after = after;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getAdded() {
        return added;
    }

    public void bumpAdded() {
        this.added++;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public long getLastID() {
        return lastID;
    }

    public void setLastID(long lastID) {
        this.lastID = lastID;
    }

    public long getExistErrors() {
        return existErrors;
    }

    public void bumpExistErrors() {
        this.existErrors++;
    }

    public void setExistErrors(long existErrors) {
        this.existErrors = existErrors;
    }
}