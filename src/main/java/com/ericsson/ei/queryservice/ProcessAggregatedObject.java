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
package com.ericsson.ei.queryservice;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class represents the mechanism to extract the aggregated data on the
 * basis of the ID from the aggregatedObject.
 * 
 * 
 */
@Component
public class ProcessAggregatedObject {

    @Value("${aggregated.collection.name}")
    private String aggregationCollectionName;

    @Value("${database.name}")
    private String aggregationDataBaseName;

    static Logger log = (Logger) LoggerFactory.getLogger(ProcessAggregatedObject.class);

    @Autowired
    MongoDBHandler handler;

    /**
     * The method is responsible to extract the aggregated data on the basis of
     * the ID from the aggregatedObject.
     * 
     * @param id
     * @return ArrayList
     */
    public ArrayList<String> processQueryAggregatedObject(String id) {
        ObjectMapper mapper = new ObjectMapper();
        String condition = "{\"id\" : \"" + id + "\"}";
        log.info("The condition is : " + condition);
        JsonNode jsonCondition = null;
        try {
            jsonCondition = mapper.readTree(condition);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("The Json condition is : " + jsonCondition);
        ArrayList<String> response = handler.find(aggregationDataBaseName, aggregationCollectionName,
                jsonCondition.toString());
        return response;
    }

    @PostConstruct
    public void init() {
        log.debug("The Aggregated Database is : " + aggregationDataBaseName);
        log.debug("The Aggregated Collection is : " + aggregationCollectionName);
    }

}
