/*
Copyright (c) 2005-2010, Regents of the University of California
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
package org.cdlib.mrt.inv.content;


import java.util.Properties;

import org.cdlib.mrt.inv.extract.StoreCollections;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvCollectionObject
        extends ContentAbs
{
    private static final String NAME = "InvCollectionObject";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long collectionID = 0;
    public long objectID = 0;
    public boolean newEntry = false;
    
    
    
    public InvCollectionObject(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvCollectionObject(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setStoreOwner(long collectionID, long objectID)
        throws TException
    {
        this.collectionID = collectionID;
        this.objectID = objectID;
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setCollectionID(prop.getProperty("inv_collection_id"));
            setObjectID(prop.getProperty("inv_object_id"));
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getCollectionID() != 0) prop.setProperty("inv_collection_id", "" + getCollectionID());
        if (getObjectID() != 0) prop.setProperty("inv_object_id", "" + getObjectID());
        return prop;
    }
    
    public String getDBName()
    {
        return COLLECTIONS_OBJECTS;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(String idS) 
    {
        this.id = setNum(idS);
    }

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long objectID) {
        this.objectID = objectID;
    }

    public void setObjectID(String objectIDS) {
        this.objectID = setNum(objectIDS);
    }
    
    public long getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(long collectionID) {
        this.collectionID = collectionID;
    }

    public void setCollectionID(String collectionIDS) {
        this.collectionID = setNum(collectionIDS);
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
}

