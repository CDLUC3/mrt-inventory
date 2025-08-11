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
import org.json.JSONObject;

/**
 * Thin Jersey layer for inv handling
 * @author  David Loy
 */
@Path ("/")
public class JerseyInv
        extends JerseyBase
        implements KeyNameHttpInf
{

    protected static final String NAME = "JerseyInv";
    protected static final String MESSAGE = NAME + ": ";
    protected static final FormatterInf.Format DEFAULT_OUTPUT_FORMAT
            = FormatterInf.Format.xml;
    protected static final boolean DEBUG = true;
    protected static final String NL = System.getProperty("line.separator");

    private static final Logger log4j = LogManager.getLogger();
    /**
     * Get state information about a specific node
     * @param nodeID node identifier
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted service information
     * @throws TException
     */
    @GET
    @Path("/state")
    public Response callGetServiceState(
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getServiceState(formatType, cs, sc);
    }

    @POST
    @Path("service/{setType}")
    public Response callService(
            @PathParam("setType") String setType,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (StringUtil.isEmpty(setType)) {
            throw new TException.REQUEST_INVALID("Set inv status requires 'S' query element");
        }
        setType = setType.toLowerCase();
        if (setType.equals("start")) {
            return startInv(formatType, cs, sc);

        } else if (setType.equals("stop")) {
            return stopInv(formatType, cs, sc);

        } else if (setType.equals("stopzoo")) {
            return stopZoo(formatType, cs, sc);

        } else if (setType.equals("restart")) {
            return restartInv(formatType, cs, sc);

        } else  {
            throw new TException.REQUEST_ELEMENT_UNSUPPORTED("Set inv state value not recognized:" + setType);
        }
    }

    @POST
    @Path("filenode/{nodeNum}")
    public Response callFileNode(
            @PathParam("nodeNum") String nodeNumS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (StringUtil.isEmpty(nodeNumS)) {
            throw new TException.REQUEST_INVALID("filenode requires node number");
        }
        int nodeNum = 0;
        try {
            nodeNum = Integer.parseInt(nodeNumS);
        } catch (Exception ex) {
            throw new TException.REQUEST_INVALID("filenode not numeric:" + nodeNumS);
        }
        return setFileNode(nodeNum, formatType, cs, sc);
    }

    @GET
    @Deprecated
    @Path("select/{sql}")
    public Response callSelect(
            @PathParam("sql") String select,
            @DefaultValue("xml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        if (StringUtil.isEmpty(select)) {
            throw new TException.REQUEST_INVALID("callSelect - select not provided");
        }
        return getSelectReport(select, formatType, cs, sc);
    }


    @DELETE
    @Path("object/{objectIDS}")
    public Response callDelete(
            @PathParam("objectIDS") String objectIDS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return delete(objectIDS, formatType, cs, sc);
    }
    
    @GET
    @Path("manurl/{objectIDS}")
    public Response callManUrl(
            @PathParam("objectIDS") String objectIDS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getManifestUrl(objectIDS, formatType, cs, sc);
    }
    
    @GET
    @Path("versions/{objectIDS}")
    public Response callVersions(
            @PathParam("objectIDS") String objectIDS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getVersions(objectIDS, null, formatType, cs, sc);
    }
    
    @GET
    @Path("versions/{objectIDS}/{versionS}")
    public Response callVersions(
            @PathParam("objectIDS") String objectIDS,
            @PathParam("versionS") String versionS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getVersions(objectIDS, versionS, formatType, cs, sc);
    }
    
    @GET
    @Path("versions/{objectIDS}/{versionS}/{fileid}")
    public Response callVersions(
            @PathParam("objectIDS") String objectIDS,
            @PathParam("versionS") String versionS,
            @PathParam("fileid") String fileID,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getVersions(objectIDS, versionS, fileID, formatType, cs, sc);
    }
    
    @GET
    @Path("current/{objectIDS}")
    public Response callCurrent(
            @PathParam("objectIDS") String objectIDS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getCurrent(objectIDS, formatType, cs, sc);
    }
    
    
    @POST
    @Path("primary/{objectIDS}/{ownerIDS}/{localIDs}")
    public Response callAddPrimary(
            @PathParam("objectIDS") String objectIDS,
            @PathParam("ownerIDS") String ownerIDS,
            @PathParam("localIDs") String localIDs,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return addPrimary(objectIDS, ownerIDS, localIDs, formatType, cs, sc);
    }
    
    @POST
    @Path("primary")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callAddPrimaryMultipart(
            @DefaultValue("") @FormDataParam("objectid") String objectIDS,
            @DefaultValue("") @FormDataParam("ownerid") String ownerIDS,
            @DefaultValue("") @FormDataParam("localids") String localIDs,
            @DefaultValue("xhtml") @FormDataParam("response-form") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return addPrimary(objectIDS, ownerIDS, localIDs, formatType, cs, sc);
    }
    
    @POST
    @Path("localafterto/{afterS}/{toS}")
    public Response callAddLocalAfterTo(
            @PathParam("afterS") String afterS,
            @PathParam("toS") String toS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return addLocalAfterTo(afterS, toS,formatType, cs, sc);
    }
    
    @DELETE
    @Path("primary/{objectIDS}")
    public Response callDeletePrimary(
            @PathParam("objectIDS") String objectIDS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return deletePrimary(objectIDS, formatType, cs, sc);
    }
    
    @GET
    @Path("primary/{ownerIDS}/{localID}")
    public Response callPrimary(
            @PathParam("ownerIDS") String ownerIDS,
            @PathParam("localID") String localID,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getPrimary(ownerIDS, localID, formatType, cs, sc);
    }
    
    @GET
    @Path("local/{objectIDS}")
    public Response callLocal(
            @PathParam("objectIDS") String objectIDS,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return getLocal(objectIDS, formatType, cs, sc);
    }
    
    @POST
    @Path("admin/sla")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callAddAdminSLA(
            @DefaultValue("") @FormDataParam("adminid") String adminIDS,
            @DefaultValue("") @FormDataParam("name") String name,
            @DefaultValue("") @FormDataParam("mnemonic") String mnemonic,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return adminSLA(adminIDS, name, mnemonic, cs, sc);
    }
    
    @POST
    @Path("admin/owner")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callAddAdminOwner(
            @DefaultValue("") @FormDataParam("adminid") String adminIDS,
            @DefaultValue("") @FormDataParam("slaid") String slaIDS,
            @DefaultValue("") @FormDataParam("name") String name,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return adminOwner(adminIDS, slaIDS, name, cs, sc);
    }
    
    @POST
    @Path("admin/collection/{coltype}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callAddAdminCollection(
            @PathParam("coltype") String coltypeS,
            @DefaultValue("") @FormDataParam("adminid") String adminIDS,
            @DefaultValue("") @FormDataParam("name") String name,
            @DefaultValue("") @FormDataParam("mnemonic") String mnemonic,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        coltypeS = coltypeS.toLowerCase();
        boolean collectPrivate = true;
        if (coltypeS.equals("public")) {
            collectPrivate = false;
        }
        return adminCollection(collectPrivate, adminIDS, name, mnemonic, cs, sc);
    }
    
    @POST
    @Path("admin/init")
    public Response callAdminInit(
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        return addAdminInit( cs, sc);
    }
    
    @POST
    @Path("reset")
    public Response callResetState(
            @DefaultValue("-none-") @QueryParam("log4jlevel") String log4jlevel,
            @DefaultValue("xhtml") @QueryParam(KeyNameHttpInf.RESPONSEFORM) String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        try {
            log4j.info("getResetState entered:"
                    + " - formatType=" + formatType
                    + " - log4jlevel=" + log4jlevel
                    );
            if (!log4jlevel.equals("-none-")) {
                Log4j2Util.setRootLevel(log4jlevel);
            }
            return getServiceState(formatType, cs, sc);

        } catch (TException tex) {
            log4j.error(tex.toString(), tex);
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            log4j.error(ex.toString(), ex);
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    
    @POST
    @Path("task")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callAddTaskMultipart(
            @DefaultValue("") @FormDataParam("name") String taskName,
            @DefaultValue("") @FormDataParam("item") String taskItem,
            @DefaultValue("") @FormDataParam("status") String currentStatus,
            @DefaultValue("") @FormDataParam("note") String note,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        log4j.debug("addVersionMultipart entered"
                    + " - taskName=" + taskName + NL
                    + " - taskItem=" + taskItem + NL
                    + " - status=" + currentStatus + NL
                    + " - note=" + note + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        
        if (note.length() == 0) note = null;
        return addTask(
                taskName,
                taskItem,
                currentStatus,
                note,
                cs,
                sc);
    }
    
    @DELETE
    @Path("task")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callDeleteTaskMultipart(
            @DefaultValue("") @FormDataParam("name") String taskName,
            @DefaultValue("") @FormDataParam("item") String taskItem,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        log4j.debug("addVersionMultipart entered"
                    + " - taskName=" + taskName + NL
                    + " - taskItem=" + taskItem + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        
        return deleteTask(
                taskName,
                taskItem,
                cs,
                sc);
    }
    
    @GET
    @Path("task")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response callGetTaskMultipart(
            @DefaultValue("") @FormDataParam("name") String taskName,
            @DefaultValue("") @FormDataParam("item") String taskItem,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        log4j.debug(MESSAGE + "addVersionMultipart entered"
                    + " - taskName=" + taskName + NL
                    + " - taskItem=" + taskItem + NL
                    );
        if (DEBUG) System.out.println("addVersionMultipart entered");
        
        return getTask(
                taskName,
                taskItem,
                cs,
                sc);
    }
    
    /**
     * Get state information about a specific node
     * @param nodeID node identifier
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted service information
     * @throws TException
     */
    public Response getServiceState(
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getServiceState entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            StateInf responseState = invService.getInvServiceState();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    @POST
    @Path("process")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response processMultipart(
            @DefaultValue("") @FormDataParam("url") String url,
            @DefaultValue("") @FormDataParam("role") String roleS,
            @DefaultValue("xhtml") @FormDataParam("responseForm") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        
        LoggerInf logger = defaultLogger;
        try {
            log("process:"
                    + " - url=" + url
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            if (StringUtil.isAllBlank(roleS)) {
                throw new TException.REQUEST_INVALID("Role required for processMultipart");
            }
            Role role = Role.getRole(roleS);
            StateInf responseState = invService.process(role, url);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    @POST
    @Path("add")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addMultipartAdd(
            @DefaultValue("") @FormDataParam("url") String url,
            @DefaultValue("true") @FormDataParam("checkVersion") String doCheckVersionS,
            @DefaultValue("xhtml") @FormDataParam("responseForm") String formatType,
            @Context CloseableService cs,
            @Context ServletConfig sc)
        throws TException
    {
        
        LoggerInf logger = defaultLogger;
        try {
            log("process:"
                    + " - url=" + url
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            boolean doCheckVersion = setBool(doCheckVersionS, true, true, true);
            logger = invService.getLogger();
            StateInf responseState = invService.add(url, doCheckVersion);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    /**
     * Retart inv service
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted inv service information
     * @throws TException
     */
    public Response restartInv(
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("runFixity entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();

            StateInf responseState = invService.shutdown();
            
            invServiceInit = InvServiceInit.resetInvServiceInit(sc);
            invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            responseState = invService.startup();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    /**
     * Start inv service
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted inv service information
     * @throws TException
     */
    public Response stopInv(
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("runFixity entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            StateInf responseState = invService.shutdown();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    /**
     * Stop zookeeper service
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted inv service information
     * @throws TException
     */
    public Response stopZoo(
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("stopZoo entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            StateInf responseState = invService.shutdownZoo();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    /**
     * Stop inv service
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted inv service information
     * @throws TException
     */
    public Response startInv(
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("stopFixity entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            StateInf responseState = invService.startup();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
       public Response setFileNode(
            int nodeNum,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("stopFixity entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            StateInf responseState = invService.setFileNode(nodeNum);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }


    /**
     * delete inv entry
     * @param entry inv entry
     * @param formatType user provided format type
     * @param cs on close actions
     * @param sc ServletConfig used to get system configuration
     * @return formatted inv entry information
     * @throws TException
     */
    public Response delete(
            String objectIDS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("deleted entered:"
                    + " - objectIDS=" + objectIDS
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            InvDeleteState responseState = invService.delete(objectID);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getManifestUrl(
            String objectIDS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getManifestUrl entered:"
                    + " - objectIDS=" + objectIDS
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            InvManifestUrl responseState  = invService.getManifestUrl(objectID);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getVersions(
            String objectIDS,
            String versionS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getManifestUrl entered:"
                    + " - objectIDS=" + objectIDS
                    + " - versionS=" + versionS
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            Long version = null;
            if (!StringUtil.isAllBlank(versionS)) {
                version = Long.parseLong(versionS);
            }
            VersionsState responseState  = invService.getVersions(objectID, version);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getVersions(
            String objectIDS,
            String versionS,
            String fileID,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getManifestUrl entered:"
                    + " - objectIDS=" + objectIDS
                    + " - versionS=" + versionS
                    + " - versionS=" + fileID
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            Long version = null;
            if (!StringUtil.isAllBlank(versionS)) {
                version = Long.parseLong(versionS);
            }
            VersionsState responseState  = invService.getVersions(objectID, version, fileID);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            System.out.println("getVersions Exception:" + tex);
            tex.printStackTrace();
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getCurrent(
            String objectIDS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getManifestUrl entered:"
                    + " - objectIDS=" + objectIDS
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            VersionsState responseState  = invService.getCurrent(objectID);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            System.out.println("getVersions Exception:" + tex);
            tex.printStackTrace();
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response addPrimary(
            String objectIDS,
            String ownerIDS,
            String localIDs,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("addPrimaryLocal entered:"
                    + " - objectIDS=" + objectIDS
                    + " - ownerIDS=" + ownerIDS
                    + " - localID=" + localIDs
                    + " - formatType=" + formatType
                    );
            long durationStart = System.currentTimeMillis();
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            Identifier ownerID = new Identifier(ownerIDS);
            LocalContainerState responseState  = invService.addPrimary(objectID, ownerID, localIDs);
            LogInvPrimary invPrimary = LogInvPrimary.getLogInvPrimary("iaddprime", "InvAddPrimary", System.currentTimeMillis() - durationStart, responseState);
            invPrimary.addEntry();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response addLocalAfterTo(
            String afterS,
            String toS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("addPrimaryLocal entered:"
                    + " - afterS=" + afterS
                    + " - toS=" + toS
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            long after = Long.parseLong(afterS);
            long to = Long.parseLong(toS);
            LocalAfterToState responseState  = invService.addLocalFromTo(after, to);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response deletePrimary(
            String objectIDS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("addPrimaryLocal entered:"
                    + " - objectIDS=" + objectIDS
                    + " - formatType=" + formatType
                    );
            long durationStart = System.currentTimeMillis();
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            LocalContainerState responseState  = invService.deletePrimary(objectID);
            LogInvPrimary invPrimary = LogInvPrimary.getLogInvPrimary("idelprime", "InvDeletePrimary", System.currentTimeMillis() - durationStart, responseState);
            invPrimary.addEntry();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getPrimary(
            String ownerIDS,
            String localID,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getPrimary entered:"
                    + " - ownerIDS=" + ownerIDS
                    + " - localID=" + localID
                    + " - formatType=" + formatType
                    );
            long durationStart = System.currentTimeMillis();
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier ownerID = new Identifier(ownerIDS);
            LocalContainerState responseState  = invService.getPrimary(ownerID, localID);
            LogInvPrimary invPrimary = LogInvPrimary.getLogInvPrimary("igetprime", "InvGetPrimary", System.currentTimeMillis() - durationStart, responseState);
            invPrimary.addEntry();
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response getLocal(
            String objectIDS,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getLocal entered:"
                    + " - objectIDS=" + objectIDS
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier objectID = new Identifier(objectIDS);
            LocalContainerState responseState  = invService.getLocal(objectID);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }

    protected Response getSelectReport(
            String select,
            String formatType,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("getSelectReport entered:"
                    + " - formatType=" + formatType
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();

            StateInf responseState = invService.select(select);
            return getStateResponse(responseState, formatType, logger, cs, sc);

        } catch (TException tex) {
            return getExceptionResponse(tex, formatType, logger);

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
   
    public Response adminSLA(
            String adminIDS,
            String name,
            String mnemonic,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
         LoggerInf logger = defaultLogger;
        try {
            log("addTask entered:"
                    + " - adminIDS=" + adminIDS
                    + " - name=" + name
                    + " - mnemonic=" + mnemonic
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier adminID = new Identifier(adminIDS);
            
            JSONObject jsonResponse = invService.addAdminSLA(adminID, name, mnemonic);
            //log4j.debug(jsonResponse);
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
   
    public Response adminOwner(
            String adminIDS,
            String slaIDS,
            String name,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
         LoggerInf logger = defaultLogger;
        try {
            log("adminOwner entered:"
                    + " - adminIDS=" + adminIDS
                    + " - slaIDS=" + slaIDS
                    + " - name=" + name
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier adminID = new Identifier(adminIDS);
            Identifier slaID = new Identifier(slaIDS);
            
            JSONObject jsonResponse = invService.addAdminOwner(adminID, slaID, name);
            //log4j.info(jsonResponse.toString());
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
   
    public Response adminCollection(
            boolean collectPrivate,
            String adminIDS,
            String name,
            String mnemonic,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
         LoggerInf logger = defaultLogger;
        try {
            log("adminCollection entered:"
                    + " - colpriv=" + collectPrivate
                    + " - adminIDS=" + adminIDS
                    + " - name=" + name
                    + " - mnemonic=" + mnemonic
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            Identifier adminID = new Identifier(adminIDS);
            
            JSONObject jsonResponse = invService.addAdminCollection(collectPrivate, adminID, name, mnemonic);
            //log4j.debug(jsonResponse);
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
   
    public Response addAdminInit(
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
         LoggerInf logger = defaultLogger;
        try {
            log("adminInit entered:");
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            
            JSONObject jsonResponse = invService.addAdminInit();
            //log4j.debug(jsonResponse);
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    public Response addTask(
            String taskName,
            String taskItem,
            String currentStatusS,
            String note,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("addTask entered:"
                    + " - taskName=" + taskName
                    + " - taskItem=" + taskItem
                    + " - currentStatusS=" + currentStatusS
                    + " - note=" + note
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            JSONObject jsonResponse = invService.addTask(taskName, taskItem, currentStatusS, note);
            log4j.debug(jsonResponse);
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
    
    
    
    public Response deleteTask(
            String taskName,
            String taskItem,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("addTask entered:"
                    + " - taskName=" + taskName
                    + " - taskItem=" + taskItem
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            JSONObject jsonResponse = invService.deleteTask(taskName, taskItem);
            log4j.debug(jsonResponse);
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }   
    
    public Response getTask(
            String taskName,
            String taskItem,
            CloseableService cs,
            ServletConfig sc)
        throws TException
    {
        LoggerInf logger = defaultLogger;
        try {
            log("addTask entered:"
                    + " - taskName=" + taskName
                    + " - taskItem=" + taskItem
                    );
            InvServiceInit invServiceInit = InvServiceInit.getInvServiceInit(sc);
            InvServiceInf invService = invServiceInit.getInvService();
            logger = invService.getLogger();
            JSONObject jsonResponse = invService.getTask(taskName, taskItem);
            log4j.debug(jsonResponse);
            return Response 
                .status(200).entity(jsonResponse.toString())
                    .build();      
            

        } catch (TException tex) {
            throw tex;

        } catch (Exception ex) {
            System.out.println("TRACE:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION(MESSAGE + "Exception:" + ex);
        }
    }
}
