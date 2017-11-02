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
import java.util.List;
import java.util.Properties;
import java.util.Set;



import org.cdlib.mrt.inv.extract.StoreERC;
import org.cdlib.mrt.utility.DOMParser;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for DC content
 * @author dloy
 */
public class InvDKVersion 
{
    private static final String NAME = "InvDKVersion";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    private long objectseq = 0;
    private long versionseq = 0;
    private int seq = 0;
    private int whereCnt = 0;
    private LoggerInf logger = null;
    private ArrayList<InvDK> dcElements = new ArrayList<InvDK>();
    
    public InvDKVersion(LoggerInf logger) 
    { 
        this.logger = logger;
    }
    
    public InvDKVersion(long objectseq, long versionseq, Properties [] props, LoggerInf logger) 
    { 
        this.objectseq = objectseq;
        this.versionseq = versionseq;
        this.logger = logger;
        setProperties(props);
    }
    
    public InvDKVersion(long objectseq, long versionseq, LoggerInf logger) 
    { 
        this.objectseq = objectseq;
        this.versionseq = versionseq;
        this.logger = logger;
    }

    public void addStoreERC(StoreERC storeERC)
        throws TException
    {
        if (storeERC == null) return;
        addStoreERC(storeERC, "who");
        addStoreERC(storeERC, "what");
        addStoreERC(storeERC, "when");
        addStoreERC(storeERC, "where");
    }

    public void addStoreERC(StoreERC storeERC, String label)
        throws TException
    {
        if (storeERC == null) return;
        if (StringUtil.isEmpty(label)) return;
        List<String> wList = storeERC.getWList(label);
        for (String w : wList) {
            add(label, w);
        }
    }
    
    public void add(String element, String value)
        throws TException
    {
        if (StringUtil.isEmpty(value)) return;
        if (StringUtil.isEmpty(element)) return;
        try {
            InvDK invDK = new InvDK(logger);
            invDK.setObjectID(objectseq);
            invDK.setVersionID(versionseq);
            invDK.setElement(element);
            invDK.setValue(value);
            seq++;
            if (element.equals("where")) {
                whereCnt++;
                if (whereCnt == 1) {
                    invDK.setQualifier("primary");
                } else if (whereCnt == 2) {
                    invDK.setQualifier("local");
                }
            }
            invDK.setSeq(seq);
            dcElements.add(invDK);
            if (DEBUG) System.out.println(invDK.dump(MESSAGE + "ADD"));
                  
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }
    public void setProperties(Properties [] props) 
    {
        if (props == null) return;
        for (Properties prop : props) {
            addProperties(prop);
        }
    }
    
    public void addProperties(Properties prop)
    {
        InvDK element = new InvDK(prop, logger);
        dcElements.add(element);
        
    }
    
    public int size()
    {
        return dcElements.size();
    }

    public ArrayList<InvDK> getDCList() {
        return dcElements;
    }

    public long getObjectseq() {
        return objectseq;
    }

    public long getVersionseq() {
        return versionseq;
    }
    
}
