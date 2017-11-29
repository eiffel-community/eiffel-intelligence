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
package com.ericsson.ei.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.ericsson.ei.jmespath.JmesPathInterface;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Provides interaction with JMESPath resource 
 *  
 */
@Component
@CrossOrigin
@Api(value = "jmespath")
public class RulesControllerImpl implements RulesController {


	private static final Logger LOG = LoggerFactory.getLogger(SubscriptionControllerImpl.class);


	@Autowired 
	JmesPathInterface jmesPathInterface;


	@Override
	@CrossOrigin
	@ApiOperation(value = "run rule on event")
	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<String> runRule(@RequestParam("arg1") String arg1, @RequestBody String arg2) {
		// TODO Auto-generated method stub

		String res = new String("[]");

		try {
			res = jmesPathInterface.runRuleOnEvent(arg1, arg2).toString();
			LOG.info("Query :" + arg1 + " executed Successfully");
			return new ResponseEntity<String>(res, HttpStatus.OK);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<String>(res, HttpStatus.BAD_REQUEST);
		}    	


	}    


}
