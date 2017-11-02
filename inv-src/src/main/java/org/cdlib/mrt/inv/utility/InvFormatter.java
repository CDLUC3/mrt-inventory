/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cdlib.mrt.inv.utility;
import java.io.InputStream;
import java.util.Vector;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.DOMParser;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.HTTPUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Vector;

import org.cdlib.mrt.formatter.*;
import org.cdlib.mrt.utility.DateUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.utility.StringUtil;


import org.w3c.dom.Document;
/**
 *
 * @author dloy
 */
public class InvFormatter {

    protected static final String NAME = "DCTest";
    protected static final String MESSAGE = NAME + ": ";
    protected final static String NL = System.getProperty("line.separator");
    protected LoggerInf logger = null;
    protected FormatterInf formatter = null;
    public InvFormatter()
        throws TException
    {
        try {
            logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);
            formatter = FormatterAbs.getXMLFormatter(logger);
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public InvFormatter(LoggerInf logger)
        throws TException
    {
        try {
            this.logger = logger;
            formatter = getFormatter(FormatterInf.Format.xml, logger);
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public InvFormatter(FormatterInf.Format format, LoggerInf logger)
        throws TException
    {
        try {
            this.logger = logger;
            formatter = getFormatter(format, logger);
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public FormatterInf getFormatter(FormatterInf.Format formatType, LoggerInf logger)
        throws TException
    {
        try {
            formatter = FormatterAbs.getFormatter(formatType, logger);
            return formatter;
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
   
    public String formatIt(
            StateInf responseState)
        throws TException
    {
        try {
           ByteArrayOutputStream outStream = new ByteArrayOutputStream(5000);
           PrintStream  stream = new PrintStream(outStream, true, "utf-8");
           formatter.format(responseState, stream);
           stream.close();
           byte [] bytes = outStream.toByteArray();
           String retString = new String(bytes, "UTF-8");
           return retString;

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            System.out.println("Trace:" + StringUtil.stackTrace(ex));
            throw new TException(ex);
        }
    }
   
    
    public static class DumpIt {
        public String type = null;
        public FormatterInf formatter = null;
        public DumpIt(String type,  FormatterInf formatter) 
        {
            this.type = type;
            this.formatter = formatter;
        }
    }

}