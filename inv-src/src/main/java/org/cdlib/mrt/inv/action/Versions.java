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

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.content.InvFile;
import org.cdlib.mrt.inv.content.InvNode;
import org.cdlib.mrt.inv.content.InvObject;
import org.cdlib.mrt.inv.extract.StoreState;
import org.cdlib.mrt.inv.service.VersionsState;
import org.cdlib.mrt.inv.service.Version;
import org.cdlib.mrt.inv.service.VFile;
import org.cdlib.mrt.inv.utility.DBAdd;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class Versions
        extends InvActionAbs
{

    protected static final String NAME = "Versions";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;

    protected final Identifier objectID;
    protected final Long specifiedVersion;
    protected final VersionsState state;
    protected InvObject invObject = null;
    
    public static Versions getVersions(
            Identifier objectID,
            Long specifiedVersion,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new Versions(objectID, specifiedVersion, connection, logger);
    }
    protected Versions(
            Identifier objectID,
            Long specifiedVersion,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        if (DEBUG) System.out.print("getVersions entered");
        this.objectID = objectID;
        this.specifiedVersion = specifiedVersion;
        this.state = new VersionsState(objectID);
        
    }
    
    public VersionsState process()
        throws TException
    {
        if (objectID == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "objectID missing");
        }
        if (connection == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "connection missing");
        }
        try {
            this.invObject = InvDBUtil.getObject(objectID, connection, logger);
            state.setCurrentVersion(invObject.getVersionNumber());
            setStateStuff();
            addVersions();
            return state;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
    
    
    protected void setStateStuff()
        throws TException
    {
        Properties versionsProp = InvDBUtil.getVersionsStuff(objectID, connection, logger);
        String nodeS = versionsProp.getProperty("number");
        String bucket = versionsProp.getProperty("logical_volume");
        String ext = versionsProp.getProperty("md5_3");
        int pos = bucket.lastIndexOf("__");
        if ((pos >= 0) && (bucket.length() - pos == 2)) {
            bucket += ext;
        }
        String containerProp = null;
        int posProp = bucket.indexOf('|');
        if (posProp >= 0) {
            containerProp = bucket.substring(posProp+1);
            bucket = bucket.substring(0, posProp);
        }
        long node = Long.parseLong(nodeS);
        state.setContainer(bucket);
        state.setBucketProperty(containerProp);
        state.setNode(node);
    }
    
    protected void addVersions()
        throws TException
    {
        if (specifiedVersion == null) {
            addAllVersions();
            
        } else if (specifiedVersion == 0) {
            addCurrentVersion();
                    
        } else {
            addSpecifiedVersion();
        }
    }
    
    protected void addAllVersions() 
        throws TException
    {
        for (int v=1; v <= state.getCurrentVersion(); v++) {
            Version version = addVersion(v);
            if (v == state.getCurrentVersion()) {
                version.setCurrent(true);
            }
            state.addVersion(version);
        }
    }
    
    protected void addCurrentVersion() 
        throws TException
    {
            Version version = addVersion(state.getCurrentVersion());
            version.setCurrent(true);
            state.addVersion(version);
    }
    
    protected void addSpecifiedVersion() 
        throws TException
    {
            Version version = addVersion(specifiedVersion);
            state.addVersion(version);
        
    }
    
    protected Version addVersion(long versionNum) 
        throws TException
    {
        Version version = new Version(versionNum);
        List<InvFile> invFiles = InvDBUtil.getFiles(objectID, versionNum, connection, logger);
        if (invFiles == null) {
            throw new TException.REQUESTED_ITEM_NOT_FOUND(MESSAGE + "Version not found"
                    + " - objectID:" + objectID.getValue()
                    + " - versionNum:" + versionNum
            );
        }
        for (InvFile invFile : invFiles) {
            long accessVersionNum = getAccessVersionNum(versionNum, invFile);
            String key =  objectID.getValue() + "|" + accessVersionNum + "|" + invFile.getPathName();
            long length = invFile.getFullSize();
            VFile file = new VFile(key, length);
            version.addVFile(file);
        }
        return version;
    }
    
    protected long getAccessVersionNum(long versionNum, InvFile invFile) 
        throws TException
    {
        
        if (versionNum == 1) return 1;
        long billableSize = invFile.getBillableSize();
        long fullSize = invFile.getFullSize();
        if (billableSize > 0) return versionNum;
        if (fullSize == 0) return versionNum;
        String pathname = invFile.getPathName();
        long versionAccess = InvDBUtil.getAccessVersionNum(objectID, versionNum, pathname, connection, logger);
        return versionAccess;
    }
    
}

