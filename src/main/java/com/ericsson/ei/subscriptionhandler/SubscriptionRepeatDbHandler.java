/*
   Copyright 2018 Ericsson AB.
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
package com.ericsson.ei.subscriptionhandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

import lombok.Getter;
import lombok.Setter;


@Component
public class SubscriptionRepeatDbHandler {

	static Logger log = (Logger) LoggerFactory.getLogger(SubscriptionRepeatDbHandler.class);

	
    @Autowired
    public MongoDBHandler mongoDbHandler;
    
    ObjectMapper mapper = new ObjectMapper();
    
    @Getter
    @Setter
    @Value("${database.name}")
    public String dataBaseName;
    @Getter
    @Setter
    @Value("${subscription.collection.repeatFlagHandlerName}")
    public String collectionName;
    
    
    /*
     * RepeatFlagHandling structure in MongoDb:
     * {
         "_id" : ObjectId("5ac62b4ea4f87e29e8cc5915"),
         "subscriptionId" : "subsA",
         //               RequirementId is Requirement List index number from Subscription json object requirement field/list.
                     <RequirementId>   <AggrObjIds>
         "requirement" : ["0" : [      
                      	               "11112", 
                                       "72324", 
                                       "72364", 
                      				   "72233", 
                                       "71233"
                                ],
                          "1" : [ 
                      	               "11112", 
                                       "72324", 
                                       "72364", 
                      				   "72233", 
                                       "71233"
                                ]
                         ]
       }
     * 
     */
    
    /*
     * Function that stores the matched aggregatedObjectId to the database.
     * 
     */
    public void addMatchedAggrObjToSubscriptionId(String subscriptionId, Integer requirementId, String aggrObjId) throws Exception {
    	
    	log.debug("Adding/Updating matched AggrObjId: " + aggrObjId + " to SubscriptionsId: " + subscriptionId + " aggrId matched list" );
    	
    	if (checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(subscriptionId, requirementId, aggrObjId)) {
    		log.info("Subscription: " + subscriptionId + " and AggrObjId, " +
    							aggrObjId + " has already been matched." +
    							"No need to register the subscription match.");		
    		return;
    	}
    	
    	String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";
    	ArrayList<String> objArray = mongoDbHandler.find(dataBaseName, collectionName, subscriptionQuery);
    	if (objArray != null && !objArray.isEmpty()) {
        	BasicDBObject subsObj = (BasicDBObject)  JSON.parse(objArray.get(0));

    		log.debug("SubscriptionId found in Db: " + subscriptionId);
    		log.debug("SubscriptionIds document content: " + subsObj.toString());
    		 
    		
			JsonNode jNode = null;
			try {
				jNode = mapper.readTree(subsObj.get("requirements").toString());
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Iterator<JsonNode> aggrIterator = jNode.get(requirementId).elements();
    		ArrayList<String> aggrIdsJsonList = new ArrayList<String>();

			while (aggrIterator.hasNext()) {
				String aggrId = aggrIterator.next().asText();
	    		aggrIdsJsonList.add(aggrId);
			}
			
    		aggrIdsJsonList.add(aggrObjId);
    		
    		ArrayList<List> reqList = new ArrayList<List>();
    		reqList.add(aggrIdsJsonList);
    		
    		subsObj.put("requirements", reqList);
    		log.debug("Updated AggrIdObject to be inserted to Db: " + subsObj.toString());


    		mongoDbHandler.updateDocument(dataBaseName, collectionName,subscriptionQuery , subsObj.toString());
    		
    		return;
    	}
    	
    	log.debug("New Subscription AggrId not match,, inserting new AggrId to matched list.");
    	BasicDBObject document = new BasicDBObject();
    	document.put("subscriptionId", subscriptionId);
		
    	ArrayList<String> aggrObjIdsList = new ArrayList<String>();
    	aggrObjIdsList.add(aggrObjId);
		
		ArrayList<ArrayList<String>> reqList = new ArrayList<ArrayList<String>>();
		reqList.add(aggrObjIdsList);
		
		document.put("requirements", reqList);
		
		log.debug("New Matched AggrIdObject update on Subscription to be inserted to Db: " + document);
        boolean result=mongoDbHandler.insertDocument(dataBaseName,collectionName, document.toString());
        if (result == false) {
            throw new Exception("failed to insert the document into database");
        }
    }
    
	public boolean checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(String subscriptionId, Integer requirementId, String aggrObjId) {
		
		log.debug("Checking if AggrObjId: " + aggrObjId + " exist in SubscriptionId: " + subscriptionId + " AggrId matched list.");
		String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";
		ArrayList<String> objArray = mongoDbHandler.find(dataBaseName, collectionName, subscriptionQuery);
		if (objArray != null && !objArray.isEmpty()) {
			JsonNode jNode = null;
			log.debug("Making AggrObjId checks on Document: " + objArray.get(0));
			try {
				jNode = mapper.readTree(objArray.get(0));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (jNode.get("subscriptionId").asText().trim().equals(subscriptionId)) {
				log.debug("SubscriptionId exist in document. Checking if AggrObjId has matched.");
				JSONArray jsonArray = null;
				String requirementsResult = null;
				try {
					jsonArray = new JSONArray(jNode.get("requirements").toString());
//					log.debug("JSONARRAY: " + jsonArray.toString());
//					log.debug("REQ ID: " + requirementId);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				log.debug("Requirements: " + requirementsResult);
				if (requirementId > (jsonArray.length() - 1)) {
					log.debug("RequirementId: " + requirementId + " and SubscriptionId: " + subscriptionId +
							"\nhas not matched any AggregatedObject yet. No need to do anymore check.");
					return false;
				}
				Iterator<JsonNode> aggrIterator = jNode.get("requirements").get(requirementId).elements();
				while (aggrIterator.hasNext()) {
					String aggrId = aggrIterator.next().asText();
					if (aggrId.trim().equals(aggrObjId)) {
						log.info("Subscription has matched aggrObjId already: " + aggrObjId);
						return true;
					}
				}
			}
		}
		log.info("AggrObjId not found for SubscriptionId in SubscriptionRepeatFlagHandlerDb -> Returning FALSE.");
		return false;
	}
	
}
