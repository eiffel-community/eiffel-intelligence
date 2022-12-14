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
package com.ericsson.ei.cache;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.DateUtils;

/**
* This class is used for caching the subscriptions and checking the aggregated
* objects from the collection.
*
*/
@Component
public class SubscriptionCacheHandler {

 private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionCacheHandler.class);
 public static Map<String, List<String>> subscriptionsCache = new HashedMap<>();

 // ttl value to cleanup the subscription cache. default to 1hour.
 private final static String delayString = "3600000";

 /**
  * This method runs on the described delay and fixed rate of 1 hr to clean up the 
  * cached subscription objects.
  * @throws ParseException 
  * 
  */
 @Scheduled(initialDelayString = delayString, fixedRateString = delayString)
 public void run() throws ParseException {
     //This statement is used to not cause concurrent modification exception to the map.
     LOGGER.debug("Cleaning the subscription cache at : {}", DateUtils.getDate());
     LOGGER.info("Size of subscription cache is : {}", subscriptionsCache.size() );
     subscriptionsCache = new HashedMap<>();
     LOGGER.info("Size of subscription cache after cleaning is: {}", subscriptionsCache.size() );
 }

}