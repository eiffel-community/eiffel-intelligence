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

/**
 * A class representation of the POST body to the search API's upstream/downstream method.
 */
public class SearchParameters {

    private List<LinkType> dlt;
    private List<LinkType> ult;

    public SearchParameters() {
    }

    public SearchParameters(final List<LinkType> dlt, final List<LinkType> ult) {
        this.dlt = dlt;
        this.ult = ult;
    }

    private List<LinkType> checkForAll(final List<LinkType> list) {
        if (list.size() == 1 && list.get(0).equals(LinkType.ALL)) {
            return Arrays.asList(LinkType.values());
        } else {
            return list;
        }
    }

    public List<LinkType> getDlt() {
        return checkForAll(dlt);
    }

    public void setDlt(final List<LinkType> dlt) {
        this.dlt = dlt;
    }

    public List<LinkType> getUlt() {
        return checkForAll(ult);
    }

    public void setUlt(final List<LinkType> ult) {
        this.ult = ult;
    }

    @Override
    public String toString() {
        return "SearchParameters{" + "dlt=" + dlt + ", ult=" + ult + '}';
    }
}
