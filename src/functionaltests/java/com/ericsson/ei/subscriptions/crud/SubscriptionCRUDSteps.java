package com.ericsson.ei.subscriptions.crud;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.ei.controller.model.Subscription;
import com.ericsson.ei.controller.model.SubscriptionResponse;
import com.ericsson.ei.utils.FunctionalTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@Ignore
@AutoConfigureMockMvc
public class SubscriptionCRUDSteps extends FunctionalTestBase {
    
    static JSONArray jsonArray = null;
    private static final String subscriptionJsonPath = "src/functionaltests/resources/subscription_single.json";
    private static final String subscriptionJsonPathUpdated = "src/functionaltests/resources/subscription_single_updated.json";    
    
    @Autowired
    private MockMvc mockMvc;    
    MvcResult result;
    
    ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCRUDSteps.class);
    
    
    @Given("^The REST API \"([^\"]*)\" is up and running$")
    public void the_REST_API_is_up_and_running(String endPoint) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint)
                .accept(MediaType.APPLICATION_JSON);
        result = mockMvc.perform(requestBuilder).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());       
        System.out.println("It is running man!");
    }

    @When("^I make a POST request with valid \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_POST_request_with_valid_to_the_subscription_REST_API(String arg1, String endPoint) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        System.out.println(arg1 + endPoint);
        String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPath), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(endPoint).accept(MediaType.APPLICATION_JSON)
                .content(jsonArray.toString()).contentType(MediaType.APPLICATION_JSON);
        result = mockMvc.perform(requestBuilder).andReturn();   
    }

    @Then("^I get response code of (\\d+)$")
    public void i_get_response_code_of(int statusCode) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
    }
///===============================================================================
    
    @When("^I make a GET request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_GET_request_with_subscription_name_to_the_subscription_REST_API(String name, String endPoint) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(endPoint)
              .accept(MediaType.APPLICATION_JSON);
      result = mockMvc.perform(requestBuilder).andReturn();
//      System.out.println(result.getResponse().getStatus());
    }

    @Then("^I get response code of (\\d+) and subscription name \"([^\"]*)\"$")
    public void i_get_response_code_of_and_subscription_name(String statusCode, String name) throws Throwable {
        // Write code here that turns the phrase above into concrete actions        
        Subscription[] subscription = mapper.readValue(result.getResponse().getContentAsString().toString(),
                Subscription[].class);        
        Assert.assertEquals("Subscription_Test", subscription[0].getSubscriptionName());   }


   //=========================================================================================
      
    
    @When("^I make a PUT request with modified user name as \"([^\"]*)\"$")
    public void i_make_a_PUT_request_with_modified_user_name_as(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        String readFileToString = FileUtils.readFileToString(new File(subscriptionJsonPathUpdated), "UTF-8");
        jsonArray = new JSONArray(readFileToString);
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/subscriptions").accept(MediaType.APPLICATION_JSON)
                .content(jsonArray.toString()).contentType(MediaType.APPLICATION_JSON);
        result = mockMvc.perform(requestBuilder).andReturn();        
        System.out.println("1");
    }

    @Then("^I get response code of (\\d+) for successful updation$")
    public void i_get_response_code_of_for_successful_updation(int statusCode) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
        System.out.println("2");
    }

    @Then("^I can validate modified user name \"([^\"]*)\" with GET request at \"([^\"]*)\"$")
    public void i_can_validate_modified_user_name_with_GET_request_at(String arg1, String arg2) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions/Subscription_Test")
                .accept(MediaType.APPLICATION_JSON);
        result = mockMvc.perform(requestBuilder).andReturn();
        System.out.println("Delete Status================" + result.getResponse().getStatus());
        
        Subscription[] subscription = mapper.readValue(result.getResponse().getContentAsString(),
                Subscription[].class);
        String s = subscription[0].getUserName();
        System.out.println(s);
//        assertEquals("XYZ", subscription[0].getUserName());
        System.out.println("3");
    }    
    
    //==========================================================================================

    @When("^I make a DELETE request with subscription name \"([^\"]*)\" to the  subscription REST API \"([^\"]*)\"$")
    public void i_make_a_DELETE_request_with_subscription_name_to_the_subscription_REST_API(String name, String endPoint) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(endPoint)
                .accept(MediaType.APPLICATION_JSON);

        result = mockMvc.perform(requestBuilder).andReturn();
        System.out.println("Delete Status" + result.getResponse().getStatus());
    }    
    
    @Then("^I get response code of (\\d+) for successful delete$")
    public void i_get_response_code_of_for_successful_delete(int statusCode) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        System.out.println(statusCode);
        System.out.println("Delete Status" + result.getResponse().getStatus());
        Assert.assertEquals(statusCode, result.getResponse().getStatus());
        
        SubscriptionResponse subscriptionResponse = mapper
                .readValue(result.getResponse().getContentAsString().toString(), SubscriptionResponse.class);

        System.out.println(subscriptionResponse.getMsg());
        assertEquals("Deleted Successfully", subscriptionResponse.getMsg());
    }
    
    @Then("^My GET request with subscription name \"([^\"]*)\" return$")
    public void my_GET_request_with_subscription_name_return(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/subscriptions/Subscription_Test")
                .accept(MediaType.APPLICATION_JSON);

        result = mockMvc.perform(requestBuilder).andReturn();
       
        String a = result.getResponse().getContentAsString();
        System.out.println(a);
        assertEquals("[]", result.getResponse().getContentAsString());
  
    }
}
