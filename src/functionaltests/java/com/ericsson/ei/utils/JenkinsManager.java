package com.ericsson.ei.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.ericsson.ei.utils.HttpRequest.HttpMethod;

public class JenkinsManager {

    private static final String JENKINS_JOB_TEMPLATE_FILE_PATH = String.join(File.separator, "src", "functionaltests",
            "resources", "jenkinsJobTemplate.xml");
    private static final String DEFAULT_TOKEN = "test";
    private static final String DEFAULT_SCRIPT = "echo &quot;Test&quot;";
    private static final String DEFAULT_JOB_NAME = "test_job";

    private String host;
    private int port;
    private String encoding;
    private String crumb;

    /**
     * Constructor, takes jenkins host, port, username and password.
     * @param host
     * @param port
     * @param username
     * @param password
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public JenkinsManager(String host, int port, String username, String password)
            throws UnsupportedEncodingException, URISyntaxException {
        String authString = String.join(":", username, password);

        this.host = host;
        this.port = port;
        this.encoding = DatatypeConverter.printBase64Binary(authString.getBytes("utf-8"));
        this.crumb = getCrumb();
    }

    /**
     * Takes a token and script and returns XML data for jenkins with token and
     * script added.
     *
     * @param token
     * @param script
     * @return
     * @throws IOException
     */
    public String getXmlJobData(String token, String script) {
        String jobData = getStringFromFile(JENKINS_JOB_TEMPLATE_FILE_PATH);

        if (token == null) {
            token = DEFAULT_TOKEN;
        }
        if (script == null) {
            script = DEFAULT_SCRIPT;
        }

        jobData = stringReplaceText(jobData, token, script);
        return jobData;
    }

    /**
     * Creates a jenkins job with a given name using the XML data as input for
     * job configuration
     *
     * @param jobName
     * @param jobXmlData
     * @return
     * @throws URISyntaxException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public boolean createJob(String jobName, String jobXmlData) throws URISyntaxException {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        boolean success = false;

        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }

        httpRequest.setHost(host).setPort(port).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_XML_VALUE).addHeader("Jenkins-Crumb", crumb)
                .addParam("name", jobName).setBody(jobXmlData).setEndpoint("/createItem");

        ResponseEntity<String> response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.OK;

        return success;
    }

    /**
     * This function recieves a jenkins job name and are used to trigger the job
     *
     * @throws URISyntaxException
     */
    public boolean triggerJob(String jobName, String token) throws URISyntaxException {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        boolean success = false;

        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }
        if (token == null) {
            token = DEFAULT_TOKEN;
        }

        String endpoint = "/job/" + jobName + "/build";

        httpRequest.setHost(host).setPort(port).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON_VALUE).addParam("token", token)
                .setEndpoint(endpoint);

        ResponseEntity<String> response = httpRequest.performRequest();
        success = response.getStatusCode() == HttpStatus.CREATED;
        return success;
    }

    /**
     * This function recieves a jenkins job name and returns weather the job has
     * been triggered at least once
     *
     * @param jobName
     * @return
     * @throws URISyntaxException
     */
    public boolean jobHasBeenTriggered(String jobName) throws URISyntaxException {
        boolean triggered = false;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);
        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }

        String endpoint = "/job/" + jobName + "/1/api/json";

        httpRequest.setHost(host).setPort(port).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON_VALUE)
                .setEndpoint(endpoint);

        ResponseEntity<String> response = httpRequest.performRequest();
        triggered = response.getStatusCode() == HttpStatus.OK;
        return triggered;
    }

    /**
     *
     * This function recieves a jenkins job name and deletes that job from the
     * jenkins system
     *
     * @param jobName
     * @return
     * @throws URISyntaxException
     */
    public boolean deleteJob(String jobName) throws URISyntaxException {
        boolean deleted = false;
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST);
        if (jobName == null) {
            jobName = DEFAULT_JOB_NAME;
        }

        String endpoint = "/job/" + jobName + "/doDelete";

        httpRequest.setHost(host).setPort(port).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON_VALUE).addHeader("Jenkins-Crumb", crumb)
                .setEndpoint(endpoint);

        ResponseEntity<String> response = httpRequest.performRequest();
        deleted = response.getStatusCode() == HttpStatus.FOUND;
        return deleted;
    }

    /**
     * Gets the jenkins crumb if any
     *
     * @return
     * @throws URISyntaxException
     */
    private String getCrumb() throws URISyntaxException {
        String crumb = "";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET);

        httpRequest.setHost(host).setPort(port).addHeader("Authorization", "Basic " + encoding)
                .addHeader("Content-type", MediaType.APPLICATION_JSON_VALUE).setEndpoint("/crumbIssuer/api/json");

        ResponseEntity<String> response = httpRequest.performRequest();
        boolean success = response.getStatusCode() == HttpStatus.OK;

        if (success) {
            JSONObject jsonObj = new JSONObject(response.getBody());
            crumb = jsonObj.getString("crumb");
        }
        return crumb;
    }

    private static String getStringFromFile(String filepath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
    }

    /**
     * Replaces tags in string with valid information.
     *
     * @param text
     * @param token
     * @param script
     * @return
     */
    private String stringReplaceText(String text, String token, String script) {
        text = text.replaceAll("\\$\\{shell\\.script\\}", script);
        text = text.replaceAll("\\$\\{auth\\.token\\}", token);
        return text;
    }

}
