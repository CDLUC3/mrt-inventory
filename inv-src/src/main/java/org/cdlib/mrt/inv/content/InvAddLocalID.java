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
package org.cdlib.mrt.inv.content;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.MessageDigestValue;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvAddLocalID
{
    private static final String NAME = "InvAddLocalID";
    private static final String MESSAGE = NAME + ": ";
    private static final int MAXW = 5394;
    

    public long objectseq = 0; 
    public Identifier objectArk = null;
    public Identifier ownerArk = null;
    public String localIDs = null;
    
    public InvAddLocalID(Properties prop)
        throws TException
    {
        setProp(prop);
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        //System.out.println(PropertiesUtil.dumpProperties(NAME, prop));
        try {
            setObjectseq(prop.getProperty("objectseq"));
            setObjectArk(prop.getProperty("object_ark"));
            setOwnerArk(prop.getProperty("owner_ark"));
            setLocalIDs(prop.getProperty("locals"));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }

    public Properties getProp()
        throws TException
    {
        Properties retProp = new Properties();
        try {
            retProp.setProperty("objectseq","" + getObjectseq());
            retProp.setProperty("object_ark",getObjectArk().getValue());
            retProp.setProperty("owner_ark",getOwnerArk().getValue());
            retProp.setProperty("locals",getLocalIDs());
            return retProp;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }

    public long getObjectseq() {
        return objectseq;
    }

    public void setObjectseq(String objectseqS) {
        this.objectseq = ContentAbs.setNumLong(objectseqS);
    }

    public void setObjectseq(long objectseq) {
        this.objectseq = objectseq;
    }


    public Identifier getObjectArk() {
        return objectArk;
    }

    public void setObjectArk(Identifier objectArk) {
        this.objectArk = objectArk;
    }

    public void setObjectArk(String objectArkS)
        throws TException
    {
        this.objectArk = sToId(objectArkS);
    }

    public Identifier getOwnerArk() {
        return ownerArk;
    }

    public void setOwnerArk(Identifier ownerArk) {
        this.ownerArk = ownerArk;
    }

    public void setOwnerArk(String ownerArkS)
        throws TException
    {
        this.ownerArk = sToId(ownerArkS);
    }

    public String getLocalIDs() {
        return localIDs;
    }

    public void setLocalIDs(String localIDs) {
        this.localIDs = localIDs;
    }

    protected Identifier pToId(Properties prop, String name)
        throws TException
    {
        if (prop == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "prop missing");
        }
        if (StringUtil.isAllBlank(name)) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "name missing");
        }
        String value = prop.getProperty(name);
        if (StringUtil.isAllBlank(value)) {
            return null;
        }
        Identifier retIdentifier = new Identifier(value);
        return retIdentifier;
    }

    protected Identifier sToId(String value)
        throws TException
    {
        if (StringUtil.isAllBlank(value)) {
            return null;
        }
        Identifier retIdentifier = new Identifier(value);
        return retIdentifier;
    }
}

