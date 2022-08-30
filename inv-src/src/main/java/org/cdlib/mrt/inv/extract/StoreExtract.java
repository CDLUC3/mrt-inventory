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



import java.io.File;
import java.util.ArrayList;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
/**
 * Container class for Storage ERC content
 * @author dloy
 */
interface RepeatExtract {
    Object run(
        String url,
        LoggerInf logger)
    throws TException;
}

public class StoreExtract
{
    private static final String NAME = "StoreExtract";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    protected LoggerInf logger = null;
    protected String collectionsS = null;
    protected ArrayList<String> list = null;
    
    public static Object repeat(
            RepeatExtract job,
            String url,
            LoggerInf logger,
            int tries)
        throws TException
    {
        TException responseEx = null;
        for (int loop=1; loop <= tries; loop++) {
            try {
                return job.run(url, logger);
                
            } catch (TException.INVALID_OR_MISSING_PARM iomp) {
                throw iomp;
                
            } catch (TException.REQUESTED_ITEM_NOT_FOUND rinf) {
                responseEx = rinf;
                if (DEBUG) System.out.println("request fail(" + loop + "):"
                        + " exception=" + responseEx
                );
                sleep(loop, tries);
                continue;
                
            } catch (TException tex) {
                if (tex.toString().contains("404")) {
                    responseEx = tex;
                    if (DEBUG) System.out.println("contains fail(" + loop + "):"
                            + " exception=" + responseEx
                    );
                    sleep(loop, tries);
                    continue;
                }
                throw tex;
            }
        }
        if (responseEx == null) {
                throw new TException.INVALID_ARCHITECTURE(MESSAGE 
                        + "no failed response");
        }
        throw responseEx;
    }
    
    private static void sleep(int loop, int tries)
    {
        try {
            if (loop == tries) return; // skip last fail
            long dosleep = loop * 30000;
            if (DEBUG) System.out.println("sleep:"
                        + " loop=" + loop
                        + " tries=" + tries
                        + " dosleep=" + dosleep
            );
            Thread.sleep(dosleep);
        } catch (Exception x) {};
    }
        
    public static String getString(
            String url,
            LoggerInf logger,
            int tries)
        throws TException
    {
        return (String) repeat(new GetString(), url, logger, tries);
    }
    
    public static class GetString
         implements RepeatExtract
    {
        public String run(
                String urlS,
                LoggerInf logger)
            throws TException
        {
            File tempFile = null;
            try {
                if (StringUtil.isEmpty(urlS)) {
                    throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                            + "getStoreCollection - storageBase missing");
                }
                if (logger == null) {
                    throw new TException.INVALID_OR_MISSING_PARM(MESSAGE 
                            + "getStoreCollection - logger missing");
                }
                if (DEBUG) System.out.println("getStoreCollection:" + urlS);
                tempFile = FileUtil.url2TempFile(logger, urlS);
                return FileUtil.file2String(tempFile);

            } catch (TException tex) {
                throw tex;

            } catch (Exception ex) {
                throw new TException.GENERAL_EXCEPTION(ex);

            } finally {
                if (tempFile != null) {
                    try {
                        tempFile.delete();
                    } catch (Exception ex) { }
                }
            }
        }
    }

}

