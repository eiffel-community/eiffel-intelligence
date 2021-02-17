
/*
Copyright 2021 Ericsson AB.
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

package com.ericsson.ei.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ericsson.ei.mongo.MongoDBHandler;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MongoDBMonitorThread extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBMonitorThread.class);

	@Autowired
	MongoDBHandler mongoDBHandler;

	private static boolean isMongoDBConnected = false;

	@Value("${spring.data.mongodb.database}")
	private String dataBaseName;

	@Override
	public void run() {
		while (!isMongoDBConnected) {
			try {
				Thread.sleep(30000);
				isMongoDBConnected = mongoDBHandler.checkMongoDbStatus(dataBaseName);
				setMongoDBConnected(isMongoDBConnected);
				LOGGER.info(" monitor the mongoDB connection for every 30sec...");
			} catch (Exception e) {
				LOGGER.error("Exception creating connection to MongoDB " + e);
			}
		}
	}

	public static boolean isMongoDBConnected() {
		return isMongoDBConnected;
	}

	public static void setMongoDBConnected(boolean isMongoDBConnected) {
		MongoDBMonitorThread.isMongoDBConnected = isMongoDBConnected;
	}
} 