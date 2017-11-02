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
package org.cdlib.mrt.inv.extract;


import org.cdlib.mrt.inv.content.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;



import org.cdlib.mrt.cloud.VersionMap;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.extract.StoreERC;
import org.cdlib.mrt.utility.DOMParser;
import org.cdlib.mrt.utility.HTTPUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.URLEncoder;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for DC content
 * @author dloy
 */
public class StoreMeta 
    implements StoreConstInt
{
    private static final String NAME = "StoreMeta";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    private static final String METANAMES = "|producer/mrt-dc.xml|producer/mrt-eml.xml|";
    
    private VersionMap versionMap = null;
    private String storageBase = null;
    private int node = 0;
    private Identifier objectID = null;
    private int versionID = -1;
    private LoggerInf logger = null;
    private ArrayList<MatchMeta> metaComponents = new ArrayList<MatchMeta>();
    private Hashtable<String, MatchMeta> matchTable = new Hashtable<String, MatchMeta>();
    
    private void initMatch()
    {
        addMatch("producer/mrt-dc.xml", "DublinCore", "xml");
        addMatch("producer/mrt-eml.xml", "EML", "xml");
        addMatch("producer/mrt-datacite.xml", "DataCite",  "xml", "3.1", true);
        addMatch("producer/mrt-oaidc.xml", "OAI_DublinCore", "xml", "2.0", true);
        addMatch("producer/stash-wrapper.xml", "StashWrapper", "xml", "1.0", true);
    }
    
    private void addMatch(String fileID, String schemaS, String serializationS)
    {
        addMatch(fileID, schemaS, serializationS, null, false);
    }
    
    private void addMatch(String fileID, String schemaS, String serializationS, String metaVersion, boolean stripHeader)
    {
        InvMeta.Schema schema = InvMeta.Schema.valueOf(schemaS);
        InvMeta.Serialization serialization = InvMeta.Serialization.valueOf(serializationS);
        MatchMeta matchMeta = new MatchMeta(fileID, schema, serialization, metaVersion, stripHeader);
        if (DEBUG) System.out.println("StoreMeta - addMatch:"
                + " - fileID=" + fileID
                + " - schemaS=" + schema.toString()
                + " - serializationS=" + serialization.toString()
                + " - metaVersion=" + metaVersion
                + " - stripHeader=" + stripHeader
                );
        matchTable.put(fileID, matchMeta);
        
    }
    
    public StoreMeta(LoggerInf logger) 
    { 
        this.logger = logger;
        initMatch();
    }
    
    public static StoreMeta getStoreMeta(String storageBase, int node, int versionID, VersionMap versionMap, LoggerInf logger) 
            throws TException
    {
        StoreMeta storeMeta = new StoreMeta(storageBase, node, versionID, versionMap, logger);
        storeMeta.process();
        return storeMeta;
    }
    
    public StoreMeta(String storageBase, int node, int versionID, VersionMap versionMap, LoggerInf logger) 
            throws TException
    { 
        this.storageBase = storageBase;
        this.node = node;
        this.versionID = versionID;
        this.versionMap = versionMap;
        this.logger = logger;
        this.objectID = versionMap.getObjectID();
        initMatch();
    }
    
    public void process()
        throws TException
    {
        try {
            List<FileComponent> components = versionMap.getVersionComponents(versionID);
            for (FileComponent component : components) {
                if (DEBUG) System.out.println("StoreMeta: name=" + component.getIdentifier());
                MatchMeta matchMeta = matchTable.get(component.getIdentifier());
                if (matchMeta != null) {
                    if (DEBUG) System.out.println("StoreMeta: add=" + component.getIdentifier());
                    metaComponents.add(matchMeta);
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }
    
    public int getMetaCnt()
    {
        return metaComponents.size();
    }
    
    public List getMetaComponents()
    {
        return metaComponents;
    }
    
    public InputStream getInputStream(String fileID)
        throws TException
    {
        try {
            String objectIDS = objectID.getValue();
            
            String urlS = storageBase 
                    +"/content/" + node 
                    + "/" + URLEncoder.encode(objectIDS, "utf-8")
                    + "/" + versionID 
                    + "/" + URLEncoder.encode(fileID, "utf-8")
                    + "?fixity=no"
                    ;
            if (DEBUG) System.out.println(MESSAGE + "getInputStream url=" + urlS);
            InputStream inStream = HTTPUtil.getObject(urlS,  EXTRACT_TIMEOUT, 3);
            return inStream;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }
    
    public String getValue(String fileID)
        throws TException
    {
        try {
            String objectIDS = objectID.getValue();
            
            String urlS = storageBase 
                    +"/content/" + node 
                    + "/" + URLEncoder.encode(objectIDS, "utf-8")
                    + "/" + versionID 
                    + "/" + URLEncoder.encode(fileID, "utf-8")
                    + "?fixity=no"
                    ;
            if (DEBUG) System.out.println(MESSAGE + "getInputStream url=" + urlS);
            InputStream inStream = HTTPUtil.getObject(urlS,  EXTRACT_TIMEOUT, 3);
            return StringUtil.streamToString(inStream, "utf-8");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        }
        
    }
    
    public static class MatchMeta
    {
        public String fileID = null;
        public InvMeta.Schema schema = null;
        public InvMeta.Serialization serialization = null;
        public String metaVersion = null;
        public boolean stripHeader = false;
        public MatchMeta(String fileID, 
            InvMeta.Schema schema,
            InvMeta.Serialization serialization,
            String metaVersion,
            boolean stripHeader)
        {
            this.fileID = fileID;
            this.schema = schema;
            this.serialization = serialization;
            this.metaVersion = metaVersion;
            this.stripHeader = stripHeader;
        }
    }
    
}
