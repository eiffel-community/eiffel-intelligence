package com.ericsson.ei.subscriptionhandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eclipsesource.json.Json;
import com.ericsson.ei.handlers.ObjectHandler;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import lombok.Getter;


@Component
public class SubscriptionRepeatDbHandler {

	static Logger log = (Logger) LoggerFactory.getLogger(SubscriptionRepeatDbHandler.class);

	
    @Autowired
    private MongoDBHandler mongoDbHandler;
    
    ObjectMapper mapper = new ObjectMapper();
    
    @Getter
    @Value("${database.name}")
    private String dataBaseName;
    @Getter
    @Value("${subscription.collection.repeatFlagHandlerName}")
    private String collectionName;
    
    public void addMatchedAggrObjToSubscriptionId(String subscriptionId, String aggrObjId) throws Exception {
    	
    	log.info("Adding/Updating matched AggrObjId: " + aggrObjId + " to SubscriptionsId: " + subscriptionId + " aggrId matched list" );
    	
    	if (checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(subscriptionId, aggrObjId)) {
    		log.info("Subscription: " + subscriptionId + " and AggrObjId, " +
    							aggrObjId + " has already been matched." +
    							"No need to register the subscription match.");		
    		return;
    	}
    	
    	String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";
    	ArrayList<String> objArray = mongoDbHandler.find(dataBaseName, collectionName, subscriptionQuery);
    	if (objArray != null && !objArray.isEmpty()) {
        	BasicDBObject subsObj = (BasicDBObject)  JSON.parse(objArray.get(0));

    		log.info("Subscription found: " + subscriptionId);
    		log.info("Subscription content: " + subsObj.toString());
    		
    		
			JsonNode jNode = null;
			try {
				jNode = mapper.readTree(subsObj.get("aggrIds").toString());
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Iterator<JsonNode> aggrIterator = jNode.elements();
    		ArrayList<String> aggrIdsJsonList = new ArrayList<String>();

			while (aggrIterator.hasNext()) {
				String aggrId = aggrIterator.next().asText();
	    		aggrIdsJsonList.add(aggrId);
			}
			
    		aggrIdsJsonList.add(aggrObjId);
    		subsObj.put("aggrIds", aggrIdsJsonList);
    		log.info("Updated AggrIdObject to be inserted to Db: " + subsObj.toString());
    		
    		mongoDbHandler.updateDocument(dataBaseName, collectionName, subscriptionQuery, subsObj.toString());
    		return;
    	}
    	
    	log.info("New Subscription AggrId not match,, inserting new AggrId to matched list.");
    	BasicDBObject document = new BasicDBObject();
    	document.put("subscriptionId", subscriptionId);
		ArrayList<String> list = new ArrayList<String>();
		list.add(aggrObjId);
		document.put("aggrIds", list);
		log.info("New AggrIdObject to be inserted to Db: " + document);
        boolean result=mongoDbHandler.insertDocument(dataBaseName,collectionName, document.toString());
        if (result == false) {
            throw new Exception("failed to insert the document into database");
        }
    }
    
	public boolean checkIfAggrObjIdExistInSubscriptionAggrIdsMatchedList(String subscriptionId, String aggrObjId) {
		
		log.info("Checking if AggrObjId: " + aggrObjId + " exist in SubscriptionId: " + subscriptionId + " AggrId matched list.");
		getAggrObjSubscriptions("");
		String subscriptionQuery = "{\"subscriptionId\" : \"" + subscriptionId + "\"}";
		ArrayList<String> objArray = mongoDbHandler.find(dataBaseName, collectionName, subscriptionQuery);
		if (objArray != null && !objArray.isEmpty()) {
			JsonNode jNode = null;
			log.info("AGGR Obj: " + objArray.get(0));
			try {
				jNode = mapper.readTree(objArray.get(0));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (jNode.get("subscriptionId").asText().trim().equals(subscriptionId)) {
				log.info("Subscription exist,, check if AggrId has matched.");
				Iterator<JsonNode> aggrIterator = jNode.get("aggrIds").elements();
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
    
    
    
    
    /*
     * Just for testing and check whole collection.
     * Currently just for debugging. Might be removed later.
     */
    public void getAggrObjSubscriptions(String aggrObjId) {
    	System.out.println("START");
    	ArrayList<String> resultArr = mongoDbHandler.getAllDocuments(dataBaseName, collectionName);
		for (int i = 0; i < resultArr.size(); i++) {
			JsonNode jNode = null;
			System.out.println("AGGR Obj: " + resultArr.get(i));
			try {
				jNode = mapper.readTree(resultArr.get(i));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("NodeObj: " + jNode.get("aggrIds"));
			Iterator<JsonNode> aggrIterator = jNode.get("aggrIds").iterator();
			while (aggrIterator.hasNext()) {
				System.out.println("DB ARR: " + aggrIterator.next());
			}
		}
    	
    }
	
	
}
