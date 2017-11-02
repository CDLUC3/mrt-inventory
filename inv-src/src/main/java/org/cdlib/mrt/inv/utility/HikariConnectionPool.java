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

import org.cdlib.mrt.utility.*;
import com.zaxxer.hikari.*;
import java.sql.*;


/**
 * Database Connection Pool explicitly set up to run with HikariCP
 * @author dloy
 */
public class HikariConnectionPool
{

    protected static final String NAME = "HickariConnectionPool";
    protected static final String MESSAGE = NAME + ": ";
    private String url, user, password;
    final private long timeout=120000;
    protected HikariDataSource connectionPool = null;
    
    public static HikariConnectionPool getDBConnectionPool(String url, String user, String password)
        throws TException
    {
        try {
            if (StringUtil.isEmpty(url)) {
                throw new TException.INVALID_OR_MISSING_PARM("url not supplied");
            }
            if (StringUtil.isEmpty(user)) {
                throw new TException.INVALID_OR_MISSING_PARM("user not supplied");
            }
            if (password == null) {
                throw new TException.INVALID_OR_MISSING_PARM("password not supplied");
            }
            return new HikariConnectionPool(url, user, password);

        } catch (Exception ex) {
            System.out.println("MrtConnectionPool Exception:" + ex);
            throw new TException(ex);
        }
    }

    protected HikariConnectionPool(String url, String user, String password)
            throws TException
    {
        try {
            this.url = url;
            this.user = user;
            this.password = password;

            if (connectionPool != null) {
                System.out.println(MESSAGE + "connection already exists - no startup");
            }
            HikariConfig config=new HikariConfig();
            
            config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            config.addDataSourceProperty("URL",url);
            config.addDataSourceProperty("user",user);
            config.addDataSourceProperty("password",password);
            config.addDataSourceProperty("socketTimeout", "1800000");
            // default:config.setMaximumPoolSize(10);
            // default:config.setConnectionTimeout(30000);
            config.setIdleTimeout(0);
            // default:config.setMaxLifetime(1800000);
            
            Class.forName("com.mysql.jdbc.Driver");  // allows tomcat to see driver
            connectionPool=new HikariDataSource(config);
  
            System.out.println("Hickari settings:" + "\n"
                    + " - MaximumPoolSize:" + connectionPool.getMaximumPoolSize() + "\n"
                    + " - ConnectionTimeout:" + connectionPool.getConnectionTimeout() + "\n"
                    + " - IdleTimeout:" + connectionPool.getIdleTimeout() + "\n"
                    + " - MaxLifetime:" + connectionPool.getMaxLifetime() + "\n"
            );
            

        } catch (Exception ex) {
            throw new TException(ex);
        }
   }

    public synchronized void closeConnections()
    {
        try {
            //trace("closeConnection");
            if (connectionPool != null) {
                connectionPool.close();
                System.out.println(MESSAGE + "closeConnection performed");
            }
        } catch (Exception ex) {
            System.out.println("Exception on closeConnection: " + ex);
            ex.printStackTrace();
            
        } finally {
            connectionPool = null;
        }
    }


    public synchronized Connection getConnection() throws SQLException
    {
        if (connectionPool == null) return null;
        try {
            Connection connection = connectionPool.getConnection();
            return connection;
            
        } catch (SQLException sqlEx) {
            System.out.println("Exception on connection:" + sqlEx);
            //sqlEx.printStackTrace();
            return null;
        }
    }

    public synchronized Connection getConnection(boolean autoCommit) throws SQLException
    {
        if (connectionPool == null) return null;
        SQLException sqlException = null;
        for (int itry=0; itry < 2; itry++) {
            Connection connection = null;
            try {
                connection = connectionPool.getConnection();
                connection.setAutoCommit(autoCommit);
                return connection;

            } catch (SQLException sqlEx) {
                System.out.println("getConnection(" + itry + ") - Exception on connection:" + sqlEx);
                sqlException = sqlEx;
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception ex) { }
                
            }
        }
        throw sqlException;
    }
    
    public void trace(String header)
    {
        
            try {
                throw new TException.GENERAL_EXCEPTION("test");
            } catch (Exception testex) {
                System.out.println("***WARNING TRACE: " + header);
                testex.printStackTrace();
            }
    }
}
