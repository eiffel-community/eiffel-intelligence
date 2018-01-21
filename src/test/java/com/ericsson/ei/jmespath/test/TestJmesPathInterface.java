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
package com.ericsson.ei.jmespath.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.burt.jmespath.Expression;

public class TestJmesPathInterface {
	private JmesPathInterface unitUnderTest;
	private final String inputFilePath = "src/test/resources/EiffelArtifactCreatedEvent.json";
	private final String outputFilePath = "src/test/resources/JmesPathInterfaceOutput.json";
	private final String inputDiffpath = "src/test/resources/DiffFunctionInput.json";
	private final String extractionRuleFilePath = "src/test/resources/ExtractionRule.txt";

	static Logger log = (Logger) LoggerFactory.getLogger(TestJmesPathInterface.class);

	@Test
	public void testRunRuleOnEvent() {
		unitUnderTest = new JmesPathInterface();
		String jsonInput = null;
		String jsonOutput = null;
		String extractionRulesTest = null;
		JsonNode output = null;

		try {
			jsonInput = FileUtils.readFileToString(new File(inputFilePath));
			jsonOutput = FileUtils.readFileToString(new File(outputFilePath));
			extractionRulesTest = FileUtils.readFileToString(new File(extractionRuleFilePath));
			ObjectMapper objectmapper = new ObjectMapper();
			output = objectmapper.readTree(jsonOutput);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		JsonNode result = unitUnderTest.runRuleOnEvent(extractionRulesTest, jsonInput);
		assertEquals(result, output);
	}

	@Test
	public void testDiffFunction() {
		unitUnderTest = new JmesPathInterface();
		String jsonInput = null;
		JsonNode expectedResult = null;
		try {
			jsonInput = FileUtils.readFileToString(new File(inputDiffpath));
			ObjectMapper mapper = new ObjectMapper();
			expectedResult = mapper.readTree("{\"testCaseExecutions\":[{\"testCaseDuration\":6.67}]}");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		String processRule = "{testCaseExecutions :[{testCaseDuration : diff(testCaseExecutions[0].testCaseFinishedTime, testCaseExecutions[0].testCaseStartedTime)}]}";
		JsonNode result = unitUnderTest.runRuleOnEvent(processRule, jsonInput);
		assertEquals(result, expectedResult);
	}

	@Test
	public void testLiteral() {
		unitUnderTest = new JmesPathInterface();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode literalJson;
		try {
			literalJson = mapper.readTree("{}");
			JsonNode input =  mapper.readTree("{\"id\":\"test\"}");
			((ObjectNode) literalJson).put("eventId", "b6ef1hd-25fh-4dh7-b9vd-87688e65de47");
			String ruleString = literalJson.toString();
			ruleString = "`" + ruleString + "`";
			unitUnderTest.runRuleOnEvent(ruleString, input.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//        String literal = "{\"eventId\":"`"fb6ef1hd-25fh-4dh7-b9vd-87688e65de47"`"}";
	}
}
