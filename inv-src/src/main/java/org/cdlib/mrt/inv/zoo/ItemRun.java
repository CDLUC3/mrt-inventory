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
package org.cdlib.mrt.inv.zoo;

import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.Properties;

import org.cdlib.mrt.queue.Item;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.action.SaveObject;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.zoo.ItemInfo;

/**
 * Build Runnable routine for adding zookeeper Item to inv database
 * @author dloy
 */
public class ItemRun
        implements Runnable
{

    protected static final String NAME = "ItemRun";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    protected static final String NL = System.getProperty("line.separator");
    
    protected URL manifestURL = null;
    protected String storageBase = null;
    protected Identifier objectID = null;
    protected int node = -1;
    protected ItemInfo info = null;
    protected Properties itemProp = null;
    protected Connection connection = null;
    protected LoggerInf logger = null;
    protected SaveObject saveObject = null;
 
    
    public static ItemRun getItemRun(
            ItemInfo info,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new ItemRun(info, connection, logger);
    }
    
    protected ItemRun(
            ItemInfo info,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        this.info = info;
        this.connection = connection;
        this.logger = logger;
        itemProp = info.getProp();
        validate(itemProp);
        buildSaveObject();
    }
    
    public ItemRun() { }
    
    public void validate(Properties itemProp)
        throws TException
    {
        try {
            String urlS = itemProp.getProperty("storage_url");
            if (StringUtil.isEmpty(urlS)) {
                logger.logError(PropertiesUtil.dumpProperties("processItem - storage_url missing", itemProp), 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "processItem - storage_url missing");
            }
            try {
                manifestURL = new URL(urlS);
                if (DEBUG) System.out.println("manifestURL=" + manifestURL);
            } catch (Exception ex) {
                String msg = "processItem - URL invalid:" + urlS;
                logger.logError(msg, 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            }
            String urlPath = manifestURL.getPath();
            storageBase = 
                    manifestURL.getProtocol() 
                    + "://" + manifestURL.getHost()
                    + ":" + manifestURL.getPort()
                    ;
            
            if (DEBUG) System.out.println("storageBase=" + storageBase);
            String parts[] = urlPath.split("\\/");
            if (DEBUG) System.out.println("parts[] length=" + parts.length);        
            if (parts.length < 4) {
                String msg = "processItem - URL format invalid:" + urlS;
                logger.logError(msg, 0);
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            }
            for (int i=0; i < parts.length; i++) {
                if (parts[i].equals("manifest")) {
                    extractParts(parts, i);
                    return;
                }
            }
            String msg = "processItem - URL format invalid:" + urlS;
            logger.logError(msg, 0);
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + msg);
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    private void extractParts(String [] parts, int manifestInx)
        throws TException
    {
        
        try {
            String nodeS = parts[manifestInx + 1];
            String objectIDSE = parts[manifestInx + 2];
            node = Integer.parseInt(nodeS);
            String objectIDS = URLDecoder.decode(objectIDSE, "utf-8");
            objectID = new Identifier(objectIDS);
            
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    private void buildSaveObject()
        throws TException
    {
        /**
        try {
            if (DEBUG) System.out.println(dump("buildSaveObject"));
            saveObject =  SaveObject.getSaveObject(
                storageBase,
                node,
                objectID,
                itemProp,
                connection,
                logger);            
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
         * */
    }

    
    public void run()
    {

    }

    public void process()
        throws TException
    {
        this.saveObject.process();
        return;
    }
    
    public boolean isCommit()
    {
        return true;
    }
    
    public String dump(String header)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("****" + header + NL);
        if (manifestURL != null) buf.append(" - manifestURL=" + manifestURL + NL);
        if (storageBase != null) buf.append(" - storageBase=" + storageBase + NL);
        if (objectID != null) buf.append(" - objectID=" + objectID + NL);
        buf.append(" - node=" + node + NL);
        if (itemProp != null) buf.append(PropertiesUtil.dumpProperties("itemProp", itemProp) + NL);
        return buf.toString();
    }
}

