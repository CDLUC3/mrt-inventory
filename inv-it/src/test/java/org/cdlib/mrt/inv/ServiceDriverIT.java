package org.cdlib.mrt.inv;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import javax.xml.xpath.XPathFactory;
//https://stackoverflow.com/a/22939742/3846548
import org.apache.xpath.jaxp.XPathFactoryImpl;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

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

        }

        @Before
        public void initServiceAndNodes() throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/service/start?t=json", port, cp);
                System.out.println(url);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());

                        System.out.println(s);
                        JSONObject json =  new JSONObject(s);
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
                JSONObject json =  new JSONObject(s);
                assertNotNull(json);
                return json;
        }

        public List<String> getZipContent(String url, int status) throws HttpResponseException, IOException {
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    HttpGet request = new HttpGet(url);
                    HttpResponse response = client.execute(request);
                    assertEquals(status, response.getStatusLine().getStatusCode());

                    List<String> entries = new ArrayList<>();
                    if (status < 300) {
                            try(ZipInputStream zis = new ZipInputStream(response.getEntity().getContent())){
                                    for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
                                            entries.add(ze.getName());
                                    }
                            }
                    }

                    return entries;
                }
        }


        @Test
        public void SimpleTest() throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/state?t=json", port, cp);
                JSONObject json = getJsonContent(url, 200);
                assertTrue(json.has("invsv:invServiceState"));
                assertEquals("running", json.getJSONObject("invsv:invServiceState").get("invsv:systemStatus"));
        }

        public JSONObject setFileNode(int node) throws IOException, JSONException {
                String url = String.format("http://localhost:%d/%s/filenode/%d?t=json", port, cp, node);
                System.out.println(url);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());

                        return new JSONObject(s);
                }
        }

        @Test
        public void AddObjectTest() throws IOException, JSONException {
                String ark = "ark:/1111/2222";
                String ark_de = URLEncoder.encode(URLEncoder.encode(ark, StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8.name());
                String manifest = String.format("http://mock-merritt-it:4567/static/storage/manifest/7777/%s", ark_de);
                String url = String.format("http://localhost:%d/%s/add", port, cp);
                System.out.println(url);
                try (CloseableHttpClient client = HttpClients.createDefault()) {
                        HttpPost post = new HttpPost(url);
                        
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.addTextBody("url", manifest, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
                        HttpEntity entity = builder.build();
                        post.setEntity(entity);
                        HttpResponse response = client.execute(post);
                        assertEquals(200, response.getStatusLine().getStatusCode());
                        String s = new BasicResponseHandler().handleResponse(response).trim();
                        assertFalse(s.isEmpty());

                        System.out.println(s);
                }
        }

}