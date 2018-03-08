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
package org.cdlib.mrt.inv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.StateInf;

/**
 * State for multiple item entry results
 * @author  dloy
 */

public class VersionsState
        implements StateInf
{
    private static final String NAME = "VersionsState";
    private static final String MESSAGE = NAME + ": ";

    protected Identifier objectID = null;
    protected DateState reportDate = new DateState();
    protected String container = null;
    protected String bucketProperty = null;
    protected Long node = null;
    protected long currentVersion = 0;
    protected ArrayList<Version> versions = new ArrayList<Version>();

    public VersionsState(Identifier objectID)
    { 
        this.objectID = objectID;
    }


    public Version getVersion(int i)
    {
        if (i < 0) return null;
        if (i >= versions.size()) return null;
        return versions.get(i);
    }

    public void addVersion(Version version)
    {
        if (version == null) return;
        versions.add(version);
    }
    
    public void clear()
    {
        versions.clear();
    }

    public DateState getReportDate() {
        return reportDate;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public Long getNode() {
        return node;
    }

    public void setNode(Long node) {
        this.node = node;
    }

    public Identifier getObjectID() {
        return objectID;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(long currentVersion) {
        this.currentVersion = currentVersion;
    }

    public List<Version> getVersions() 
    {
        return versions;
    }

    public String getBucketProperty() {
        return bucketProperty;
    }

    public void setBucketProperty(String bucketProperty) {
        this.bucketProperty = bucketProperty;
    }
    
    public Set<String> retrieveKeys() 
    {
        HashMap<String,String>  map = new HashMap<>();
        try {
            for (Version version : versions) {
                List<VFile> vFiles = version.getFiles();
                for (VFile vFile : vFiles) {
                    String key = vFile.getKey();
                    map.put(key, key);
                }
            }
            Set<String> keys = map.keySet();
            return keys;
            
        } catch (Exception ex) {
            return null;
        }
    }

}
