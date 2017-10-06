package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.handlers.ExtractionHandler;
import com.ericsson.ei.jsonmerge.MergeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpStreamEventHandlerTest {

    private ExtractionHandler classUnderTest;

    static Logger log = (Logger) LoggerFactory.getLogger(UpStreamEventHandlerTest.class);

    @Test
    public void test() {
        String searchParametersString = "{ \"dlt\":[ \"\" ], \"ult\":[ \"ALL\" ] }";
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode searchParameters = mapper.readValue(searchParametersString, JsonNode.class);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }

        String upStreamString = "[\"A\", [\"B\", \"C\", [\"D\", \"E\"], \"F\", [\"N\", [\"G\", \"H\"]], \"I\", [\"J\", [\"K\", \"L\", [\"M\"]]]]]";

        //ERQueryService mockedERQueryService = mock(ERQueryService.class);
        //when(mockedERQueryService.getEventSteamDataById(eventId, searchParameters, -1, -1, true)).thenReturn(upStreamString);

        classUnderTest

        assertEquals("0", "1");
    }

}
