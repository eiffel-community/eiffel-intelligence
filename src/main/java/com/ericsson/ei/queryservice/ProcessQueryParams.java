/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.queryservice;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class is responsible to fetch the criterias from both the query
 * parameters or the form parameters. Then find the aggregatedObject from the
 * database and concatenate the result.
 *
 * @author xjibbal
 *
 */

@Component
public class ProcessQueryParams {

	@Value("${aggregationCollectionName}")
	private String aggregationCollectionName;

	@Value("${aggregationDataBaseName}")
	private String aggregationDataBaseName;

	@Value("${missedNotificationCollectionName}")
	private String missedNotificationCollectionName;

	@Value("${missedNotificationDataBaseName}")
	private String missedNotificationDataBaseName;

	static Logger log = (Logger) LoggerFactory.getLogger(QueryServiceRestController.class);

	@Autowired
	ProcessAggregatedObject processAggregatedObject;

	@Autowired
	ProcessMissedNotification processMissedNotification;

	/**
	 * This method takes the parameters from the REST POST request body and
	 * process it to create a JsonNode request to query the Aggregated Objects.
	 * If the Aggregated Object matches the condition, then it is returned.
	 *
	 * @param request
	 * @return JSONArray
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public JSONArray filterFormParam(JsonNode request) throws JsonProcessingException, IOException {
		JsonNode criteria = request.get("criteria");
		JsonNode options = request.get("options");
		log.info("The criteria is : " + criteria.toString());
		log.info("The options is : " + options.toString());
		JSONArray resultAggregatedObject = null;
		JSONArray resultMissedNotification = null;
		if (options.toString().equals("{}") || options.isNull()) {
			resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject((JsonNode) criteria,
					aggregationDataBaseName, aggregationCollectionName);
			resultMissedNotification = processMissedNotification.processQueryMissedNotification((JsonNode) criteria,
					missedNotificationDataBaseName, missedNotificationCollectionName);
		} else {
			String result = "{ \"$and\" : [ " + criteria.toString() + "," + options.toString() + " ] }";
			resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject(
					new ObjectMapper().readTree(result), aggregationDataBaseName, aggregationCollectionName);
			resultMissedNotification = processMissedNotification.processQueryMissedNotification(
					new ObjectMapper().readTree(result), missedNotificationDataBaseName,
					missedNotificationCollectionName);
		}
		log.info("resultAggregatedObject : " + resultAggregatedObject.toString());
		log.info("resultMissedNotification : " + resultMissedNotification.toString());
		JSONArray result = null;
		try {
			result = ProcessQueryParams.concatArray(resultAggregatedObject, resultMissedNotification);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Final Result is : " + result.toString());
		return result;
	}

	/**
	 * This method is responsible for concatenating two JSONArrays.
	 *
	 * @param arr1
	 * @param arr2
	 * @return JSONArray
	 * @throws JSONException
	 */
	private static JSONArray concatArray(JSONArray arr1, JSONArray arr2) throws JSONException {
		JSONArray result = new JSONArray();
		for (int i = 0; i < arr1.length(); i++) {
			result.put(arr1.get(i));
		}
		for (int i = 0; i < arr2.length(); i++) {
			result.put(arr2.get(i));
		}
		return result;
	}

	/**
	 * This method takes the parameters from the REST GET request query
	 * parameters and process it to create a JsonNode request to query the
	 * Aggregated Objects. If the Aggregated Object matches the condition, then
	 * it is returned.
	 *
	 * @param request
	 * @return JSONArray
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public JSONArray filterQueryParam(HttpServletRequest request) throws JsonProcessingException, IOException {
		String url = request.getRequestURI();
		log.info("The URI is :" + url);
		String queryString = request.getQueryString();
		log.info("The query string is : " + queryString);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode criteria = mapper.createObjectNode();
		String[] criterias = queryString.split(",");
		log.info("The query parameters are :");
		for (String s : criterias) {
			log.info(s);
			String[] node = s.split(":");
			String key = node[0];
			String value = node[1];
			log.info("The key is : " + key);
			log.info("The value is : " + value);
			((ObjectNode) criteria).put(key, value);
		}
		log.info(criteria.toString());
		JSONArray resultAggregatedObject = processAggregatedObject.processQueryAggregatedObject((JsonNode) criteria,
				aggregationDataBaseName, aggregationCollectionName);
		JSONArray resultMissedNotification = processMissedNotification.processQueryMissedNotification(
				(JsonNode) criteria, missedNotificationDataBaseName, missedNotificationCollectionName);
		log.info("resultAggregatedObject : " + resultAggregatedObject.toString());
		log.info("resultMissedNotification : " + resultMissedNotification.toString());
		JSONArray result = null;
		try {
			result = ProcessQueryParams.concatArray(resultAggregatedObject, resultMissedNotification);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.info("Final Result is : " + result.toString());
		return result;
	}

	@PostConstruct
	public void print() {
		log.debug("Values from application.properties file");
		log.debug("AggregationCollectionName : " + aggregationCollectionName);
		log.debug("AggregationDataBaseName : " + aggregationDataBaseName);
		log.debug("MissedNotificationCollectionName : " + missedNotificationCollectionName);
		log.debug("MissedNotificationDataBaseName : " + missedNotificationDataBaseName);
	}
}
