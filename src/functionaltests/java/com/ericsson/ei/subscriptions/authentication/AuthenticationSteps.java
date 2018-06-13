package com.ericsson.ei.subscriptions.authentication;

import com.ericsson.ei.controller.AuthControllerImpl;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.ericsson.ei.utils.TestLDAPInitializer;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Ignore
@AutoConfigureMockMvc
@ContextConfiguration(initializers = TestLDAPInitializer.class)
public class AuthenticationSteps extends FunctionalTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSteps.class);
    private static final String SUBSCRIPTION = "src/functionaltests/resources/subscription_single.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthControllerImpl authController;

    private MvcResult mvcResult;

    private String requestBody;

    @Given("^LDAP is activated$")
    public void ldap_is_activated() throws Throwable {
        String responseBody = new JSONObject().put("security", true).toString();
        mockMvc.perform(MockMvcRequestBuilders.get("/auth")
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().string(responseBody))
            .andReturn();
        requestBody = FileUtils.readFileToString(new File(SUBSCRIPTION), "UTF-8");
    }

    @When("^make a POST request to the subscription REST API \"(/\\w+)\" without credentials$")
    public void make_a_post_request_to_the_subscription_rest_api_without_credentials(String endpoint) throws Throwable {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Then("^get response code of (\\d+) and subscription with name \"(\\w+)\" is not created$")
    public void get_response_code_of_and_subscription_with_name_is_not_created(int statusCode, String subscriptionName) throws Throwable {
        Assert.assertEquals(statusCode, mvcResult.getResponse().getStatus());
        mockMvc.perform(MockMvcRequestBuilders.get("/subscriptions/" + subscriptionName)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("[]"))
            .andReturn();
    }
    ///Scenario:1 ends ===============================================================================

    @When("^make a POST request to the subscription REST API \"(/\\w+)\" with username \"(\\w+)\" and password \"(\\w+)\"")
    public void make_a_post_request_to_the_subscription_rest_api_with_username_and_password(String endpoint, String username, String password) throws Throwable {
        String auth = username + ":" + password;
        String encodedAuth = StringUtils.newStringUtf8(Base64.encodeBase64(auth.getBytes()));

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(endpoint)
            .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
            .accept(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Then("^get response code of (\\d+) and subscription with name \"(\\w+)\" is created$")
    public void get_response_code_of_and_subscription_with_name_is_created(int statusCode, String subscriptionName) throws Throwable {
        Assert.assertEquals(statusCode, mvcResult.getResponse().getStatus());
        mockMvc.perform(MockMvcRequestBuilders.get("/subscriptions/" + subscriptionName)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andReturn();
    }
    ///Scenario:2 ends ===============================================================================

}
