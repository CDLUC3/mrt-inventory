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

import java.util.Properties;

import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.StateInf;

/**
 * Format container class for Fixity Service
 * @author dloy
 */
public class InvServiceState
        implements StateInf
{
    private static final String NAME = "InvServiceState";
    private static final String MESSAGE = NAME + ": ";

    protected String name = null;
    protected String identifier = null;
    protected String description = null;
    protected String baseURI = null;
    protected ServiceStatus zookeeperStatus = ServiceStatus.unknown;
    protected ServiceStatus dbStatus = ServiceStatus.unknown;
    protected ServiceStatus systemStatus = ServiceStatus.unknown;
    
    public InvServiceState() { }

    public InvServiceState(Properties prop)
    {
        setValues(prop);
    }

    /**
     * @return base URL for Fixity Service
     */
    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    /**
     * 
     * @return non required description of entry
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * 
     * @return Name of fixity service
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDbStatus() {
        return dbStatus.toString();
    }

    public ServiceStatus retrieveDbStatus() {
        return dbStatus;
    }

    public void setDbStatus(String dbStatusS) {
        if (dbStatusS == null) this.dbStatus = null;
        this.dbStatus = ServiceStatus.valueOf(dbStatusS);
    }

    public void setDbStatus(ServiceStatus dbStatus) {
        this.dbStatus = dbStatus;
    }

    public String getZookeeperStatus() {
        return zookeeperStatus.toString();
    }

    public ServiceStatus retrieveZookeeperStatus() {
        return zookeeperStatus;
    }

    public void setZookeeperStatus(String zookeeperStatusS) {
        if (zookeeperStatusS == null) this.zookeeperStatus = null;
        this.zookeeperStatus = ServiceStatus.valueOf(zookeeperStatusS);
    }

    public void setZookeeperStatus(ServiceStatus zookeeperStatus) {
        this.zookeeperStatus = zookeeperStatus;
    }

    public String getSystemStatus() {
        return systemStatus.toString();
    }

    public ServiceStatus retrieveSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatusS) {
        if (systemStatusS == null) this.systemStatus = null;
        this.systemStatus = ServiceStatus.valueOf(systemStatusS);
    }

    public void setSystemStatus(ServiceStatus systemStatus) {
        this.systemStatus = systemStatus;
    }

    /**
     * Set all entry values based on Properties
     * @param prop 
     */
    public void setValues(Properties prop)
    {
        setName(prop.getProperty("name"));
        setIdentifier(prop.getProperty("id"));
        setDescription(prop.getProperty("description"));
        setBaseURI(prop.getProperty("baseURI"));
    }

    public DateState getCurrentReportDate()
    {
        return new DateState();
    }
}
