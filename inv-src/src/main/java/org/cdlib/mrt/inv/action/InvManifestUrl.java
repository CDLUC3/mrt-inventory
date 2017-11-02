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
import java.net.URL;
import java.util.Properties;

import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;

/**
 * Run fixity
 * @author dloy
 */
public class InvManifestUrl
        extends InvActionAbs
        implements Runnable, StateInf
{

    protected static final String NAME = "ManifestUrl";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    
    protected Identifier objectID = null;
    protected URL manifestUrl = null;
    protected Properties[] props = null;
    protected Properties prop = null;
 
    
    public static InvManifestUrl getInvManifestUrl(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new InvManifestUrl(objectID, connection, logger);
    }
    
    protected InvManifestUrl(
            Identifier objectID,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        this.objectID = objectID;
    } 

    public void run()
    {
        try {
            process();
        } catch (Exception ex) {
            setException(ex);
        }

    }

    public void process()
        throws TException
    {
        try {
            connection.setAutoCommit(true);
            props = InvDBUtil.getObjectUrl(objectID, connection, logger);
            if (props == null) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND(MESSAGE + "Unable to locate object:" + objectID.getValue());
            }
 
            for (Properties local : props) {
                if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("****test****", local));
                String role = local.getProperty("role");
                if ((role != null) && role.equals("primary")) {
                    prop = local;
                    break;
                }
            }
            if (prop == null) {
                throw new TException.INVALID_DATA_FORMAT(MESSAGE 
                        + "Primary entry not found:" + objectID.getValue());
            }
            String baseURLS = propEx("base_url");
            String nodeS = propEx("number");
            int node = Integer.parseInt(nodeS);
            
            
            String urlS = baseURLS + "/manifest/" + node + '/' + URLEncoder.encode(objectID.getValue(), "utf-8");
            manifestUrl = new URL(urlS);
        
        return;
        
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
            
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ex) { }
            }
        }
    }
    
    protected String propEx(String key)
        throws TException
    {
        if (StringUtil.isAllBlank(key)) {
            throw new TException.INVALID_OR_MISSING_PARM("propEx no key supplied");
        }
        String value = prop.getProperty(key);
        if (value == null) {
            throw new TException.INVALID_OR_MISSING_PARM("propEx key not found:" + key);
        }
        return value;
    }
    
    public Identifier getObjectID() {
        return objectID;
    }

    public void setObjectID(Identifier objectID) {
        this.objectID = objectID;
    }

    public URL retrieveManifestUrl() {
        return manifestUrl;
    }

    public String getManifestUrl() {
        if (manifestUrl == null) return null;
        return manifestUrl.toString();
    }

    public void setManifestUrl(URL manifestUrl) {
        this.manifestUrl = manifestUrl;
    }
}

