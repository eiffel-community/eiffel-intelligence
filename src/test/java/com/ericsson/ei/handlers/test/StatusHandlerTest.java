/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.handlers.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.ericsson.ei.handlers.StatusHandler;
import com.ericsson.ei.status.Status;
import com.fasterxml.jackson.databind.JsonNode;

@RunWith(MockitoJUnitRunner.class)
public class StatusHandlerTest {

    @InjectMocks
    private StatusHandler statusHandler;

    @Test
    public void testStatusHandlerWithStatusDataAsDefault() {
        JsonNode statusData = statusHandler.getCurrentStatus();

        assertEquals("Event repository default value should be NOT_SET", Status.NOT_SET.name(), statusData.get("eventRepositoryStatus").asText());
    }
}
