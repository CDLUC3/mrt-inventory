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
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathFactory;
//https://stackoverflow.com/a/22939742/3846548
import org.apache.xpath.jaxp.XPathFactoryImpl;

import java.io.IOException;

import static org.junit.Assert.*;

public class ServiceDriverIT {
        private int port = 8080;
        private int primaryNode = 7777;
        private int replNode = 8888;
        private String cp = "mrtinv";
        private DocumentBuilder db;
        private XPathFactory xpathfactory;

        public ServiceDriverIT() throws ParserConfigurationException, HttpResponseException, IOException, JSONException {
                try {
                        port = Integer.parseInt(System.getenv("it-server.port"));
                } catch (NumberFormatException e) {
                        System.err.println("it-server.port not set, defaulting to " + port);
                }
                db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                xpathfactory = new XPathFactoryImpl();
                initServiceAndNodes();
        }

        public void initServiceAndNodes() throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/service/start?t=json", port, cp);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());

                        JSONObject json =  new JSONObject(s);
                        assertNotNull(json);
                }
                setFileNode(primaryNode);
                setFileNode(replNode);
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

        public JSONObject addUrlToZk(String ark) throws IOException, JSONException {
                String ark_e = URLEncoder.encode(ark, StandardCharsets.UTF_8.name());
                String manifest = String.format("http://mock-merritt-it:4567/storage/manifest/7777/%s", ark_e);
                String url = String.format("http://localhost:%d/%s/addzoo", port, cp);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.addTextBody("responseForm", "json", ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        builder.addTextBody("zoourl", manifest, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
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
        public void AddObjectTest() throws IOException, JSONException {
                String ark = "ark:/1111/2222";
                if (checkArk(ark)) {
                        deleteObject(ark);
                }
                assertFalse(checkArk(ark));
                addObject(ark);
                assertTrue(checkArk(ark));
                deleteObject(ark);
        }

        @Test
        public void AddObjectToZkTest() throws IOException, JSONException, InterruptedException {
                String ark = "ark:/1111/3333";
                if (checkArk(ark)) {
                        deleteObject(ark);
                }
                assertFalse(checkArk(ark));
                addUrlToZk(ark);
                //Allow time for queue entry to be processed
                Thread.sleep(20000);
                assertTrue(checkArk(ark));
                deleteObject(ark);
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

        public JSONObject getLocalidsByArk(String ark, boolean expectToFind) throws HttpResponseException, IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/local/%s?t=json", 
                        port, 
                        cp, 
                        URLEncoder.encode(ark, StandardCharsets.UTF_8.name())
                );
                JSONObject json = getJsonContent(url, 200);
                verifyLocalIdResponse(json, expectToFind, ark);
                return json;
        }

        public void verifyLocalIdResponse(JSONObject json, boolean expectToFind, String ark) throws JSONException {
                assertTrue(json.has("invloc:localContainerState"));
                assertEquals(expectToFind, json.getJSONObject("invloc:localContainerState").getBoolean("invloc:exists"));
                if (expectToFind) {
                        assertEquals(ark, json.getJSONObject("invloc:localContainerState").get("invloc:primaryIdentifier"));
                }
        }

        public void addZookeeperTest() {
                //POST @Path("addzoo") @Consumes(MediaType.MULTIPART_FORM_DATA)

                //test read from zookeeper
        }

}
