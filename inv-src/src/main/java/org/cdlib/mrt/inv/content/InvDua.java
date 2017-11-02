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
import java.util.Properties;


import org.cdlib.mrt.inv.utility.InvUtil;
import org.cdlib.mrt.core.DateState;
import org.cdlib.mrt.core.Identifier;
import org.cdlib.mrt.utility.DateUtil;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.XMLUtil;
import org.cdlib.mrt.utility.XSLTUtil;
/**
 * Container class for inv duas content
 * @author dloy
 */
public class InvDua
        extends ContentAbs
{
    private static final String NAME = "InvDua";
    private static final String MESSAGE = NAME + ": ";
    
    /*
     * CREATE  TABLE IF NOT EXISTS `duas` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection_id` SMALLINT UNSIGNED NULL ,
  `object_id` INT UNSIGNED NULL ,
  `title` VARCHAR(255) NOT NULL ,
  `terms` VARCHAR(16383) NOT NULL ,
  `termplate` TEXT NOT NULL ,
  `accept_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `name_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `affiliation_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `email_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `applicability` ENUM('collection', 'object', 'version', 'file') NOT NULL ,
  `persistence` ENUM('request', 'session', 'permanent') NOT NULL ,
  `notification` VARCHAR(255) NOT NULL ,

     */

    protected long id = 0;
    protected long collectionID = 0;
    protected long objectID = 0;
    protected String identifier = null;
    protected String title = null;
    protected String terms = null;
    protected String template = null;
    protected AcceptObligation acceptObligation = null;
    protected NameObligation nameObligation = null;
    protected AffiliationObligation affiliationObligation = null;
    protected EmailObligation emailObligation = null;
    protected Applicability applicability = null;
    protected Persistence persistence = null;
    protected String notification = null;
    public boolean newEntry = false;
    
    
    
    public enum AcceptObligation
    {
        // 'required', 'optional', 'none'
        required,
        optional,
        none;
    }    
    public enum NameObligation
    {
        // 'required', 'optional', 'none'
        required,
        optional,
        none;
    }
    public enum AffiliationObligation
    {
        // 'required', 'optional', 'none'
        required,
        optional,
        none;
    }
    public enum EmailObligation
    {
        // 'required', 'optional', 'none'
        required,
        optional,
        none;
    }
    public enum Applicability
    {
        // ('collection', 'object', 'version', 'file') 
        collection,
        object,
        version,
        file;
    }
    public enum Persistence
    {
        // ('request', 'session', 'permanent'
        request,
        session,
        permanent;
    }
    
    public InvDua(LoggerInf logger)
        throws TException
    { 
        super(logger);
    }
    
    public InvDua(Properties prop, LoggerInf logger)
        throws TException
    {
        super(logger);
        setProp(prop);
    }

    public void setDuaFile(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setTitle(prop.getProperty("Title"));
            setTerms(prop.getProperty("Terms"));
            setTemplate(prop.getProperty("template"));
            setAcceptObligation(prop.getProperty("Accept"));
            setNameObligation(prop.getProperty("Name"));
            setAffiliationObligation(prop.getProperty("Affiliation"));
            setEmailObligation(prop.getProperty("Email"));
            setApplicability(prop.getProperty("Applicability"));
            setPersistence(prop.getProperty("Persistence"));
            setNotification(prop.getProperty("Notification"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public void setProp(Properties prop)
        throws TException
    {
        if ((prop == null) || (prop.size() == 0)) return;
        try {
            setId(prop.getProperty("id"));
            setCollectionID(prop.getProperty("inv_collection_id"));
            setObjectID(prop.getProperty("inv_object_id"));
            setIdentifier(prop.getProperty("identifier"));
            setTitle(prop.getProperty("title"));
            setTerms(prop.getProperty("terms"));
            setTemplate(prop.getProperty("template"));
            setAcceptObligation(prop.getProperty("accept_obligation"));
            setNameObligation(prop.getProperty("name_obligation"));
            setAffiliationObligation(prop.getProperty("affiliation_obligation"));
            setEmailObligation(prop.getProperty("email_obligation"));
            setApplicability(prop.getProperty("applicability"));
            setPersistence(prop.getProperty("persistence"));
            setNotification(prop.getProperty("notification"));
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }

    public Properties retrieveProp()
        throws TException
    {
        Properties prop = new Properties();
        if (getId() != 0) prop.setProperty("id", "" + getId());
        if (getObjectID() > 0) prop.setProperty("inv_object_id", "" + getObjectID());
        if (getCollectionID() > 0) prop.setProperty("inv_collection_id", "" + getCollectionID());
        if (getIdentifier() != null) prop.setProperty("identifier", getIdentifier());
        if (getTitle() != null) prop.setProperty("title", getTitle());
        if (getTerms() != null) prop.setProperty("terms", getTerms());
        if (getTemplate() != null) prop.setProperty("template", getTemplate());
        if (getAcceptObligation() != null) prop.setProperty("accept_obligation", getAcceptObligation().toString());
        if (getNameObligation() != null) prop.setProperty("name_obligation", getNameObligation().toString());
        if (getEmailObligation() != null) prop.setProperty("email_obligation", getEmailObligation().toString());
        if (getApplicability() != null) prop.setProperty("applicability", getApplicability().toString());
        if (getPersistence() != null) prop.setProperty("persistence", getPersistence().toString());
        if (getNotification() != null) prop.setProperty("notification", getNotification());
        return prop;
    }
    
    public String getDBName()
    {
        return DUAS;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(String idS) 
    {
        this.id = setNum(idS);
    }

    public long getObjectID() {
        return objectID;
    }

    public void setObjectID(long objectID) {
        this.objectID = objectID;
    }

    public void setObjectID(String objectIDS) {
        this.objectID = setNum(objectIDS);
    }

    public long getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(long collectionID) {
        this.collectionID = collectionID;
    }

    public void setCollectionID(String collectionIDS) {
        this.collectionID = setNum(collectionIDS);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public AcceptObligation getAcceptObligation() {
        return acceptObligation;
    }

    public void setAcceptObligation(AcceptObligation acceptObligation) {
        this.acceptObligation = acceptObligation;
    }

    public void setAcceptObligation(String acceptObligationS) {
        if (acceptObligationS == null) return;
        this.acceptObligation = AcceptObligation.valueOf(acceptObligationS);
    }

    public NameObligation getNameObligation() {
        return nameObligation;
    }

    public void setNameObligation(NameObligation nameObligation) {
        this.nameObligation = nameObligation;
    }

    public void setNameObligation(String nameObligationS) {
        if (nameObligationS == null) return;
        this.nameObligation = NameObligation.valueOf(nameObligationS);
    }
    

    public AffiliationObligation getAffiliationObligation() {
        return affiliationObligation;
    }

    public void setAffiliationObligation(AffiliationObligation affiliationObligation) {
        this.affiliationObligation = affiliationObligation;
    }

    public void setAffiliationObligation(String affiliationObligationS) {
        if (affiliationObligationS == null) return;
        this.affiliationObligation = AffiliationObligation.valueOf(affiliationObligationS);
    }

    public Applicability getApplicability() {
        return applicability;
    }

    public void setApplicability(Applicability applicability) {
        this.applicability = applicability;
    }

    public void setApplicability(String applicabilityS) {
        if (applicabilityS == null) return;
        this.applicability = Applicability.valueOf(applicabilityS);
    }

    public EmailObligation getEmailObligation() {
        return emailObligation;
    }

    public void setEmailObligation(EmailObligation emailObligation) {
        this.emailObligation = emailObligation;
    }

    public void setEmailObligation(String emailObligationS) {
        if (emailObligationS == null) return;
        this.emailObligation = EmailObligation.valueOf(emailObligationS);
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

    public void setPersistence(String persistenceS) {
        if (persistenceS == null) return;
        this.persistence = Persistence.valueOf(persistenceS);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isNewEntry() {
        return newEntry;
    }

    public void setNewEntry(boolean newEntry) {
        this.newEntry = newEntry;
    }
    
}

