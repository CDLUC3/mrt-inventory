
package org.cdlib.mrt.inv;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.cdlib.mrt.utility.HTTPUtil;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.zk.Batch;
import org.cdlib.mrt.zk.Job;
import org.cdlib.mrt.zk.JobState;
import org.cdlib.mrt.zk.MerrittStateError;
import org.cdlib.mrt.zk.MerrittZKNodeInvalid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathFactory;
//https://stackoverflow.com/a/22939742/3846548
import org.apache.xpath.jaxp.XPathFactoryImpl;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

import static org.junit.Assert.*;

public class ServiceDriverIT {
        private int port = 8080;
        private int dbport = 9999;
        private int primaryNode = 7777;
        private int replNode = 8888;
        private int zkport = 8084;

        //https://github.com/CDLUC3/merritt-docker/blob/main/mrt-inttest-services/mock-merritt-it/data/system/mrt-owner.txt
        private String owner = "ark:/99999/owner";
        //https://github.com/CDLUC3/merritt-docker/blob/main/mrt-inttest-services/mock-merritt-it/data/system/mrt-membership.txt
        private String collection = "ark:/99999/collection";
        //https://github.com/CDLUC3/merritt-docker/blob/main/mrt-inttest-services/mock-merritt-it/data/system/mrt-erc.txt
        private String title = "Hello File";
        //https://github.com/CDLUC3/merritt-docker/blob/main/mrt-inttest-services/mock-merritt-it/data/producer/mrt-dc.xml
        private String mdfilename = "producer/mrt-dc.xml";

        private String cp = "mrtinv";
        private DocumentBuilder db;
        private XPathFactory xpathfactory;
        private ZooKeeper zk;

        private String connstr;
        private String user = "user";
        private String password = "password";
    
        public ServiceDriverIT() throws ParserConfigurationException, HttpResponseException, IOException, JSONException, SQLException {
                try {
                        port = Integer.parseInt(System.getenv("it-server.port"));
                        dbport = Integer.parseInt(System.getenv("mrt-it-database.port"));
                        zkport = Integer.parseInt(System.getenv("mrt-zk.port"));
                } catch (NumberFormatException e) {
                        System.err.println("it-server.port not set, defaulting to " + port);
                }
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                xpathfactory = new XPathFactoryImpl();
                zk = new ZooKeeper(String.format("localhost:%s", zkport), 100, null);

                connstr = String.format("jdbc:mysql://localhost:%d/inv?characterEncoding=UTF-8&characterSetResults=UTF-8&useSSL=false&serverTimezone=UTC", dbport);
                initServiceAndNodes();
       }

        public void checkInvDatabase(String sql, int n, int value) throws SQLException {
                try(Connection con = DriverManager.getConnection(connstr, user, password)){
                        try (PreparedStatement stmt = con.prepareStatement(sql)){
                                stmt.setInt(1, n);
                                ResultSet rs=stmt.executeQuery();
                                while(rs.next()) {
                                        assertEquals(value, rs.getInt(1));  
                                }  
                        }
                }
        }

        public void checkInvDatabase(String sql, String ark, int value) throws SQLException {
                try(Connection con = DriverManager.getConnection(connstr, user, password)){
                        try (PreparedStatement stmt = con.prepareStatement(sql)){
                                stmt.setString(1, ark);
                                ResultSet rs=stmt.executeQuery();
                                while(rs.next()) {
                                        assertEquals(value, rs.getInt(1));  
                                }  
                        }
                }
        }

        public void checkInvDatabase(String sql, String ark, String ark2, int value) throws SQLException {
                try(Connection con = DriverManager.getConnection(connstr, user, password)){
                        try (PreparedStatement stmt = con.prepareStatement(sql)){
                                stmt.setString(1, ark);
                                stmt.setString(2, ark2);
                                ResultSet rs=stmt.executeQuery();
                                while(rs.next()) {
                                        assertEquals(value, rs.getInt(1));  
                                }  
                        }
                }
        }

        public void checkInvDatabase(String sql, String ark, int n, int value) throws SQLException {
                try(Connection con = DriverManager.getConnection(connstr, user, password)){
                        try (PreparedStatement stmt = con.prepareStatement(sql)){
                                stmt.setString(1, ark);
                                stmt.setInt(2, n);
                                ResultSet rs=stmt.executeQuery();
                                while(rs.next()) {
                                        assertEquals(value, rs.getInt(1));  
                                }  
                        }
                }
        }

