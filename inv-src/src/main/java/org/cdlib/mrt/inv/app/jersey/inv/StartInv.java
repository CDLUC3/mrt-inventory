/*
Copyright (c) 2005-2012, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

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
package org.cdlib.mrt.inv.app.jersey.inv;

import org.cdlib.mrt.inv.app.InvServiceInit;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.CloseableService;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cdlib.mrt.formatter.FormatterInf;
import org.cdlib.mrt.inv.action.InvManifestUrl;
import org.cdlib.mrt.inv.action.AddZoo;
import org.cdlib.mrt.inv.app.jersey.KeyNameHttpInf;
import org.cdlib.mrt.inv.app.jersey.JerseyBase;
import org.cdlib.mrt.inv.service.InvDeleteState;
import org.cdlib.mrt.inv.service.InvServiceState;
import org.cdlib.mrt.inv.service.InvServiceInf;
import org.cdlib.mrt.inv.service.LocalContainerState;
import org.cdlib.mrt.inv.service.LocalAfterToState;
import org.cdlib.mrt.inv.service.PrimaryLocalState;
import org.cdlib.mrt.inv.service.Role;
import org.cdlib.mrt.inv.service.VersionsState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.inv.logging.LogInvPrimary;
import org.cdlib.mrt.inv.service.InvProcessState;
import org.cdlib.mrt.log.utility.Log4j2Util;
import org.cdlib.mrt.utility.StateInf;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.zk.Batch;
import org.cdlib.mrt.zk.Job;
import org.cdlib.mrt.zk.JobState;
import org.cdlib.mrt.inv.zoo.ZooManager;
import org.apache.zookeeper.ZooKeeper;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.zk.Access;
import org.json.JSONObject;

/**
 * Thin Jersey layer for inv handling
 * @author  David Loy
 */

public class StartInv extends HttpServlet
{

    protected static final String NAME = "StartInv";
    protected static final String MESSAGE = NAME + ": ";

    private static final Logger log4j = LogManager.getLogger();
    private InvServiceState responseState = null;
    
    public void init(ServletConfig servletConfig)
            throws ServletException 
    
    {
        try {
            super.init(servletConfig);

	
            log4j.info("StartInv entered:");
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(servletConfig);
            InvServiceInf invService = invServiceInit.getInvService();

            responseState = invService.startup();
            
        } catch (ServletException se) {
            se.printStackTrace();
            throw se;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new RuntimeException("BYE");
        }
    }

    public InvServiceState getResponseState() {
        return responseState;
    }
}
