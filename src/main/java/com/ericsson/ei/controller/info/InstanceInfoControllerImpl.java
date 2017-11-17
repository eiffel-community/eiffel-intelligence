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
package com.ericsson.ei.controller.info;

import com.ericsson.ei.controller.model.ParseInstanceInfoEI;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@CrossOrigin
@Api(value = "information", description = "The Information about Eiffel Intelligence Backend instance")
public class InstanceInfoControllerImpl implements InstanceInfoController {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(InstanceInfoControllerImpl.class);

    @Autowired
    private ParseInstanceInfoEI istanceInfo;

    @Override
    @CrossOrigin
    @ApiOperation(value = "Parse information")
    public String parseInfo() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(istanceInfo);
        } catch (Exception e) {
            LOGGER.error("Serialization has failed " + e.getMessage());
        }
        return null;
    }
}
