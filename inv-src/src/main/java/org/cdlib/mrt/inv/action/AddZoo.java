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

import java.util.Properties;
import org.apache.zookeeper.ZooKeeper;

import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.core.ProcessStatus;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.inv.zoo.ZooManager;
import org.cdlib.mrt.utility.ZooCodeUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.zk.Batch;
import org.cdlib.mrt.zk.Job;
import org.cdlib.mrt.zk.JobState;
import org.cdlib.mrt.zk.MerrittJsonKey;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Run fixity
 * @author dloy
 */
public class AddZoo
        //extends InvActionAbs
        implements Runnable
{

    protected static final String NAME = "AddZoo";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = false;
    protected static final boolean LIST = true;
    
    
    protected ZooManager zooManager = null;
    protected boolean sent = false;
    protected ProcessStatus processStatus = null;
    protected byte[] bytes = null;
    protected Exception saveEx = null;
    protected String manifestUrl = null;
    protected LoggerInf logger = null;
 
    protected static final Logger log4j = LogManager.getLogger();   
    
    public static AddZoo getAddZoo(
            String manifestUrl,
            ZooManager zooManager)
        throws Exception
    {
        return new AddZoo(zooManager).setManifestUrl(manifestUrl);
    }
    public static AddZoo getAddZoo(
            ZooManager zooManager)
        throws Exception
    {
        return new AddZoo(zooManager);
    }
    
    protected AddZoo(
            ZooManager zooManager)
        throws Exception
    {
        this.logger = zooManager.getLogger();
        this.zooManager = zooManager;
    }

    public void run()
    {
        if (!StringUtil.isAllBlank(manifestUrl)) {
            processStatus = addUrl(manifestUrl);
        }

    }
   
    public ProcessStatus addUrl(String manifestUrl)
    {
        try {
            ServiceStatus runStatus = zooManager.getZookeeperStatus();
            if (runStatus != ServiceStatus.running) {
                return ProcessStatus.shutdown;
            }
            processStatus = ProcessStatus.queued;
            //ZooKeeper zk = zooManager.setZoo();
            ZooKeeper zk = zooManager.getZooKeeper();
            Batch b = Batch.createBatch(zk, fooBar());
            Batch bb = Batch.acquirePendingBatch(zk);
            
            Job j = Job.createJob(zk, bb.id(), quack());
            Job jj = Job.acquireJob(zk, JobState.Pending);
            jj.setStatus(zk, jj.status().stateChange(JobState.Estimating));
            jj.unlock(zk);
            jp("After Pending", jj);
            
            jj = Job.acquireJob(zk, JobState.Estimating);
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            jp("After Estimating", jj);
            
            jj = Job.acquireJob(zk, JobState.Provisioning);
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            jp("After Provisioning", jj);
            
            jj = Job.acquireJob(zk, JobState.Downloading);
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            jp("After Downloading", jj);
            
            jj = Job.acquireJob(zk, JobState.Processing);
            jj.setInventory(zk, manifestUrl, "tbd");
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            jp("After Processing", jj);
            
            saveEx = null;
            return ProcessStatus.completed;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            saveEx = ex;
            return ProcessStatus.failed;
        }
    }
 
    public int addList(String listFilePath)
        throws Exception
    {
        int successCnt = 0;
        try {
            List<String> listOfStrings
                = new ArrayList<String>();

            // load the data from file
            listOfStrings
                = Files.readAllLines(Paths.get(listFilePath));

            // print each line of string in array
            for (String urlString :listOfStrings) {
                ProcessStatus result = addUrl(urlString);
                if (result == ProcessStatus.completed) {
                    successCnt++;
                }
            }
            
        } catch (Exception ex) {
            log4j.debug("AddZoo exception:" + ex, ex);
        
        } finally {
            return successCnt;
        }
    }
    
    public AddZoo setManifestUrl(String manifestUrl)
    {
        this.manifestUrl = manifestUrl;
        return this;
    }
    
    public boolean isSent()
    {
        return sent;
    }

    public Exception getEx() {
        return saveEx;
    }

    private JSONObject quack() {
        UUID uuid = UUID.randomUUID();
      return quack(uuid.toString());
    }

    private JSONObject quack(String suffix) {
      JSONObject json = new JSONObject();
      json.put("job", "quack"+suffix);
      return json;
    }
    
    private JSONObject fooBar() {
        UUID uuid = UUID.randomUUID();
      return quack(uuid.toString());
    }

    private JSONObject fooBar(String suffix) {
      return fooBar(suffix, "bid-uuid");
    }

    private JSONObject fooBar(String suffix, String uuid) {
      JSONObject json = new JSONObject();
      json.put("foo", "bar" + suffix);
      json.put(MerrittJsonKey.BatchId.key(), uuid);
      return json;
    }
    
    public static void jp(String msg, Job job)
    {
        log4j.trace("***" + msg + "***\n"
                + " - id=" + job.id() + "\n"
                + " - bid=" + job.bid() + "\n"
                + " - status=" + job.status() + "\n"
                + " - priority=" + job.priority() + "\n"
                + " - profileName:" + job.profileName() + "\n"
                + " - submitter:" + job.submitter() + "\n"
                + " - payloadUrl:" + job.payloadUrl() + "\n"
                + " - inventoryManifestUrl:" + job.inventoryManifestUrl() + "\n"
                + " - inventoryMode:" + job.inventoryMode()+ "\n"
                + " - payloadType:" + job.payloadType() + "\n"
                + " - responseType:" + job.responseType() + "\n"
                + " - data:\n" + job.data().toString(2) + "\n"
        );
    }
}

