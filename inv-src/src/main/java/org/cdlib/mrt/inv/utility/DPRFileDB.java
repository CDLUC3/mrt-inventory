/*
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
*********************************************************************/
package org.cdlib.mrt.inv.utility;

import java.sql.Connection;
import java.util.Properties;
import org.cdlib.mrt.security.SecurityUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

/**
 *
 * @author dloy
 */
public class DPRFileDB
{

    protected static final String NAME = "DPRFileDB";
    protected static final String MESSAGE = NAME + ": ";
    protected static final String PW = "THESE are the times that try men's souls. "
            + "The summer soldier and the sunshine patriot will, "
            + "in this crisis, shrink from the service of their country; "
            + "but he that stands by it now, deserves the love and thanks of man and woman.";
    protected LoggerInf logger = null;
    protected HikariConnectionPool pool = null;
    //protected DBConnectionPool pool = null;
    protected String dburl = null;
    protected String dbuser = null;
    protected String dbpass = null;
    protected int dbMaxConnectionsPerPartition = 5;
    private static final boolean DEBUG = false;
    
    public DPRFileDB(LoggerInf logger, Properties frame)
        throws TException
    {
        this.logger = logger;
        
  
        String password = frame.getProperty("db.password");
        String pw = frame.getProperty("db.pw");
        if (StringUtil.isNotEmpty(pw)) {
            password = SecurityUtil.desDecrypt(pw, PW);
        }
        System.out.println(MESSAGE
                + " - db.url=" + frame.getProperty("db.url")
                + " - db.user=" + frame.getProperty("db.user")
                + " - db.pw=" + frame.getProperty("db.pw")
                + " - db.password=" + frame.getProperty("db.password")
                + " - db.maxConnectionsPerPartition=" + frame.getProperty("db.maxConnectionsPerPartition")
                );
        dburl = frame.getProperty("db.url");
        dbpass = password;
        dbuser = frame.getProperty("db.user");
        String dbMaxConnectionsPerPartitionS = frame.getProperty("db.maxConnectionsPerPartition");
        if (!StringUtil.isAllBlank(dbMaxConnectionsPerPartitionS)) {
            dbMaxConnectionsPerPartition = Integer.parseInt(dbMaxConnectionsPerPartitionS);
        }
        setPool();
    }

    public DPRFileDB(LoggerInf logger,
            String dburl,
            String dbuser,
            String dbpass)
        throws TException
    {
        this.logger = logger;
        this.dburl = dburl;
        this.dbuser = dbuser;
        this.dbpass = dbpass;
        setPool();
    }

     public Connection getConnection(
            boolean autoCommit)
        throws TException
     {
         
         if(pool == null) return null;
         return getConnectionWithReconnect(autoCommit);
     }

     public Connection getSingleConnection(
            boolean autoCommit)
        throws TException
     {
         if(pool == null) return null;
         try {
            return pool.getConnection(autoCommit);

        } catch(Exception e) {
            String msg = "Exception"
                + " - exception:" + e;

            logger.logError(MESSAGE + "getOperation - " + msg, 0);
            e.printStackTrace();
            return null;
        }

     }

    public Connection getConnectionWithReconnect(
            boolean autoCommit)
        throws TException
    {
        if(pool == null) return null;
        Connection connect = null;
        try {
            if (DEBUG) System.out.println("Before getSingleConnection");
            connect = getSingleConnection(autoCommit);
            if (DEBUG) System.out.println("After getSingleConnection");
            if (connect == null) {
                for (int i=0; i<5; i++) {
                    System.out.println(MESSAGE + ">>>>getConnectionWithReconnect retry:" + i);
                    try {
                        if (i > 1) Thread.sleep(1000 * 5); //* 60 * 30);
                        //attemptReconnection();
                        connect = getSingleConnection(autoCommit);
                        if (connect == null) continue;
                        return connect;

                     } catch (Exception arex) {
                         throw new TException.SQL_EXCEPTION("Reconnect attempted and fails:" + arex);
                     }
                }
            }
            return connect;

         } catch (Exception ex) {
             ex.printStackTrace();
                 throw new TException.SQL_EXCEPTION("Reconnect attempted and fails:" + ex);
         }
     }
     
     protected void setPool()
         throws TException
     {
        //pool = DBConnectionPool.getDBConnectionPool(dburl, dbuser, dbpass, dbMaxConnectionsPerPartition);
        pool = HikariConnectionPool.getDBConnectionPool(dburl, dbuser, dbpass);
     }
     
     public void attemptReconnection()
         throws TException
     {
         if (DEBUG) System.out.println("attemptReconnection entered");
         shutDown();
         try {
            setPool();
         } catch (Exception ex) {
             System.err.println("WARNING setPool fails:" + ex);
         }
         
     }

     public Connection getConnectionOriginal(
            boolean autoCommit)
        throws TException
     {
         if(pool == null) return null;
         try {
            return pool.getConnection(autoCommit);

        } catch(Exception e) {
            String msg = "Exception"
                + " - exception:" + e;

            logger.logError(MESSAGE + "getOperation - " + msg, 0);
            e.printStackTrace();
            throw new TException.GENERAL_EXCEPTION(msg, e);
        }

     }

     public void shutDown()
     {
         try {
             if (pool != null) {
                 pool.closeConnections();
                 System.out.println(MESSAGE + "connections closed");
             }
         } catch (Exception ex) { 
             System.out.println(MESSAGE + "WARNING shutDown exception:" + ex);
             
         } finally {
             pool = null;
         }
     }

    public static String retrieveENC(String key)
        throws TException
    {
        try {
            if (StringUtil.isEmpty(key)) return "";
            return SecurityUtil.desEncrypt(key, PW);

        } catch (Exception ex) {
            System.out.println(MESSAGE + "retrieveENC: "
                    + " - key=\"" + key + "\""
                    + " - Exception:" + ex);
            return "";
        }
    }
}

