package com.ericsson.ei.flowtests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ericsson.ei.App;
import com.ericsson.ei.erqueryservice.ERQueryService;
import com.ericsson.ei.erqueryservice.SearchOption;
import com.ericsson.ei.handlers.UpStreamEventsHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, ArrayAggregationTest.class })
@SpringBootTest(classes = App.class)
public class ArrayAggregationTest extends FlowTestBase {

    private static final String UPSTREAM_RESULT_FILE = "arrayAggregationUpstreamResult.json";
    private static final String EVENTS_FILE_PATH = "src/test/resources/arrayAggregationEvents.json";
    private static final String RULES_FILE_PATH = "src/test/resources/arrayAggregationRules.json";
    private static final String AGGREGATED_OBJECT_FILE_PATH = "src/test/resources/arrayAggregatedObject.json";
    private static final String AGGREGATED_OBJECT_ID = "175f08ff-1e4b-4265-a0d4-36e744297dc3";

    @Autowired
    private UpStreamEventsHandler upStreamEventsHandler;

    @Mock
    private ERQueryService erQueryService;

    @Before
    public void before() throws IOException {
        MockitoAnnotations.initMocks(this);
        upStreamEventsHandler.setEventRepositoryQueryService(erQueryService);

        final URL upStreamResult = this.getClass().getClassLoader().getResource(UPSTREAM_RESULT_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set("upstreamLinkObjects", objectMapper.readTree(upStreamResult));
        objectNode.set("downstreamLinkObjects", objectMapper.createArrayNode());

        when(erQueryService.getEventStreamDataById(anyString(), any(SearchOption.class), anyInt(), anyInt(),
                anyBoolean())).thenReturn(new ResponseEntity<>(objectNode, HttpStatus.OK));
    }

    @Override
    String getRulesFilePath() {
        return RULES_FILE_PATH;
    }

    @Override
    String getEventsFilePath() {
        return EVENTS_FILE_PATH;
    }

    @Override
    protected int extraEventsCount() {
        // extra events from ER upstream
        return 2;
    }

    @Override
    List<String> getEventNamesToSend() {
        List<String> eventNames = new ArrayList<>();
        eventNames.add("EiffelArtifactPublishedEvent_1");
        eventNames.add("EiffelArtifactPublishedEvent_2");
        eventNames.add("EiffelCompositionDefinedEvent");
        eventNames.add("EiffelArtifactPublishedEvent_3");
        eventNames.add("EiffelArtifactPublishedEvent_4");
        return eventNames;
    }

    @Override
    Map<String, JsonNode> getCheckData() throws IOException {
        JsonNode expectedJSON = getJSONFromFile(AGGREGATED_OBJECT_FILE_PATH);
        Map<String, JsonNode> checkData = new HashMap<>();
        checkData.put(AGGREGATED_OBJECT_ID, expectedJSON);
        return checkData;
    }

}
