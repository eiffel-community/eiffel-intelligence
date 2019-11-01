/*
   Copyright 2017 Ericsson AB.
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
package com.ericsson.ei.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.App;
import com.ericsson.ei.controller.model.ParseInstanceInfoEI;
import com.ericsson.ei.utils.TestContextInitializer;

@TestPropertySource(properties = {
        "spring.data.mongodb.database: TestInformationControllerImpl",
        "failed.notification.database-name: TestInformationControllerImpl-failedNotifications",
        "rabbitmq.exchange.name: TestInformationControllerImpl-exchange",
        "rabbitmq.consumerName: TestInformationControllerImpl" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = App.class, loader = SpringBootContextLoader.class, initializers = TestContextInitializer.class)
@WebMvcTest(value = InformationController.class, secure = false)
public class TestInformationControllerImpl extends ControllerTestBaseClass {

    @MockBean
    private ParseInstanceInfoEI istanceInfo;

    @MockBean
    private InformationController infoController;

    @Test
    public void testResponseStatus() throws Throwable {
        assertOkResponseStatus("/information");
    }
}