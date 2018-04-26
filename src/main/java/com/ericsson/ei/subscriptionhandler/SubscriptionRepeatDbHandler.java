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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.mongodb.BasicDBObject;

import lombok.Getter;
import lombok.Setter;


@Component
public class SubscriptionRepeatDbHandler {

	private static Logger LOGGER = (Logger) LoggerFactory.getLogger(SubscriptionRepeatDbHandler.class);

	
    @Autowired
    public MongoDBHandler mongoDbHandler;
    
    private ObjectMapper mapper = new ObjectMapper();
    
    @Getter
    @Setter
    @Value("${spring.data.mongodb.database}")
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
         //               RequirementId corresponds to a Requirement List of matched Aggregated Objects Ids.
                     <RequirementId>   <AggrObjIds>
         "requirements" : {"0" : [      
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
                         }
       }
     * 
     */
    
    /*
     * Function that stores the matched aggregatedObjectId to the database.
     * 
     */
    public void addMatchedAggrObjToSubscriptionId(String subscriptionId, int requirementId, String aggrObjId) throws Exception {
    	
    	LOGGER.debug("Adding/Updating matched AggrObjId: " + aggrObjId + " to SubscriptionsId: " + subscriptionId + " aggrId matched list" );
    	
    	if (checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(subscriptionId, requirementId, aggrObjId)) {
    		LOGGER.info("Subscription: " + subscriptionId + " and AggrObjId, " +
    							aggrObjId + " has already been matched." +
    							"No need to register the subscription match.");		
    		return;
    	}
    	

		if (!updateExistingMatchedSubscriptionWithAggrObjId(subscriptionId, requirementId, aggrObjId)) {
			LOGGER.error("Couldn't update SubscriptionMathced id.");
		}
		else {
			LOGGER.debug("New Subscription AggrId has not matched, inserting new SubscriptionId and AggrObjId to matched list.");
			BasicDBObject document = new BasicDBObject();
			document.put("subscriptionId", subscriptionId);

			ArrayList<String> aggrObjIdsList = new ArrayList<String>();
			aggrObjIdsList.add(aggrObjId);

			BasicDBObject reqDocument = new BasicDBObject();
			reqDocument.put(String.valueOf(requirementId), aggrObjIdsList);

			document.put("requirements", reqDocument);

			LOGGER.debug("New Matched AggrIdObject update on Subscription to be inserted to Db: " + document);
			boolean result = mongoDbHandler.insertDocument(dataBaseName, collectionName, document.toString());
			if (result == false) {
				throw new Exception("Failed to insert the document into database");
			}
		}
    }
    
	private boolean updateExistingMatchedSubscriptionWithAggrObjId(String subscriptionId, int requirementId,
			String aggrObjId) throws Exception {
		String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";

		JsonNode updateDocJsonNode = mapper
				.readValue("{\"$push\" : { \"requirements." + requirementId + "\" : \"" + aggrObjId + "\"}}", JsonNode.class);

		LOGGER.debug("SubscriptionId \"", subscriptionId, "\" document will be updated with following requirement update object: " + updateDocJsonNode.toString());

		JsonNode queryJsonNode = mapper.readValue(subscriptionQuery, JsonNode.class);
		try {
			mongoDbHandler.findAndModify(dataBaseName, collectionName, queryJsonNode.toString(),
					updateDocJsonNode.toString());
		} catch (Exception e) {
			LOGGER.debug("Failed to update existing matched SubscriptionId with new AggrId." + "SubscriptionId: "
					+ subscriptionId + "New matched AggrObjId: " + aggrObjId + "RequirementId that have matched: "
					+ requirementId);
			return false;
		}
		return true;
	}

    
	public boolean checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(String subscriptionId, int requirementId, String aggrObjId) {
		
		LOGGER.debug("Checking if AggrObjId: " + aggrObjId + " exist in SubscriptionId: " + subscriptionId + " AggrId matched list.");
		String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";
		List<String> objArray = mongoDbHandler.find(dataBaseName, collectionName, subscriptionQuery);
		if (objArray != null && !objArray.isEmpty()) {
			JsonNode jNode = null;
			LOGGER.debug("Making AggrObjId checks on SubscriptionId document: " + objArray.get(0));
			try {
				jNode = mapper.readTree(objArray.get(0));
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
				e.printStackTrace();
			}
			if (jNode.get("subscriptionId").asText().trim().equals(subscriptionId)) {
				LOGGER.debug("SubscriptionId \"" , subscriptionId , "\" , exist in document. Checking if AggrObjId has matched earlier.");
				List<String> listAggrObjIds = null;
				LOGGER.debug("Subscription requirementId: " + requirementId + " and Requirements content:\n" + jNode.get("requirements").get(new Integer(requirementId).toString()));
				try {
					ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {
					});
					JsonNode arrayNode = mapper.createArrayNode().add(jNode.get("requirements").get(new Integer(requirementId)));
					listAggrObjIds = reader.readValue(arrayNode);
					
					if (requirementId > (listAggrObjIds.size() - 1)) {
						LOGGER.debug("RequirementId: " + requirementId + " and SubscriptionId: " + subscriptionId +
								"\nhas not matched any AggregatedObject yet. No need to do anymore check.");
						return false;
					}
					
					if (listAggrObjIds.get(requirementId) == aggrObjId) {
						LOGGER.info("Subscription has matched aggrObjId already: " + aggrObjId);
						return true;
					}
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
					e.printStackTrace();
				} 
			}
		}
		LOGGER.info("AggrObjId not found for SubscriptionId in SubscriptionRepeatFlagHandlerDb -> Returning FALSE.");
		return false;
	}
	
}
