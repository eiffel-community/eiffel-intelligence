package com.ericsson.ei.handlers.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.Header;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.exception.PropertyNotFoundException;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.ericsson.ei.utils.TestContextInitializer;
import com.ericsson.eiffelcommons.utils.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: UpStreamEventHandlerTest",
        "failed.notifications.collection.name: UpStreamEventHandlerTest-failedNotifications",
        "rabbitmq.exchange.name: UpStreamEventHandlerTest-exchange",
        "rabbitmq.queue.suffix: UpStreamEventHandlerTest",
        "event.repository.url: " })
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { App.class })
public class UpStreamEventHandlerTest {

    @Autowired
    private UpStreamEventsHandler classUnderTest;

    static Logger log = LoggerFactory.getLogger(UpStreamEventHandlerTest.class);

    @Test(expected = PropertyNotFoundException.class)
    public void testRunHistoryExtractionRulesOnAllUpstreamEvents() throws Exception {
        // TODO to complete implementation
        String upStreamString = "{\"upstreamLinkObjects\":[\n" + "\t\t{\"_id\":\"event1_level_1\"},[\n"
                + "\t\t\t{\"_id\":\"event2_level_2\"},\n" + "\t\t\t{\"_id\":\"event3_level_2\"},[\n"
                + "\t\t\t\t{\"_id\":\"event4_level_3\"},[\n" + "\t\t\t\t\t{\"_id\":\"event5_level_4\"}],\n"
                + "\t\t\t\t{\"_id\":\"event6_level_3\"}],\n" + "\t\t\t{\"_id\":\"event7_level_2\"},\n"
                + "\t\t\t{\"_id\":\"event8_level_2\"}]]}\n";
        final JsonNode response = new ObjectMapper().readTree(upStreamString);

        ERQueryService mockedERQueryService = mock(ERQueryService.class);
        String aggregatedObjectId = "0123456789abcdef";
        Header[] headers = {};
        when(mockedERQueryService.getEventStreamDataById(aggregatedObjectId, SearchOption.UP_STREAM, -1, -1, true))
                .thenReturn(new ResponseEntity(201, response.toString(), headers));

        classUnderTest.runHistoryExtractionRulesOnAllUpstreamEvents("0123456789abcdef");
    }

}
