/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.queryservice;

import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * This class responsible to fetch the aggregatedObject from the aggregation
 * database and return it as JSONArray.
 *
 * @author xjibbal
 *
 */

@Component
public class ProcessAggregatedObject {

    static Logger log = (Logger) LoggerFactory.getLogger(ProcessAggregatedObject.class);

    /**
     * This method is responsible for fetching all the aggregatedObjects from the
     * Aggregation database and return it as JSONArray.
     *
     * @param request
     * @param AggregationDataBaseName
     * @param AggregationCollectionName
     * @return JSONArray
     */
    public JSONArray processQueryAggregatedObject(JsonNode request, String AggregationDataBaseName,
            String AggregationCollectionName) {
        DB db = new MongoClient("localhost", 27018).getDB(AggregationDataBaseName);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(AggregationCollectionName);
        log.info("Successfully connected to AggregatedObject database");
        MongoCursor<Document> allDocuments = aggObjects.find(request.toString()).as(Document.class);
        log.info("Number of document returned from AggregatedObject collection is : " + allDocuments.count());
        JSONArray jsonArray = new JSONArray();
        JSONObject doc = null;
        while (allDocuments.hasNext()) {
            Document temp = allDocuments.next();
            try {
                doc = new JSONObject(temp.toJson());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            jsonArray.put(doc);
        }
        return jsonArray;
    }

}
