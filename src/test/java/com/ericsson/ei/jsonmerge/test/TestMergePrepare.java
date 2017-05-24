package com.ericsson.ei.jsonmerge.test;

import com.ericsson.ei.jsonmerge.MergePrepare;
import org.json.*;
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

import org.apache.commons.io.FileUtils;


@RunWith(Parameterized.class)
public class TestMergePrepare {
    public static final String testDataPath = "src/test/resources/MergePrepareData.json";
    public String originObject;
    public String mergeObject;
    public String mergeRule;
    public String mergePath;
    public String ruleKey;
    public String ruleValue;
    public String mergedObject;

    static Logger log = (Logger) LoggerFactory.getLogger(TestMergePrepare.class);

    public TestMergePrepare(String originObject, String mergeObject,
                            String mergeRule, String mergePath,
                            String ruleKey, String ruleValue,
                            String mergedObject){
        this.originObject = originObject;
        this.mergeObject = mergeObject;
        this.mergeRule = mergeRule;
        this.mergePath = mergePath;
        this.ruleKey = ruleKey;
        this.ruleValue = ruleValue;
        this.mergedObject = mergedObject;
    }

    @Test
    public void getKeyFromRule() {
        MergePrepare mergePrepareObject = new MergePrepare();
        String result = mergePrepareObject.getKeyFromRule(mergeRule);
        assertEquals(ruleKey, result);
    }

    @Test
    public void getValueFromRule() {
        MergePrepare mergePrepareObject = new MergePrepare();
        String result = mergePrepareObject.getValueFromRule(mergeRule);
        assertEquals(ruleValue, result);
    }

    @Test
    public void getMergePath() {
        MergePrepare mergePrepareObject = new MergePrepare();
        String result = mergePrepareObject.getMergePath(originObject, mergeRule);
        assertEquals(mergePath, result);
    }

    @Test
    public void addMissingLevels() {
        MergePrepare mergePrepareObject = new MergePrepare();
        String result = mergePrepareObject.addMissingLevels(originObject, mergeObject, mergeRule, mergePath);
        assertEquals(mergedObject, result.replace("\"",""));
    }


    @Parameters
    public static Collection<Object[]> inputTestData() {
        String testData = null;
        Collection<Object[]> baseList = new ArrayList<Object[]>();
        try {
            testData = FileUtils.readFileToString(new File(testDataPath));
            JSONArray testDataJson = new JSONArray(testData);
            for (int i=0; i<testDataJson.length(); i++) {
                ArrayList<String> childList = new ArrayList<String>();
                for (int k=0; k<((JSONArray)testDataJson.get(i)).length(); k++) {
                    childList.add(((String)((JSONArray) testDataJson.get(i)).get(k)));
                }
                baseList.add(childList.toArray());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return baseList;
    }
}