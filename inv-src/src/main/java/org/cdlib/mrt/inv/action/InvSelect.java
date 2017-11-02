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

import java.sql.Connection;
import java.util.Properties;
import java.util.concurrent.Callable;
import org.cdlib.mrt.inv.service.InvSelectState;
import org.cdlib.mrt.inv.utility.InvDBUtil;

import org.cdlib.mrt.db.DBUtil;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;

/**
 * Run fixity
 * @author dloy
 */
public class InvSelect
        extends InvActionAbs
        implements Callable, Runnable
{

    protected static final String NAME = "InvSelect";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;

    protected String select = null;
    protected InvSelectState invSelectState = null;
    
    public static InvSelect getInvSelect(
            String select,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        return new InvSelect(select, connection, logger);
    }
    
    protected InvSelect(
            String select,
            Connection connection,
            LoggerInf logger)
        throws TException
    {
        super(connection, logger);
        if (StringUtil.isEmpty(select)) {
            throw new TException.REQUEST_INVALID(MESSAGE + "sql select required");
        }
        this.select = "select " + select;
    }

    Properties [] rows = null;

    @Override
    public void run()
    {
        try {
            process();

        } catch (Exception ex) {
            String msg = MESSAGE + "Exception for select=" + select
                    ;
            logger.logError(msg, 2);
            setException(ex);

        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }

    }


    @Override
    public InvSelectState call()
    {
        run();
        return getFixitySelect();
    }
    
    public InvSelectState process()
        throws TException
    {
        try {
            log("run entered");
            connection.setAutoCommit(true);
            rows = DBUtil.cmd(connection, select, logger);
            if ((rows == null) || (rows.length == 0)) {
                System.out.println(MESSAGE + " null results");
                return new InvSelectState((Properties [])null);
            }
            System.out.println(MESSAGE + "rows cnt:" + rows.length);
            invSelectState = new InvSelectState(rows);
            invSelectState.setSql(select);
            return invSelectState;

        } catch (TException tex) {
            String msg = MESSAGE + "Exception for select=" + select
                    ;
            logger.logError(msg, 2);
            throw tex;

        } catch (Exception ex) {
            String msg = MESSAGE + "Exception for select=" + select
                    ;
            logger.logError(msg, 2);
            throw new TException(ex);

        } finally {
            try {
                connection.close();
            } catch (Exception ex) { }
        }

    }

    public Properties[] getRows() {
        return rows;
    }

    public void setRows(Properties[] rows) {
        this.rows = rows;
    }

    public InvSelectState getFixitySelect() {
        return invSelectState;
    }

    public void setFixitySelect(InvSelectState invSelectState) {
        this.invSelectState = invSelectState;
    }

    protected void log(String msg)
    {
        if (!DEBUG) return;
        System.out.println(MESSAGE + msg);
    }
}

