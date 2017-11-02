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


import org.cdlib.mrt.inv.extract.StoreState;
import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.FileComponent;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.MessageDigest;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv Object content
 * @author dloy
 */
public class InvNode
        extends ContentAbs
{
    private static final String NAME = "InvNode";
    private static final String MESSAGE = NAME + ": ";
    

    public long id = 0;
    public long number = 0;
    public MediaType mediaType = null;
    public AccessMode accessMode = null;
    public MediaConnectivity mediaConnectivity = null;
    public AccessProtocol accessProtocol = null;
    public NodeForm nodeForm = NodeForm.physical;
    public NodeProtocol nodeProtocol = NodeProtocol.file;
    public String logicalVolume = null;
    public String externalProvider = null;
    public boolean verifyOnRead = false;
    public boolean verifyOnWrite = false;
    public String baseURL = null;
    public DateState created = null;
    public String description = null;
    public boolean newEntry = true;
    public Long targetNodeId = null;
    public Long sourceNodeId = null;
    public Long targetNodeSeq = null;
    public Long sourceNodeSeq = null;
    
    public enum MediaType
    {
        magneticDisk("magnetic-disk"),
        magneticTape("magnetic-tape"),
        opticalDisk("optical-disk"),
        solidState("solid-state"),
        unknown("unknown")
        ;

        protected final String mediaType;
        MediaType(String mediaType) {
            this.mediaType = mediaType;
        }
        public String toString()
        {
            return mediaType;
        }

        public static MediaType getMediaType(String t)
        {
            if (t == null) return null;
            for (MediaType p : MediaType.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return unknown;
        }
    }
    
    public enum AccessMode
    {
        onLine("on-line"),
        nearLine("near-line"),
        offLine("off-line"),
        unknown("unknown")
        ;

        protected final String accessMode;
        AccessMode(String accessMode) {
            this.accessMode = accessMode;
        }
        public String toString()
        {
            return accessMode;
        }

        public static AccessMode getAccessMode(String t)
        {
            if (t == null) return null;
            for (AccessMode p : AccessMode.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return unknown;
        }
    }
    
    public enum AccessProtocol
    {
        //('cifs','nfs','open-stack','s3','zfs'
        cifs("cifs"),
        nfs("nfs"),
        openStack("open-stack"),
        s3("s3"),
        zfs("zfs"),
        unknown("unknown")
        ;

        protected final String accessProtocol;
        AccessProtocol(String accessProtocol) {
            this.accessProtocol = accessProtocol;
        }
        public String toString()
        {
            return accessProtocol;
        }

        public static AccessProtocol getAccessProtocol(String t)
        {
            if (t == null) return null;
            for (AccessProtocol p : AccessProtocol.values()) {
                if (p.toString().equals(t)) {
                    return p;
                }
            }
            return unknown;
        }
    }
    
    public enum MediaConnectivity
    {
        cloud, das, nas, san, unknown;
    }
    
    public enum NodeForm
    {
        physical, virtual;
    }
    
    public enum NodeProtocol
    {
        file, http;
    }
    
    public InvNode(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvNode(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }
    /**
     * Using the StoreState container set the values for the nodes local content
     * @param state = StorageState output
     * @throws TException 
     */
    public void setState(StoreState state)
        throws TException
    {
        if (state == null) {
            throw new TException.INVALID_DATA_FORMAT(MESSAGE + "state missing");
        }
        Properties prop = state.getStateProp();
        setCanInfo(prop);
    }
    

    /**
     * Set InvNode values based on can-info properties
     * 
     * @param prop extracted properties from can-info.txt file
     * @throws TException 
     */
    public void setCanInfo(Properties prop)
        throws TException
    {
        try {
            if (prop == null) {
                throw new TException.INVALID_DATA_FORMAT(MESSAGE + "prop missing");
            }
            setNumber(prop.getProperty("identifier"));
            setMediaType(prop.getProperty("mediaType"));
            setAccessMode(prop.getProperty("accessMode"));
            setMediaConnectivity(prop.getProperty("mediaConnectivity"));
            setNodeForm(prop.getProperty("nodeForm"));
            setNodeProtocol(prop.getProperty("nodeProtocol"));
            setAccessProtocol(prop.getProperty("accessProtocol"));
            setLogicalVolume(prop.getProperty("logicalVolume"));
            setExternalProvider(prop.getProperty("externalProvider"));
            setVerifyOnRead(prop.getProperty("verifyOnRead"));
            setVerifyOnWrite(prop.getProperty("verifyOnWrite"));
            setCreated(prop.getProperty("created"));
            setDescription(prop.getProperty("description"));
            setTargetNodeId(prop.getProperty("targetNodeID"));
            setSourceNodeId((String)prop.getProperty("sourceNodeID"));
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }

    /**
     * From a Properties container set the local values for the nodes table
     * @param prop nodes Properties
     * @throws TException 
     */
    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setNumber(prop.getProperty("number"));
            setMediaType(prop.getProperty("media_type"));
            setMediaConnectivity(prop.getProperty("media_connectivity"));
            setAccessMode(prop.getProperty("access_mode"));
            setAccessProtocol(prop.getProperty("access_protocol"));
            setLogicalVolume(prop.getProperty("logical_volume"));
            setExternalProvider(prop.getProperty("external_provider"));
            setVerifyOnReadDB(prop.getProperty("verify_on_read"));
            setVerifyOnWriteDB(prop.getProperty("verify_on_write"));
            setBaseURL(prop.getProperty("base_url"));
            setCreatedDB(prop.getProperty("created"));
            setDescription(prop.getProperty("description"));
            setNodeForm(prop.getProperty("node_form"));
            setNodeProtocol(prop.getProperty("node_protocol"));
            setSourceNodeSeq(prop.getProperty("source_node"));
            setTargetNodeSeq(prop.getProperty("target_node"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    /**
     * Retrieve a properties where key=db key and value=db usable value
     * @return db properties
     * @throws TException 
     */
    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getNumber() != 0) prop.setProperty("number", "" + getNumber());
        if (getMediaType() != null) prop.setProperty("media_type", getMediaType().toString());
        if (getMediaConnectivity() != null) prop.setProperty("media_connectivity", getMediaConnectivity().toString());
        if (getNodeForm() != null) prop.setProperty("node_form", getNodeForm().toString());
        if (getNodeProtocol() != null) prop.setProperty("node_protocol", getNodeProtocol().toString());
        if (getAccessMode() != null) prop.setProperty("access_mode", getAccessMode().toString());
        if (getAccessProtocol() != null) prop.setProperty("access_protocol", getAccessProtocol().toString());
        if (getLogicalVolume() != null) prop.setProperty("logical_volume", getLogicalVolume());
        if (getExternalProvider() != null) prop.setProperty("external_provider", getExternalProvider());
        prop.setProperty("verify_on_read", "" + getVerifyOnReadDB());
        prop.setProperty("verify_on_write", "" + getVerifyOnWriteDB());
        if (!StringUtil.isAllBlank(getBaseURL())) prop.setProperty("base_url", getBaseURL());
        if (getCreatedDB() != null) prop.setProperty("created", getCreatedDB());
        if (getDescription() != null) prop.setProperty("description", getDescription());
        if (getSourceNodeSeq() != null) prop.setProperty("source_node", "" + getSourceNodeSeq());
        if (getTargetNodeSeq() != null) prop.setProperty("target_node", "" + getTargetNodeSeq());
        return prop;
    }
    
    public String getDBName()
    {
        return NODES;
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

    public DateState getCreated() {
        return created;
    }

    public String getCreatedDB() {
        if (created == null) return null;
        return InvUtil.getDBDate(created);
    }

    public void setCreated(DateState created) {
        this.created = created;
    }

    public void setCreated(String createdS) {
        if (StringUtil.isAllBlank(createdS)) this.created = new DateState();
        this.created = new DateState(createdS);
    }

    public void setCreatedDB(String createdS) {
        if (StringUtil.isAllBlank(createdS)) this.created = new DateState();
        this.created = InvUtil.setDBDate(createdS);
    }

    public AccessMode getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(AccessMode accessMode) {
        this.accessMode = accessMode;
    }

    public void setAccessMode(String accessModeS) {
        if (StringUtil.isEmpty(accessModeS)) {
            this.accessMode = null;
            return;
        }
        try {
            this.accessMode = AccessMode.getAccessMode(accessModeS);
        } catch (Exception e) {
            this.accessMode = AccessMode.unknown;
        }
    }

    public MediaConnectivity getMediaConnectivity() {
        return mediaConnectivity;
    }

    public void setMediaConnectivity(MediaConnectivity mediaConnectivity) {
        this.mediaConnectivity = mediaConnectivity;
    }

    public void setMediaConnectivity(String mediaConnectivityS) {
        if (StringUtil.isEmpty(mediaConnectivityS)) {
            this.mediaConnectivity = null;
            return;
        }
        try {
            this.mediaConnectivity = MediaConnectivity.valueOf(mediaConnectivityS);
        } catch (Exception e) {
            this.mediaConnectivity = MediaConnectivity.unknown;
        }
        
    }

    public NodeForm getNodeForm() {
        return nodeForm;
    }

    public void setNodeForm(NodeForm nodeForm) {
        this.nodeForm = nodeForm;
    }

    public void setNodeForm(String nodeFormS) {
        if (StringUtil.isEmpty(nodeFormS)) {
            this.nodeForm = null;
            return;
        }
        try {
            this.nodeForm = NodeForm.valueOf(nodeFormS);
        } catch (Exception e) {
            this.nodeForm = NodeForm.physical;
        }
        
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setMediaType(String mediaTypeS) {
        if (StringUtil.isEmpty(mediaTypeS)) {
            this.mediaType = null;
            return;
        }
        this.mediaType = MediaType.getMediaType(mediaTypeS);
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public void setNumber(String numberS) {
        if (numberS == null) this.number = 0L;
        else this.number = Long.parseLong(numberS);
    }

    public AccessProtocol getAccessProtocol() {
        return accessProtocol;
    }

    public void setAccessProtocol(AccessProtocol accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    public void setAccessProtocol(String accessProtocolS) {
        if (StringUtil.isEmpty(accessProtocolS)) {
            this.accessProtocol = null;
            return;
        }
        this.accessProtocol = AccessProtocol.getAccessProtocol(accessProtocolS);
    }

    public boolean getVerifyOnRead() {
        return verifyOnRead;
    }

    public int getVerifyOnReadDB() {
        if (verifyOnRead) return 1;
        return 0;
    }

    public void setVerifyOnRead(boolean verifyOnRead) {
        this.verifyOnRead = verifyOnRead;
    }

    public void setVerifyOnRead(int verifyOnReadI) {
        if (verifyOnReadI == 0) this.verifyOnRead = false;
        else this.verifyOnRead = true;
    }

    public void setVerifyOnRead(String verifyOnReadS) {
        this.verifyOnRead = setBoolString(verifyOnReadS);
    }

    public void setVerifyOnReadDB(String verifyOnReadS) {
        if (verifyOnReadS == null) this.verifyOnRead = true;
        int verifyOnReadI = Integer.parseInt(verifyOnReadS);
        setVerifyOnRead(verifyOnReadI);
    }

    public boolean getVerifyOnWrite() {
        return verifyOnWrite;
    }

    public int getVerifyOnWriteDB() {
        if (verifyOnWrite) return 1;
        return 0;
    }

    public void setVerifyOnWrite(int verifyOnWriteI) {
        if (verifyOnWriteI == 0) this.verifyOnWrite = false;
        else this.verifyOnWrite = true;
    }

    public boolean isVerifyOnWrite() {
        return verifyOnWrite;
    }

    public void setVerifyOnWrite(boolean verifyOnWrite) {
        this.verifyOnWrite = verifyOnWrite;
    }

    public void setVerifyOnWrite(String verifyOnWriteS) {
        this.verifyOnWrite = setBoolString(verifyOnWriteS);
    }

    public void setVerifyOnWriteDB(String verifyOnWriteS) {
        if (verifyOnWriteS == null) this.verifyOnWrite = true;
        int verifyOnWriteI = Integer.parseInt(verifyOnWriteS);
        setVerifyOnWrite(verifyOnWriteI);
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }

    public String getExternalProvider() {
        return externalProvider;
    }

    public void setExternalProvider(String externalProvider) {
        this.externalProvider = externalProvider;
    }

    public String getLogicalVolume() {
        return logicalVolume;
    }

    public void setLogicalVolume(String logicalVolume) {
        this.logicalVolume = logicalVolume;
    }

    public NodeProtocol getNodeProtocol() {
        return nodeProtocol;
    }

    public void setNodeProtocol(NodeProtocol nodeProtocol) {
        this.nodeProtocol = nodeProtocol;
    }
    
    public void setNodeProtocol(String nodeProtocolS) {
        if (StringUtil.isEmpty(nodeProtocolS)) {
            this.nodeProtocol = NodeProtocol.file;
            return;
        }
        try {
            nodeProtocolS = nodeProtocolS.toLowerCase();
            this.nodeProtocol = NodeProtocol.valueOf(nodeProtocolS);
        } catch (Exception e) {
            this.nodeProtocol = NodeProtocol.file;
        }
        
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public Long getTargetNode() {
        return targetNodeId;
    }

    public void setTargetNodeId(Long targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public void setTargetNodeId(String targetNodeIdS)
    {
        this.targetNodeId = setNumLong(targetNodeIdS);
    }

    public Long getSourceNode() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeIdS)
    {
        this.sourceNodeId = setNumLong(sourceNodeIdS);
    }

    public Long getTargetNodeSeq() {
        return targetNodeSeq;
    }

    public void setTargetNodeSeq(Long targetNodeSeq) {
        this.targetNodeSeq = targetNodeSeq;
    }

    public void setTargetNodeSeq(String targetNodeSeqS) {
        this.targetNodeSeq = setNumLong(targetNodeSeqS);
    }

    public Long getSourceNodeSeq() {
        return sourceNodeSeq;
    }

    public void setSourceNodeSeq(Long sourceNodeSeq) {
        this.sourceNodeSeq = sourceNodeSeq;
    }

    public void setSourceNodeSeq(String sourceNodeSeqS) {
        this.sourceNodeSeq = setNumLong(sourceNodeSeqS);
    }



    protected boolean setBoolString(String testS) {
        if (testS == null) return true;
        try {
            return StringUtil.argIsTrue(testS);
        } catch (Exception ex) {
            return true;
        }
    }
}

