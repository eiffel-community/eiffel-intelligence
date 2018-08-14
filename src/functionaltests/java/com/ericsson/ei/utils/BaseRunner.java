/*
   Copyright 2018 Ericsson AB.
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

import com.ericsson.ei.MongoClientInitializer;
import com.mongodb.MongoClient;
import cucumber.api.junit.Cucumber;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;

@Ignore
@RunWith(Cucumber.class)
public class BaseRunner {

    private static MongoClient mongoClient;

    @BeforeClass
    public static void setUp() {
        mongoClient = MongoClientInitializer.borrow();
        String port = "" + mongoClient.getAddress().getPort();
        System.setProperty("spring.data.mongodb.port", port);
    }

    @AfterClass
    public static void returnMongoClient() {
        MongoClientInitializer.returnMongoClient(mongoClient);
    }
}