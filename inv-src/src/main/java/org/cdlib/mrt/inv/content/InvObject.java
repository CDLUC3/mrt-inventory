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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.MessageDigestValue;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvObject
        extends ContentAbs
{
    private static final String NAME = "InvObject";
    private static final String MESSAGE = NAME + ": ";
    private static final int MAXW = 5394;
    protected static final boolean DEBUG = false;
    

    public long id = 0;
    public long ownerID = 0;
    public Identifier ark = null;
    //public long nodeNumber = 0;
    public String md5_3 = null;
    public long versionNumber = 0;
    public String who = null;
    public String what = null;
    public String when = null;
    public String where = null;
    public Type type = null;
    public Role role = null;
    public AggregateRole aggregateRole = null;
    public DateState created = null;
    public DateState modified = null;
    public boolean newEntry = false;
    
    public enum Type
    {
        mrtCuratorial("MRT-curatorial"),
        mrtSystem("MRT-system");

        protected final String dispType;
        Type(String dispType) {
            this.dispType = dispType;
        }
        public String toString()
        {
            return dispType;
        }

        public static Type getType(String t)
        {
            for (Type p : Type.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    public enum Role
    {
        mrtClass("MRT-class"),
        mrtContent("MRT-content");

        protected final String dbRole;
        Role(String dbRole) {
            this.dbRole = dbRole;
        }
        public String toString()
        {
            return dbRole;
        }

        public static Role getRole(String t)
        {
            for (Role p : Role.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    public enum AggregateRole
    {
        mrtNone("MRT-none"),
        mrtCollection("MRT-collection"),
        mrtOwner("MRT-owner"),
        mrtServiceLevelAgreement("MRT-service-level-agreement"),
        mrtSubmissionAgreement("MRT-submission-agreement");

        protected final String aggregateRole;
        AggregateRole(String aggregateRole) {
            this.aggregateRole = aggregateRole;
        }
        public String toString()
        {
            return aggregateRole;
        }

        public static AggregateRole getAggregateRole(String t)
        {
            for (AggregateRole p : AggregateRole.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return null;
        }
    }
    
    public InvObject(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvObject(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setOwnerID(prop.getProperty("inv_owner_id"));
            setArk(prop.getProperty("ark"));
            setMd5_3(prop.getProperty("md5_3"));
            if (getMd5_3() == null) { 
                buildMd5_3();
            }
            setType(prop.getProperty("object_type"));
            setAggregateRole(prop.getProperty("aggregate_role"));
            setRole(prop.getProperty("role"));
            setVersionNumber(prop.getProperty("version_number"));
            setWho(prop.getProperty("erc_who"));
            setWhat(prop.getProperty("erc_what"));
            setWhen(prop.getProperty("erc_when"));
            setWhere(prop.getProperty("erc_where"));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getOwnerID() != 0) prop.setProperty("inv_owner_id", "" + getOwnerID());
        if (getArk() != null) prop.setProperty("ark", getArk().getValue());
        if (getMd5_3() == null) { 
            buildMd5_3();
        }
        if (getMd5_3() != null) prop.setProperty("md5_3", getMd5_3());
        if (getType() != null) prop.setProperty("object_type", getType().toString());
        if (getRole() != null) prop.setProperty("role", getRole().toString());
        if (getAggregateRole() != null) prop.setProperty("aggregate_role", getAggregateRole().toString());
        prop.setProperty("version_number", "" + getVersionNumber());
        if (getWho() != null) prop.setProperty("erc_who", getWho());
        if (getWhen() != null) prop.setProperty("erc_what", getWhat());
        if (getWhen() != null) prop.setProperty("erc_when", getWhen());
        if (getWhere() != null) prop.setProperty("erc_where", getWhere());
        if (getType() != null) prop.setProperty("object_type", getType().toString());
        setModified();
        prop.setProperty("modified", getModifiedDB());
        return prop;
    }
    
    public String getDBName()
    {
        return OBJECTS;
    }

    public AggregateRole getAggregateRole() {
        return aggregateRole;
    }

    public void setAggregateRole(AggregateRole aggregateRole) {
        this.aggregateRole = aggregateRole;
    }

    public void setAggregateRole(String aggregateRoleS) {
        this.aggregateRole = AggregateRole.getAggregateRole(aggregateRoleS);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRole(String roleS) {
        this.role = Role.getRole(roleS);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setType(String typeS) {
        this.type = Type.getType(typeS);
    }

    public Identifier getArk() {
        return ark;
    }

    public void setArk(Identifier ark) {
        this.ark = ark;
    }

    public void setArk(String arkS)
        throws TException
    {
        if (StringUtil.isAllBlank(arkS)) return;
        this.ark = new Identifier(arkS);
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

    public long getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(long ownerID) {
        this.ownerID = ownerID;
    }

    public void setOwnerID(String ownerIDS) {
        this.ownerID = setNum(ownerIDS);
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(long versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setVersionNumber(String versionNumberS) {
        this.versionNumber = setNum(versionNumberS);
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = setW(what);
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = setW(when);
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = setW(where);
    }

    public String getWho() {
        return who;
    }

    public void setWho(String who) {
        this.who = setW(who);
    }

    public DateState getCreated() {
        return created;
    }

    public void setCreated(DateState created) {
        this.created = created;
    }

    public void setCreatedDB(String createdS) {
        if (StringUtil.isEmpty(createdS)) this.created = null;
        this.created = InvUtil.setDBDate(createdS);
    }

    public DateState getModified() {
        return modified;
    }

    public String getModifiedDB() {
        if (modified == null) return null;
        return InvUtil.getDBDate(modified);
    }

    public void setModified(DateState modified) {
        this.modified = modified;
    }

    public void setModifiedDB(String modifiedS) {
        this.modified = InvUtil.setDBDate(modifiedS);
    }

    public void setModified() {
        this.modified = new DateState();
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
    
    public static String setW(String w)
    {
        if (StringUtil.isEmpty(w)) return null;
        if (w.length() <=  MAXW) {
            return w;
        }
        return w.substring(0,(MAXW - 3)) + "...";
    }

    public String getMd5_3() {
        return md5_3;
    }

    public void setMd5_3(String md5_3) {
        this.md5_3 = md5_3;
    }
    
    public void buildMd5_3()
         throws TException
    {
        try {
            if (ark == null) {
                return;
            }
            String key = ark.getValue();
            byte[] bytes = key.getBytes("utf-8");
            InputStream stream = new ByteArrayInputStream(bytes);
            MessageDigestValue mdv = new MessageDigestValue(stream, "md5", logger);
            String md5 = mdv.getChecksum();
            if (DEBUG) System.out.println("md5=" + md5);
            md5_3 = md5.substring(0,3);
            
        } catch (TException tex) {
           throw tex;
           
        } catch (Exception ex) {
           throw new TException(ex);
        }
        
    }

}