        public void initServiceAndNodes() throws IOException, JSONException, SQLException {
                String url = String.format("http://localhost:%d/%s/service/start?t=json", port, cp);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        HttpResponse response = client.execute(post);
                        HTTPUtil.dumpHttpResponse(response, 200); //!!!!!
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());

                        JSONObject json =  new JSONObject(s);
                        assertNotNull(json);
                }
                setFileNode(primaryNode);
                checkInvDatabase("select count(*) from inv_nodes where number=?", primaryNode, 1);
                setFileNode(replNode);
                checkInvDatabase("select count(*) from inv_nodes where number=?", replNode, 1);
        }

        public String getContent(String url, int status) throws HttpResponseException, IOException {
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet(url);
                    HttpResponse response = client.execute(request);
                    if (status > 0) {
                        assertEquals(status, response.getStatusLine().getStatusCode());
                    }

                    if (status > 300) {
                        return "";
                    }
                    String s = new BasicResponseHandler().handleResponse(response).trim();
                    assertFalse(s.isEmpty());
                    return s;
                }
        }

        public JSONObject getJsonContent(String url, int status) throws HttpResponseException, IOException, JSONException {
                String s = getContent(url, status);
                JSONObject json = s.isEmpty() ? new JSONObject() : new JSONObject(s);
                assertNotNull(json);
                return json;
        }

        @Test
        public void SimpleTest() throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/state?t=json", port, cp);
                JSONObject json = getJsonContent(url, 200);
                assertTrue(json.has("invsv:invServiceState"));
                assertEquals("running", json.getJSONObject("invsv:invServiceState").get("invsv:systemStatus"));
                assertEquals("running", json.getJSONObject("invsv:invServiceState").get("invsv:zookeeperStatus"));
                assertEquals("running", json.getJSONObject("invsv:invServiceState").get("invsv:dbStatus"));
                
        }

        public JSONObject setFileNode(int node) throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/filenode/%d?t=json", port, cp, node);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());

                        return new JSONObject(s);
                }
        }

        public JSONObject addObject(String ark) throws IOException, JSONException {
                String ark_e = URLEncoder.encode(ark, StandardCharsets.UTF_8.name());
                String manifest = String.format("http://mock-merritt-it:4567/storage/manifest/7777/%s", ark_e);
                String url = String.format("http://localhost:%d/%s/add", port, cp);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.addTextBody("responseForm", "json", ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        builder.addTextBody("url", manifest, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        builder.addTextBody("checkVersion", "false", ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        HttpEntity entity = builder.build();
                        post.setEntity(entity);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());
                        JSONObject json = new JSONObject(s);
                        assertTrue(json.has("invp:invProcessState"));
                        return json;
                }
        }

        public JSONObject setLocalIdByFormParams(String ark, String owner, String localid) throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/primary", port, cp);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.addTextBody("response-form", "json", ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        builder.addTextBody("objectid", ark, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        builder.addTextBody("ownerid", owner, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        builder.addTextBody("localids", localid, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        HttpEntity entity = builder.build();
                        post.setEntity(entity);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());
                        JSONObject json = new JSONObject(s);
                        assertTrue(json.has("invloc:localContainerState"));
                        assertEquals(owner, json.getJSONObject("invloc:localContainerState").getString("invloc:ownerID"));
                        assertEquals(localid, json.getJSONObject("invloc:localContainerState").getString("invloc:localIDs"));
                        return json;
                }
        }

        public JSONObject setLocalId(String ark, String owner, String localid) throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/primary/%s/%s/%s?t=json", 
                        port,
                        cp,
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name()),
                        URLEncoder.encode(owner, StandardCharsets.UTF_8.name()),
                        URLEncoder.encode(localid, StandardCharsets.UTF_8.name())
                );
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());
                        JSONObject json = new JSONObject(s);
                        assertTrue(json.has("invloc:localContainerState"));
                        assertEquals(owner, json.getJSONObject("invloc:localContainerState").getString("invloc:ownerID"));
                        assertEquals(localid, json.getJSONObject("invloc:localContainerState").getString("invloc:localIDs"));
                        return json;
                }
        }

        public JSONObject deleteObject(String ark) throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/object/%s?t=json",
                        port,
                        cp,
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name())
                );
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpDelete del = new HttpDelete(url);
                        HttpResponse response = client.execute(del);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());
                        JSONObject json = new JSONObject(s);
                        assertTrue(json.has("invd:invDeleteState"));
                        return json;
                }
        }

        public JSONObject deleteLocalids(String ark) throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/primary/%s?t=json",
                        port,
                        cp,
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name())
                );
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpDelete del = new HttpDelete(url);
                        HttpResponse response = client.execute(del);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());
                        JSONObject json = new JSONObject(s);
                        assertTrue(json.has("invloc:localContainerState"));
                        return json;
                }
        }

        public boolean checkArk(String ark) throws HttpResponseException, IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/manurl/%s?t=json", 
                        port, 
                        cp, 
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name())
                );
                try{
                        JSONObject json = getJsonContent(url, 0);
                        JSONObject j = json.has("invman:invManifestUrl") ? json.getJSONObject("invman:invManifestUrl") : new JSONObject();
                        String ja = j.has("invman:objectID") ? j.getString("invman:objectID") : "";
                        assertEquals(ark, ja);
                        return true;
                } catch(Exception e) {
                        return false;
                }
        }

        @Test
        public void AddObjectTest() throws IOException, JSONException, SQLException {
                String ark = "ark:/1111/2222";
                if (checkArk(ark)) {
                        deleteObject(ark);
                }
                assertFalse(checkArk(ark));
                addObject(ark);
                getVersion(ark, primaryNode, 1);
                assertTrue(checkArk(ark));

                //the following will not be deleted after the test
                checkInvDatabase(inv_owner_sql(), owner, 1);
                checkInvDatabase(inv_collection_sql(), collection, 1);

                //the following will be deleted after the test
                checkInvDatabase(inv_object_sql(), ark, owner, 1);
                checkInvDatabase(inv_object_title_sql(), ark, title, 1);
                checkInvDatabase(inv_dublinkernels_sql(), ark, title, 1);
                checkInvDatabase(inv_metadatas_sql(), ark, mdfilename, 1);
                checkInvDatabase(inv_files_sql(), ark, 7);
                checkInvDatabase(inv_collections_inv_objects_sql(), ark, collection, 1);
                checkInvDatabase(inv_nodes_inv_objects_sql(), ark, primaryNode, 1);
                checkInvDatabase(inv_audits_sql(), ark, primaryNode, 7);

                deleteObject(ark);

                checkInvDatabase(inv_object_sql(), ark, owner, 0);
                checkInvDatabase(inv_object_title_sql(), ark, title, 0);
                checkInvDatabase(inv_dublinkernels_sql(), ark, title, 0);
                checkInvDatabase(inv_metadatas_sql(), ark, mdfilename, 0);
                checkInvDatabase(inv_files_sql(), ark, 0);
                checkInvDatabase(inv_collections_inv_objects_sql(), ark, collection, 0);
                checkInvDatabase(inv_nodes_inv_objects_sql(), ark, primaryNode, 0);
                checkInvDatabase(inv_audits_sql(), ark, primaryNode, 0);
        }

        public String inv_object_sql() {
                return "select count(*) from inv_objects where ark=? and inv_owner_id=" +
                        "(select id from inv_owners where ark=?)";
        }

        public String inv_object_title_sql() {
                return "select count(*) from inv_objects where ark=? and erc_what=?";
        }

        public String inv_dublinkernels_sql() {
                return "select count(*) from inv_dublinkernels where inv_object_id=" +
                        "(select id from inv_objects where ark=?)" +
                        " and element='what' and value=?";
        }

        public String inv_metadatas_sql() {
                return "select count(*) from inv_metadatas where inv_object_id=" +
                        "(select id from inv_objects where ark=?)" +
                        " and filename=?";
        }

        public String inv_files_sql() {
                return "select count(*) from inv_files where inv_object_id=" +
                        "(select id from inv_objects where ark=?)";
        }

        public String inv_owner_sql() {
                return "select count(*) from inv_owners where ark=?";
        }

        public String inv_collection_sql() {
                return "select count(*) from inv_collections where ark=?";
        }

        public String inv_collections_inv_objects_sql() {
                return "select count(*) from inv_collections_inv_objects where inv_object_id=" + 
                        "(select id from inv_objects where ark=?)" +
                        " and inv_collection_id=" +
                        "(select id from inv_collections where ark=?)";
        }

        public String inv_nodes_inv_objects_sql() {
                return "select count(*) from inv_nodes_inv_objects where inv_object_id=" + 
                        "(select id from inv_objects where ark=?)" +
                        " and inv_node_id=" +
                        "(select id from inv_nodes where number=?)";
        }

        public String inv_audits_sql() {
                return "select count(*) from inv_audits where inv_object_id=" + 
                        "(select id from inv_objects where ark=?)" +
                        " and inv_node_id=" +
                        "(select id from inv_nodes where number=?)";
        }

        @Test
        public void AddObjectV3Test() throws IOException, JSONException {
                // The v3 prefix triggers the mock-merritt-it container to return a multi-version manifest
                String ark = "ark:/v311/2233";
                if (checkArk(ark)) {
                        deleteObject(ark);
                }
                assertFalse(checkArk(ark));
                addObject(ark);
                getVersion(ark, primaryNode, 2);
                assertTrue(checkArk(ark));
                deleteObject(ark);
        }

        @Test
        public void AddObjectToZkTest() 
                throws IOException, JSONException, MerrittZKNodeInvalid, KeeperException, InterruptedException, MerrittStateError 
        {
            String ark = "ark:/1111/3333";
            if (checkArk(ark)) {
                        deleteObject(ark);
            }
            
            String ark_e = URLEncoder.encode(ark, StandardCharsets.UTF_8.name());
            String manifest = String.format("http://mock-merritt-it:4567/storage/manifest/7777/%s", ark_e);
            JSONObject json = new JSONObject();
            json.put("job", "quack");
            Batch b = Batch.createBatch(zk, json);
            Batch bb = Batch.acquirePendingBatch(zk);
            
            Job j = Job.createJob(zk, bb.id(), json);
            Job jj = Job.acquireJob(zk, JobState.Pending);
            jj.setStatus(zk, jj.status().stateChange(JobState.Estimating));
            jj.unlock(zk);
            
            jj = Job.acquireJob(zk, JobState.Estimating);
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            
            jj = Job.acquireJob(zk, JobState.Provisioning);
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            
            jj = Job.acquireJob(zk, JobState.Downloading);
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);
            
            jj = Job.acquireJob(zk, JobState.Processing);
            jj.setInventory(zk, manifest, "tbd");
            jj.setStatus(zk, jj.status().success());
            jj.unlock(zk);

            if (false) return;
            boolean complete = false;
            for(int i=0; i<20; i++) {
                Thread.sleep(1000);
                boolean found = checkArk(ark);
                if (found) break;
                //System.out.println(i + " Test");
            }
  
            assertTrue(checkArk(ark));
            deleteObject(ark);
            
            
            Job job = new Job(jj.id());
            job.load(zk);
            //System.out.println("job status:"  + job.status());
            assertTrue(job.status() == JobState.Notify);
        }

        @Test
        public void AddObjectWithLocalIdTest() throws IOException, JSONException {
                String ark = "ark:/1111/4444";
                String localid = "localid";
                String owner = "owner";
                if (checkArk(ark)) {
                        deleteObject(ark);
                        deleteLocalids(ark);
                }
                assertFalse(checkArk(ark));
                addObject(ark);
                assertTrue(checkArk(ark));
                getVersion(ark, primaryNode, 1);
                getLocalids(localid, owner, false, ark);
                getLocalidsByArk(ark, false);
                setLocalId(ark, owner, localid);
                getLocalids(localid, owner, true, ark);
                getLocalidsByArk(ark, true);
                deleteObject(ark);
                deleteLocalids(ark);
                getLocalids(localid, owner, false, ark);
                getLocalidsByArk(ark, false);
        }

        @Test
        public void AddObjectWithMultipleLocalIdTest() throws IOException, JSONException {
                String ark = "ark:/1111/4444";
                String localid = "localid";
                String localid_v2 = "localid;localid2";
                String localid_v3 = "localid;localid3";
                String owner = "owner";
                if (checkArk(ark)) {
                        deleteObject(ark);
                        deleteLocalids(ark);
                }
                assertFalse(checkArk(ark));
                addObject(ark);
                assertTrue(checkArk(ark));
                getVersion(ark, primaryNode, 1);
                getLocalids(localid, owner, false, ark);
                Set<String> set = getLocalidsByArk(ark, false);
                assertEquals(0, set.size());

                setLocalId(ark, owner, localid);
                getLocalids(localid, owner, true, ark);
                set = getLocalidsByArk(ark, true);
                assertEquals(1, set.size());
                assertTrue(set.contains(localid));
                
                setLocalId(ark, owner, localid_v2);
                getLocalids(localid, owner, true, ark);
                set = getLocalidsByArk(ark, true);
                assertEquals(2, set.size());
                for(String s: localid_v2.split(";")) {
                        assertTrue(set.contains(s));
                }
                
                setLocalId(ark, owner, localid_v3);
                getLocalids(localid, owner, true, ark);
                set = getLocalidsByArk(ark, true);
                assertEquals(3, set.size());
                for(String s: localid_v3.split(";")) {
                        assertTrue(set.contains(s));
                }

                deleteObject(ark);
                deleteLocalids(ark);
                getLocalids(localid, owner, false, ark);
                set = getLocalidsByArk(ark, false);
                assertEquals(0, set.size());
        }

        @Test
        public void AddObjectWithLocalIdByFormParamTest() throws IOException, JSONException {
                String ark = "ark:/1111/5555";
                String localid = "localid";
                String owner = "owner";
                if (checkArk(ark)) {
                        deleteObject(ark);
                        deleteLocalids(ark);
                }
                assertFalse(checkArk(ark));
                addObject(ark);
                assertTrue(checkArk(ark));
                getVersion(ark, primaryNode, 1);
                getLocalids(localid, owner, false, ark);
                getLocalidsByArk(ark, false);
                setLocalIdByFormParams(ark, owner, localid);
                getLocalids(localid, owner, true, ark);
                getLocalidsByArk(ark, true);
                deleteObject(ark);
                deleteLocalids(ark);
                getLocalids(localid, owner, false, ark);
                getLocalidsByArk(ark, false);
        }

        public JSONObject getLocalids(String localid, String owner, boolean expectToFind, String ark) throws HttpResponseException, IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/primary/%s/%s?t=json", 
                        port, 
                        cp, 
                        URLEncoder.encode(owner, StandardCharsets.UTF_8.name()),
                        URLEncoder.encode(localid, StandardCharsets.UTF_8.name())
                );
                JSONObject json = getJsonContent(url, 200);
                verifyLocalIdResponse(json, expectToFind, ark);
                return json;
        }

        public Set<String> getLocalidsByArk(String ark, boolean expectToFind) throws HttpResponseException, IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/local/%s?t=json", 
                        port, 
                        cp, 
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name())
                );
                JSONObject json = getJsonContent(url, 200);
                verifyLocalIdResponse(json, expectToFind, ark);
                assertTrue(json.has("invloc:localContainerState"));
                JSONObject j = json.getJSONObject("invloc:localContainerState");

                Set<String> set = new HashSet<>();
                if(j.has("invloc:local")) {
                        j = j.getJSONObject("invloc:local");
                        assertTrue(j.has("invloc:primaryLocalState"));
                        Object obj = j.get("invloc:primaryLocalState");
                        if (obj instanceof JSONArray) {
                                JSONArray arr = j.getJSONArray("invloc:primaryLocalState");
                                for(int i = 0; i < arr.length(); i++) {
                                        j = arr.getJSONObject(i);
                                        set.add(j.getString("invloc:localID"));
                                }                
                        } else if (obj instanceof JSONObject) {
                                j = j.getJSONObject("invloc:primaryLocalState");
                                set.add(j.getString("invloc:localID"));
                        }
                }
                return set;
        }

        public JSONObject getVersion(String ark, int node, int ver) throws HttpResponseException, IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/versions/%s?t=json", 
                        port, 
                        cp, 
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name())
                );
                JSONObject json = getJsonContent(url, 200);
                assertTrue(json.has("invv:versionsState"));
                assertEquals(node, json.getJSONObject("invv:versionsState").getInt("invv:bucketProperty"));
                assertEquals(ver, json.getJSONObject("invv:versionsState").getInt("invv:currentVersion"));
                return json;
        }

        public void verifyLocalIdResponse(JSONObject json, boolean expectToFind, String ark) throws JSONException {
                assertTrue(json.has("invloc:localContainerState"));
                assertEquals(expectToFind, json.getJSONObject("invloc:localContainerState").getBoolean("invloc:exists"));
                if (expectToFind) {
                        assertEquals(ark, json.getJSONObject("invloc:localContainerState").get("invloc:primaryIdentifier"));
                }
        }

}
