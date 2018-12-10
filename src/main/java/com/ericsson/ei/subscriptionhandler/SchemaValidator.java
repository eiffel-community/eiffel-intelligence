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

package com.ericsson.ei.subscriptionhandler;

import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.controller.model.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SchemaValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidator.class);
	private static final String SCHEMA_FILE_PATH = "/subscription_schema.json";

	/**
	 * Validation of keys in subscriptions objects. Throws
	 * SubscriptionValidationException if validation of a parameter fails due to non
	 * existence of parameter.
	 *
	 * @param subscription
	 */
	public void validateSubscription(Subscription subscription) {
		LOGGER.debug("Validation of subscription " + subscription.getSubscriptionName() + " Started.");

		ObjectMapper mapper = new ObjectMapper();
		String subscriptionJson = null;
		try {
			subscriptionJson = mapper.writeValueAsString(subscription);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Failed to create object to json" + "\nError message: " + e.getMessage(), e);
		}

		InputStream file = this.getClass().getResourceAsStream(SCHEMA_FILE_PATH);

		JSONObject jsonSchema = new JSONObject(new JSONTokener(file));

		JSONObject jsonSubject = new JSONObject(subscriptionJson);

		Schema schema = SchemaLoader.load(jsonSchema);
		try {
			schema.validate(jsonSubject);
		} catch (ValidationException e) {
			LOGGER.error("Schema validation fails" + "\nError message: " + e.getMessage(), e);
		}
	}

}
