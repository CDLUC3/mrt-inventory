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

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.StateInf;

/**
 * State for multiple item entry results
 * @author  dloy
 */

public class Version
        implements StateInf
{
    private static final String NAME = "Version";
    private static final String MESSAGE = NAME + ": "; 
    protected long version = 0;
    protected boolean current = false;
    protected ArrayList<VFile> files = new ArrayList<VFile>();

    public Version(long version) 
    {
        this.version = version;
    }

    public VFile getFile(int i)
    {
        if (i < 0) return null;
        if (i >= files.size()) return null;
        return files.get(i);
    }

    public void addVFile(VFile versionFile)
    {
        if (versionFile == null) return;
        files.add(versionFile);
    }

    public int size()
    {
        return files.size();
    }

    public ArrayList<VFile> getFiles() {
        return files;
    }

    public long getVersionNum() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
    
    public long getVersionSize()
    {
        if ((files == null) || (files.size() == 0)) return 0;
        long totSize = 0;
        for (VFile file : files) {
            totSize += file.getLength();
        }
        return totSize;
    }

}
