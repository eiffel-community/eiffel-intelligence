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
package com.ericsson.ei.erqueryservice;

import java.util.Arrays;
import java.util.List;

public enum LinkType {
    // Link types
    CAUSE,
    CONTEXT,
    FLOW_CONTEXT,
    ACTIVITY_EXECUTION,
    PREVIOUS_ACTIVITY_EXECUTION,
    PREVIOUS_VERSION,
    COMPOSITION,
    ENVIRONMENT,
    ARTIFACT,
    SUBJECT,
    ELEMENT,
    BASE,
    CHANGE,
    TEST_SUITE_EXECUTION,
    TEST_CASE_EXECUTION,
    IUT,
    TERC,
    MODIFIED_ANNOUNCEMENT,
    SUB_CONFIDENCE_LEVEL,
    REUSED_ARTIFACT,
    VERIFICATION_BASIS,
    ALL;

    public static List<LinkType> asList() {
        return Arrays.asList(values());
    }
}