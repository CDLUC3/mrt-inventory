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
package org.cdlib.mrt.inv.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.core.MessageDigest;
import org.cdlib.mrt.utility.DateUtil;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.FixityTests;
import org.cdlib.mrt.utility.HTTPUtil;
import static org.cdlib.mrt.utility.HTTPUtil.getFTPInputStream;
import static org.cdlib.mrt.utility.HTTPUtil.getHttpResponse;
import static org.cdlib.mrt.utility.HTTPUtil.isFTP;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

/**
 * This interface defines the functional API for a Curational Storage Service
 * @author dloy
 */
public class InvUtil
{

    protected static final String NAME = "FixityUtil";
    protected static final String MESSAGE = NAME + ": ";

    protected static final boolean DEBUG = true;
    public static final String DBDATEPATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String OAIDATEPATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    protected static final String NL = System.getProperty("line.separator");
    protected static final String SYSTEM_EXCEPTION = "***System Exception";
    protected static final String FIXITY_EXCEPTION = "***Fixity Exception";


    protected static MessageDigest display2Digest(String display)
        throws TException
    {
        try {
            if (StringUtil.isEmpty(display)) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE
                        + "Display value missing");
            }
            String [] parts = display.split("=");
            if (parts.length == 1) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE
                        + "fixity digest content invalid:" + display);
            }
            MessageDigest digest = new MessageDigest(parts[1], parts[0]);
            return digest;

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(ex);
        }
    }

    public static InputStream getInputStream(String location, int timeout)
        throws TException
    {
        if (location == null) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "getInputStream - Exception: no location provided");
        }
        InputStream inputStream = null;
        String locationLower = location.toLowerCase();
        try {
            if (locationLower.startsWith("http://")) {
                inputStream = getObject(location, timeout);
            } else if (locationLower.startsWith("https://")) {
                inputStream = getObject(location, timeout);
            } else if (locationLower.startsWith("file://")) {
                URL fileURL = new URL(location);
                File file = FileUtil.fileFromURL(fileURL);
                inputStream = new FileInputStream(file);
            } else {
                inputStream = new FileInputStream(location);
            }
            return inputStream;

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception tryng to input:" + location);
        
        }
    }

    /**
     * Send this manifestFile to mrt store
     * @param manifestFile
     * @return
     * @throws org.cdlib.framework.utility.FrameworkException
     */
    public static InputStream getObject(String requestURL, int timeout)
        throws TException
    {
        if (isFTP(requestURL)) return getFTPInputStream(requestURL, timeout);
        try {
            HttpResponse response = getHttpResponse(requestURL, timeout);
	    int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null && (responseCode >= 200 && responseCode < 300)) {
                return entity.getContent();
            }
            if (responseCode == 404) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND(
                    "HTTPUTIL: getObject- Error during HttpClient processing"
                    + " - timeout:" + timeout
                    + " - URL:" + requestURL
                    + " - responseCode:" + responseCode
                    );
            }
            throw new TException.EXTERNAL_SERVICE_UNAVAILABLE(
                    "HTTPUTIL: getObject- Error during HttpClient processing"
                    + " - timeout:" + timeout
                    + " - URL:" + requestURL
                    + " - responseCode:" + responseCode
                    );

        } catch( TException tex ) {
            //System.out.println("trace:" + StringUtil.stackTrace(tex));
            throw tex;

        } catch( Exception ex ) {
            System.out.println("trace:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION("HTTPUTIL: getObject- Exception:" + ex);
        }
    }
    public static void setProp(Properties prop, String key, String value)
    {
        if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value)) return;
        prop.setProperty(key, value);
    }

    public static DateState setOAIDate(String dateS)
    {
        if (dateS == null) return null;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        // set the timezone to the original date string's timezone
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = fmt.parse(dateS, new ParsePosition(0));
        DateState dateState = new DateState(date);
        return dateState;
        //return new DateState(DateUtil.getDateFromString(dateS, OAIDATEPATTERN));
    }

    public static String getOAIDate(DateState dateState)
    {
        if (dateState == null) return null;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = dateState.getDate();
        return fmt.format(date);
    }

    public static DateState setDBDate(String dateS)
    {
        if (dateS == null) return null;
        return new DateState(DateUtil.getDateFromString(dateS, DBDATEPATTERN));
    }

    public static String getDBDate(DateState dateState)
    {
        if (dateState == null) return null;
        return DateUtil.getDateString(dateState.getDate(), DBDATEPATTERN);
    }

    public static DateState setDBZDate(String dateS)
    {
        if (dateS == null) return null;
        return new DateState(DateUtil.getIsoDateFromZString(dateS));
    }

    public static String getZDate(DateState dateState)
    {
        if (dateState == null) return null;
        return DateUtil.getIsoZDate(dateState.getDate());
    }

    public static MessageDigest getDigest(String digestTypeS, String digestValueS)
        throws TException
    {
        if (StringUtil.isEmpty(digestTypeS) && StringUtil.isEmpty(digestValueS)) {
            return null;
        }
        return new MessageDigest(digestValueS, digestTypeS);
    }

    public static long setLong(String inLong)
        throws TException
    {
        if (StringUtil.isEmpty(inLong)) return 0;
        try {
            return Long.parseLong(inLong);
        } catch (Exception ex) {
              throw new TException.INVALID_DATA_FORMAT(MESSAGE
                    + "setLong - Exception: size is not numeric:" + inLong);
        }
    }

    public static void sysoutThreads(String header) 
    {
        int activeCount = Thread.activeCount();
        
        Thread[] threads = new Thread[activeCount];
        Thread.enumerate(threads);
        System.out.println("sysoutThreads:" + header + " - count=" + threads.length);
        for (int j=0; j<threads.length; j++) {
            System.out.println(threads[j].toString());
        }
    }
    
    
    public static String getAuditUrl(String fromUrl, String idStop, long nodeNumber)
        throws TException
    {
        if (StringUtil.isAllBlank(fromUrl)) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "getUrlFromBase: fromUrl not supplied");
        }
        if (StringUtil.isAllBlank(idStop)) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "getUrlFromBase: idStop not supplied");
        }
        if (nodeNumber < 1) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "getUrlFromBase: nodeNumber invalid:" + nodeNumber);
        }
        try {
            int pos = fromUrl.lastIndexOf("/" + idStop + "/");
            if (pos < 0) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "getUrlFromBase: idStop not found"
                        + " - fromUrl=" + fromUrl
                        + " - idStop=" + idStop
                        + " - nodeNumber=" + nodeNumber
                        );
            }
            String baseUrl = fromUrl.substring(0, pos);
            int afterNodePos = fromUrl.indexOf('/', pos + idStop.length() + 1);
            afterNodePos = fromUrl.indexOf('/', afterNodePos + 1);
            System.out.println("out"
                        + " - pos=" + pos
                        + " - afterNodePos=" + afterNodePos
                        + " - baseUrl=" + baseUrl
                        );
            String retString = baseUrl + '/' + idStop + '/' + nodeNumber + fromUrl.substring(afterNodePos);
            return retString;

        } catch (Exception ex) {
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "getUrlFromBase Exception-"
                        + " - fromUrl=" + fromUrl
                        + " - idStop=" + idStop
                        + " - nodeNumber=" + nodeNumber
                    );
                    
        
        }
    }
}

