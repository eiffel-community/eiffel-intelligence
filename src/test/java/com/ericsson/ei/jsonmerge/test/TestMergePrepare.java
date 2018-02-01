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
package com.ericsson.ei.jsonmerge.test;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.jsonmerge.MergePrepare;
import org.json.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;

@RunWith(Parameterized.class)
public class TestMergePrepare {
    public static final String testDataPath = "src/test/resources/MergePrepareData.json";
    public String originObject;
    public String mergeObject;
    public String mergeRule;
    public String mergePath;
    public String ruleValue;
    public String mergedObject;

    static Logger log = (Logger) LoggerFactory.getLogger(TestMergePrepare.class);

    public TestMergePrepare(String originObject, String mergeObject, String mergeRule, String mergePath,
            String ruleValue, String mergedObject) {
        this.originObject = originObject;
        this.mergeObject = mergeObject;
        this.mergeRule = mergeRule;
        this.mergePath = mergePath;
        this.ruleValue = ruleValue;
        this.mergedObject = mergedObject;
    }

    static MergePrepare mergePrepareObject;

    @BeforeClass
    public static void setup() throws Exception {
        mergePrepareObject = new MergePrepare();
        mergePrepareObject.setJmesPathInterface(new JmesPathInterface());
    }

    @Test
    public void getValueFromRule() {
        String result = mergePrepareObject.getValueFromRule(mergeRule);
        assertEquals(ruleValue, result);
    }

    @Test
    public void getMergePath() {
        String result = mergePrepareObject.getMergePath(originObject, mergeRule);
        assertEquals(mergePath, result);
    }

    @Test
    public void addMissingLevels() {
        String result = mergePrepareObject.addMissingLevels(originObject, mergeObject, mergeRule, mergePath);
        assertEquals(mergedObject, result.replace("\"", ""));
    }

    @Parameters
    public static Collection<Object[]> inputTestData() {
        String testData;
        Collection<Object[]> baseList = new ArrayList<>();
        try {
            testData = FileUtils.readFileToString(new File(testDataPath), "UTF-8");
            JSONArray testDataJson = new JSONArray(testData);
            for (int i = 0; i < testDataJson.length(); i++) {
                final List<String> childList = new ArrayList<>();
                for (int k = 0; k < ((JSONArray) testDataJson.get(i)).length(); k++) {
                    childList.add(((String) ((JSONArray) testDataJson.get(i)).get(k)));
                }
                baseList.add(childList.toArray());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return baseList;
    }
}