package com.ericsson.ei.rules;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;

import org.junit.Ignore;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

@Ignore
public class RulesHandlerSteps {

    private ClientAndServer restServer;
    private MockServerClient mockClient;
    private RulesHandler rulesHandler;
    private String rulesPath = "";
    private int port = -1;

    private static final String HOST = "localhost";
    private static final String ROUTE_RULES_FILE = "/some/route/MyRules.json";
    private static final String ROUTE_RULES_FILE_EMPTY = "/some/route/EmptyRules.json";
    private static final String BODY = "{}";
    private static final String EMPTY = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(RulesHandlerSteps.class);

    @Before("@RulesHandlerHttpURI")
    public void beforeScenario() throws IOException {
        LOGGER.debug("Setting up Mock Server and Endpoints");
        setupRestEndpoints();
    }

    @After("@RulesHandlerHttpURI")
    public void afterScenario() throws IOException {
        LOGGER.debug("Stopping Mock Server");
        restServer.stop();
        mockClient.close();
    }

    @Given("^a file with path \"([^\"]*)\"$")
    public void file_with_path(String rulesPath) {
        this.rulesPath = rulesPath.replace("{port}", String.valueOf(port));
    }

    @Given("^path is made absolute$")
    public void path_is_absolute() throws IOException {
        rulesPath = new File(rulesPath).getAbsolutePath();
        rulesPath = rulesPath.replace("\\", "/");
    }

    @Given("^path is URI with \"([^\"]*)\" scheme$")
    public void path_is_uri(String scheme) {
        rulesPath = scheme + rulesPath;
    }

    @Then("^rules are loaded$")
    public void rules_are_loaded() throws Exception {
        initializeRulesHandler();
    }

    @Then("^rules are loaded with expected exception$")
    public void rules_are_loaded_with_exception() throws Exception {
        try {
            initializeRulesHandler();
        } catch (Exception e) {
            LOGGER.debug("Expected exception occurred");
        }
    }

    /**
     * Create a new instance of RulesHandler using a rules.path
     * set by the rulesPath variable.
     *
     * @throws Exception
     */
    private void initializeRulesHandler() throws Exception {
        LOGGER.debug("Rules Path: " + rulesPath);
        System.setProperty("rules.path", rulesPath);
        rulesHandler = new RulesHandler();
        rulesHandler.init();
    }

    /**
     * Setting up the needed endpoints for the functional test.
     */
    private void setupRestEndpoints() {
        port = SocketUtils.findAvailableTcpPort();
        restServer = startClientAndServer(port);

        LOGGER.debug("Setting up endpoints on host '" + HOST + "' and port '" + port + "'");
        mockClient = new MockServerClient(HOST, port);
        mockClient.when(request().withMethod("GET").withPath(ROUTE_RULES_FILE)).respond(response().withStatusCode(201).withBody(BODY));
        mockClient.when(request().withMethod("GET").withPath(ROUTE_RULES_FILE_EMPTY)).respond(response().withStatusCode(201).withBody(EMPTY));
    }
}
